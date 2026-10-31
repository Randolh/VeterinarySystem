package randolh.veterinarysystem.alerta;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.patterns.observer.GestorEventos;
import randolh.veterinarysystem.core.patterns.observer.Observador;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.inventario.InventarioDAO;
import randolh.veterinarysystem.inventario.Producto;

import java.util.List;
import java.util.Optional;

// Controlador de logica de negocio y observador de eventos para generacion y atencion de alertas
public class AlertaController implements Observador {

    private static final String MODULO = "AlertaController";

    private final AlertaDAO alertaDAO;
    private final InventarioDAO inventarioDAO;
    private final GestorEventos gestorEventos;

    public AlertaController() {
        this(new AlertaDAO(), new InventarioDAO(), GestorEventos.getInstancia());
    }

    public AlertaController(AlertaDAO alertaDAO, InventarioDAO inventarioDAO, GestorEventos gestorEventos) {
        this.alertaDAO = alertaDAO;
        this.inventarioDAO = inventarioDAO;
        this.gestorEventos = gestorEventos;

        // Suscripcion a eventos del sistema
        if (this.gestorEventos != null) {
            this.gestorEventos.suscribir("INVENTARIO_STOCK_MINIMO", this);
            this.gestorEventos.suscribir("INVENTARIO_VENCIDO", this);
            this.gestorEventos.suscribir("INVENTARIO_PROXIMO_VENCER", this);
            this.gestorEventos.suscribir("INVENTARIO_DEDUCIDO", this);
        }
    }

    // Metodo de recepcion de eventos del patron Observer
    @Override
    public void actualizar(String tipoEvento, Object datos) {
        LoggerManager.info(MODULO, "Evento recibido en Observador: " + tipoEvento);

        if (datos instanceof Producto p) {
            procesarAlertaProducto(tipoEvento, p);
        } else if (datos instanceof Alerta a) {
            alertaDAO.insertar(a);
        }
    }

    // Procesa y persiste una alerta generada a partir de un evento de producto
    private void procesarAlertaProducto(String tipoEvento, Producto p) {
        if ("INVENTARIO_STOCK_MINIMO".equals(tipoEvento) || (p.isBajoStockMinimo() && !"INVENTARIO_VENCIDO".equals(tipoEvento))) {
            Alerta a = new Alerta(
                    TipoAlertaEnum.STOCK_MINIMO,
                    NivelSeveridadEnum.CRITICO,
                    "Stock Critico: " + p.getNombreProducto(),
                    String.format("El articulo '%s' ha alcanzado o superado el stock minimo de seguridad. Stock actual: %d, Minimo: %d",
                            p.getNombreProducto(), p.getCantidadStock(), p.getStockMinimo()),
                    p.getId(),
                    null
            );
            alertaDAO.insertar(a);
        } else if ("INVENTARIO_VENCIDO".equals(tipoEvento) || p.isVencido()) {
            Alerta a = new Alerta(
                    TipoAlertaEnum.VENCIDO,
                    NivelSeveridadEnum.CRITICO,
                    "Medicamento Vencido: " + p.getNombreProducto(),
                    String.format("El producto '%s' (Lote: %s) vencio el %s. Debe retirarse de estanteria.",
                            p.getNombreProducto(), p.getLote() != null ? p.getLote() : "-", p.getFechaVencimiento()),
                    p.getId(),
                    null
            );
            alertaDAO.insertar(a);
        } else if ("INVENTARIO_PROXIMO_VENCER".equals(tipoEvento) || p.isProximoAVencer(30)) {
            Alerta a = new Alerta(
                    TipoAlertaEnum.POR_VENCER,
                    NivelSeveridadEnum.ADVERTENCIA,
                    "Proximo a Vencer: " + p.getNombreProducto(),
                    String.format("El producto '%s' vencera el %s (en menos de 30 dias). Priorizar rotacion.",
                            p.getNombreProducto(), p.getFechaVencimiento()),
                    p.getId(),
                    null
            );
            alertaDAO.insertar(a);
        }
    }

