package randolh.veterinarysystem.facturacion;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.exportador.ExportadorFactory;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.exportador.factura.FacturaExportData;
import randolh.veterinarysystem.core.patterns.strategy.EstrategiaDescuento;
import randolh.veterinarysystem.core.patterns.strategy.FactoriaEstrategias;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.dueno.Dueno;
import randolh.veterinarysystem.dueno.DuenoDAO;
import randolh.veterinarysystem.inventario.InventarioDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Controlador de negocio para facturacion, aplicacion de estrategia de descuentos y exportacion dual
public class FacturacionController {

    private static final String MODULO = "FacturacionController";

    private final FacturacionDAO facturacionDAO;
    private final DuenoDAO duenoDAO;
    private final InventarioDAO inventarioDAO;

    public FacturacionController() {
        this.facturacionDAO = new FacturacionDAO();
        this.duenoDAO = new DuenoDAO();
        this.inventarioDAO = new InventarioDAO();
    }

    public FacturacionController(FacturacionDAO facturacionDAO, DuenoDAO duenoDAO, InventarioDAO inventarioDAO) {
        this.facturacionDAO = facturacionDAO != null ? facturacionDAO : new FacturacionDAO();
        this.duenoDAO = duenoDAO != null ? duenoDAO : new DuenoDAO();
        this.inventarioDAO = inventarioDAO != null ? inventarioDAO : new InventarioDAO();
    }

    // Emite una factura contable aplicando la estrategia de descuento segun el nivel del club del dueno
    public Factura emitirFactura(int duenoId, List<ItemFactura> items, MetodoPagoEnum metodoPago, String observaciones) {
        if (items == null || items.isEmpty()) {
            throw new ValidacionException("La factura debe contener al menos un producto o servicio.");
        }

        // 1. Validar existencia del dueno o cliente receptor
        Optional<Dueno> duenoOpt = duenoDAO.buscarPorId(duenoId);
        if (duenoOpt.isEmpty()) {
            throw new ValidacionException("El cliente con ID " + duenoId + " no se encuentra registrado.");
        }
        Dueno dueno = duenoOpt.get();
        if (!dueno.isActivo()) {
            throw new ValidacionException("El cliente " + dueno.getNombreCompleto() + " se encuentra inactivo.");
        }

        // 2. Determinar membresia y resolver patron Strategy para descuentos
        Optional<FacturacionDAO.InfoClubDueno> clubOpt = facturacionDAO.obtenerInfoClubDueno(duenoId);
        String nivelClub = clubOpt.map(FacturacionDAO.InfoClubDueno::getNivel).orElse("Sin Club");
        EstrategiaDescuento estrategia = FactoriaEstrategias.obtenerEstrategia(nivelClub);

        // 3. Liquidar montos financieros y tributarios
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemFactura it : items) {
            if (it.getCantidad() <= 0) {
                throw new ValidacionException("La cantidad para '" + it.getNombreItem() + "' debe ser mayor a 0.");
            }
            if (it.getPrecioUnitario() == null || it.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidacionException("El precio unitario de '" + it.getNombreItem() + "' no puede ser negativo.");
            }
            subtotal = subtotal.add(it.getSubtotal());
        }

        // Aplicacion dinamica de la estrategia
        double descVal = estrategia.aplicarDescuento(subtotal.doubleValue(), items);
        BigDecimal descuento = BigDecimal.valueOf(descVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal baseGravable = subtotal.subtract(descuento);
        if (baseGravable.compareTo(BigDecimal.ZERO) < 0) {
            baseGravable = BigDecimal.ZERO;
        }

        // IVA parametrico institucional
        double ivaPorc = ConexionBD.getInstancia().getIvaPorcentaje();
        BigDecimal impuesto = baseGravable.multiply(BigDecimal.valueOf(ivaPorc / 100.0))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = baseGravable.add(impuesto).setScale(2, RoundingMode.HALF_UP);
        int puntosGanados = estrategia.calcularPuntosGanados(total.doubleValue());

        // 4. Construir entidad de factura
        String numeroFactura = facturacionDAO.generarSiguienteNumeroFactura();
        Factura factura = new Factura(
                null,
                duenoId,
                numeroFactura,
                LocalDateTime.now(),
                subtotal.setScale(2, RoundingMode.HALF_UP),
                impuesto,
                descuento.setScale(2, RoundingMode.HALF_UP),
                total,
                metodoPago != null ? metodoPago : MetodoPagoEnum.EFECTIVO,
                EstadoFacturaEnum.PAGADA,
                observaciones
        );

        factura.setNombreDueno(dueno.getNombreCompleto());
        factura.setDocumentoDueno(dueno.getNumeroDocumento());
        factura.setEmailDueno(dueno.getEmail());
        factura.setTelefonoDueno(dueno.getTelefono());
        factura.setDireccionDueno(dueno.getDireccion());
        factura.setNivelClub(nivelClub);
        factura.setPuntosGanados(puntosGanados);
        factura.setItems(items);

        // 5. Persistencia transaccional atomica
        Factura facturaGuardada = facturacionDAO.insertarFacturaTransaccional(factura, puntosGanados);

        // 6. Generacion automatica de documentos (Texto plano y PDF estandar)
        exportarComprobantes(facturaGuardada);

        LoggerManager.info(MODULO, String.format("Factura emitida exitosamente: %s | Total: $%.2f | Nivel: %s | Puntos: +%d",
                facturaGuardada.getNumeroFactura(), facturaGuardada.getTotal(), nivelClub, puntosGanados));

        return facturaGuardada;
    }

