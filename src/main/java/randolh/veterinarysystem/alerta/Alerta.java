package randolh.veterinarysystem.alerta;

import java.time.LocalDateTime;

// Modelo entidad para alertas y notificaciones del sistema
public class Alerta {

    private Integer id;
    private TipoAlertaEnum tipoAlerta;
    private NivelSeveridadEnum nivelSeveridad;
    private String titulo;
    private String mensaje;
    private Integer productoId;
    private Integer referenciaId;
    private LocalDateTime fechaGeneracion;
    private boolean leida;

    // Campo auxiliar para visualizacion
    private String nombreProducto;

    public Alerta() {
        this.tipoAlerta = TipoAlertaEnum.STOCK_MINIMO;
        this.nivelSeveridad = NivelSeveridadEnum.INFO;
        this.fechaGeneracion = LocalDateTime.now();
        this.leida = false;
    }

    public Alerta(Integer id, TipoAlertaEnum tipoAlerta, NivelSeveridadEnum nivelSeveridad,
                  String titulo, String mensaje, Integer productoId, Integer referenciaId,
                  LocalDateTime fechaGeneracion, boolean leida) {
        this.id = id;
        this.tipoAlerta = tipoAlerta != null ? tipoAlerta : TipoAlertaEnum.STOCK_MINIMO;
        this.nivelSeveridad = nivelSeveridad != null ? nivelSeveridad : NivelSeveridadEnum.INFO;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.productoId = productoId;
        this.referenciaId = referenciaId;
        this.fechaGeneracion = fechaGeneracion != null ? fechaGeneracion : LocalDateTime.now();
        this.leida = leida;
    }

    public Alerta(TipoAlertaEnum tipoAlerta, NivelSeveridadEnum nivelSeveridad,
                  String titulo, String mensaje, Integer productoId, Integer referenciaId) {
        this(null, tipoAlerta, nivelSeveridad, titulo, mensaje, productoId, referenciaId, LocalDateTime.now(), false);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TipoAlertaEnum getTipoAlerta() { return tipoAlerta; }
    public void setTipoAlerta(TipoAlertaEnum tipoAlerta) { this.tipoAlerta = tipoAlerta; }

    public NivelSeveridadEnum getNivelSeveridad() { return nivelSeveridad; }
    public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) { this.nivelSeveridad = nivelSeveridad; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }

    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    @Override
    public String toString() {
        return String.format("Alerta[id=%d, tipo=%s, severidad=%s, titulo='%s', leida=%b]",
                id, tipoAlerta, nivelSeveridad, titulo, leida);
    }
}