    // Sincroniza y detecta alertas del sistema (BD y analisis en memoria)
    public int sincronizarAlertas() {
        LoggerManager.info(MODULO, "Iniciando sincronizacion y generacion de alertas...");

        // 1. Ejecutar procedimiento en base de datos si esta disponible
        alertaDAO.ejecutarGeneracionAlertasDB();

        // 2. Verificacion preventiva en Java para asegurar cobertura
        int generadas = 0;
        List<Alerta> pendientes = alertaDAO.obtenerAlertasPendientes();

        // Verificar productos con stock bajo
        List<Producto> bajoStock = inventarioDAO.listarBajoStockMinimo();
        for (Producto p : bajoStock) {
            boolean yaExiste = pendientes.stream().anyMatch(a ->
                    a.getTipoAlerta() == TipoAlertaEnum.STOCK_MINIMO &&
                    a.getProductoId() != null && a.getProductoId().equals(p.getId()));

            if (!yaExiste) {
                Alerta a = new Alerta(
                        TipoAlertaEnum.STOCK_MINIMO,
                        NivelSeveridadEnum.CRITICO,
                        "Stock Critico: " + p.getNombreProducto(),
                        String.format("El producto '%s' tiene %d unidades (Minimo requerido: %d).",
                                p.getNombreProducto(), p.getCantidadStock(), p.getStockMinimo()),
                        p.getId(),
                        null
                );
                alertaDAO.insertar(a);
                generadas++;
            }
        }

        // Verificar productos proximos a caducar en 30 dias
        List<Producto> porVencer = inventarioDAO.listarProximosAVencer(30);
        for (Producto p : porVencer) {
            boolean yaExiste = pendientes.stream().anyMatch(a ->
                    a.getTipoAlerta() == TipoAlertaEnum.POR_VENCER &&
                    a.getProductoId() != null && a.getProductoId().equals(p.getId()));

            if (!yaExiste) {
                Alerta a = new Alerta(
                        TipoAlertaEnum.POR_VENCER,
                        NivelSeveridadEnum.ADVERTENCIA,
                        "Proximo a Vencer: " + p.getNombreProducto(),
                        String.format("El lote de '%s' vence el %s.", p.getNombreProducto(), p.getFechaVencimiento()),
                        p.getId(),
                        null
                );
                alertaDAO.insertar(a);
                generadas++;
            }
        }

        LoggerManager.info(MODULO, "Sincronizacion finalizada. Nuevas alertas generadas: " + generadas);
        return generadas;
    }

    // Emite una alerta manualmente
    public Alerta emitirAlerta(TipoAlertaEnum tipo, NivelSeveridadEnum severidad, String titulo,
                              String mensaje, Integer productoId, Integer referenciaId) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ValidacionException("El titulo de la alerta es obligatorio.");
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new ValidacionException("El mensaje de la alerta es obligatorio.");
        }

        Alerta a = new Alerta(tipo, severidad, titulo.trim(), mensaje.trim(), productoId, referenciaId);
        Alerta guardada = alertaDAO.insertar(a);

        // Notificar a observadores globales
        if (gestorEventos != null) {
            gestorEventos.notificar("ALERTA_GENERADA", guardada);
        }

        return guardada;
    }

    // Obtiene alertas pendientes
    public List<Alerta> obtenerAlertasPendientes() {
        return alertaDAO.obtenerAlertasPendientes();
    }

    // Obtiene todo el historico de alertas
    public List<Alerta> obtenerTodas() {
        return alertaDAO.obtenerTodas();
    }

    // Busca alerta por ID
    public Optional<Alerta> buscarPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID de la alerta debe ser mayor a 0.");
        }
        return alertaDAO.buscarPorId(id);
    }

    // Marca alerta como atendida
    public boolean marcarComoAtendida(int id) {
        if (id <= 0) {
            throw new ValidacionException("El ID de la alerta debe ser mayor a 0.");
        }
        return alertaDAO.marcarComoAtendida(id);
    }

    // Marca todas las alertas pendientes como atendidas
    public int marcarTodasComoAtendidas() {
        return alertaDAO.marcarTodasComoAtendidas();
    }

    // Cuenta alertas pendientes
    public int contarAlertasPendientes() {
        return alertaDAO.contarAlertasPendientes();
    }
}