    // Exporta la factura en formatos .txt y .pdf sin dependencias
    public void exportarComprobantes(Factura factura) {
        ConexionBD config = ConexionBD.getInstancia();
        String clinicaNombre = config.getNombreClinica();
        String clinicaNit = config.getNitClinica();
        String clinicaTel = config.getTelefonoClinica();
        String clinicaDir = config.getDireccionClinica();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaStr = factura.getFechaEmision() != null ? factura.getFechaEmision().format(dtf) : "";

        List<String[]> itemsList = new ArrayList<>();
        if (factura.getItems() != null) {
            for (ItemFactura it : factura.getItems()) {
                itemsList.add(new String[]{
                        it.getTipoItem().getValor(),
                        it.getNombreItem(),
                        String.valueOf(it.getCantidad()),
                        String.format(Locale.US, "%.2f", it.getPrecioUnitario()),
                        String.format(Locale.US, "%.2f", it.getSubtotal())
                });
            }
        }

        FacturaExportData data = FacturaExportData.builder()
                .conClinica(clinicaNombre, clinicaNit, clinicaTel, clinicaDir)
                .conFactura(factura.getNumeroFactura(), fechaStr)
                .conCliente(factura.getNombreDueno(), factura.getDocumentoDueno(), factura.getEmailDueno(), factura.getTelefonoDueno())
                .conClub(factura.getNivelClub(), factura.getPuntosGanados())
                .conItems(itemsList)
                .conMontos(factura.getSubtotal().doubleValue(), factura.getDescuento().doubleValue(), factura.getImpuesto().doubleValue(), factura.getTotal().doubleValue())
                .conPago(factura.getMetodoPago().getValor(), factura.getEstado().getValor())
                .build();

        ExportadorFactory.getExportadorFactura(FormatoExportacion.TEXTO).exportar(data);
        ExportadorFactory.getExportadorFactura(FormatoExportacion.PDF).exportar(data);
    }

    // Busca factura por ID
    public Optional<Factura> buscarFacturaPorId(int id) {
        return facturacionDAO.buscarPorId(id);
    }

    // Busca factura por numero
    public Optional<Factura> buscarFacturaPorNumero(String numero) {
        return facturacionDAO.buscarPorNumero(numero);
    }

    // Lista todas las facturas
    public List<Factura> listarFacturas() {
        return facturacionDAO.listarTodas();
    }

    // Lista facturas por dueno
    public List<Factura> listarFacturasPorDueno(int duenoId) {
        return facturacionDAO.listarPorDueno(duenoId);
    }

    // Anula una factura emitida
    public boolean anularFactura(int id) {
        Optional<Factura> opt = facturacionDAO.buscarPorId(id);
        if (opt.isEmpty()) {
            throw new ValidacionException("La factura con ID " + id + " no existe.");
        }
        Factura f = opt.get();
        if (f.getEstado() == EstadoFacturaEnum.ANULADA) {
            throw new ValidacionException("La factura ya se encuentra anulada.");
        }
        return facturacionDAO.anularFactura(id);
    }

    // Consulta el catalogo de servicios
    public List<FacturacionDAO.ServicioCatalogo> listarCatalogoServicios() {
        return facturacionDAO.listarCatalogoServicios();
    }

    // Obtiene informacion de membresia club de un dueno
    public Optional<FacturacionDAO.InfoClubDueno> obtenerInfoClub(int duenoId) {
        return facturacionDAO.obtenerInfoClubDueno(duenoId);
    }

    // Resuelve la estrategia de descuento para un dueno
    public EstrategiaDescuento resolverEstrategiaDueno(int duenoId) {
        Optional<FacturacionDAO.InfoClubDueno> clubOpt = facturacionDAO.obtenerInfoClubDueno(duenoId);
        String nivel = clubOpt.map(FacturacionDAO.InfoClubDueno::getNivel).orElse("Sin Club");
        return FactoriaEstrategias.obtenerEstrategia(nivel);
    }

    public FacturacionDAO getFacturacionDAO() { return facturacionDAO; }
    public DuenoDAO getDuenoDAO() { return duenoDAO; }
    public InventarioDAO getInventarioDAO() { return inventarioDAO; }
}
