package randolh.veterinarysystem.consulta;

// Modelo entidad para el consumo de insumos quirurgicos y material medico
public class InsumoProcedimiento {

    private Integer id;
    private Integer procedimientoId;
    private int productoId;
    private int cantidadUsada;
    private String observaciones;

    // Campos auxiliares para visualizacion
    private String nombreProducto;
    private String unidadMedida;

    public InsumoProcedimiento() {}

    public InsumoProcedimiento(Integer id, Integer procedimientoId, int productoId,
                               int cantidadUsada, String observaciones) {
        this.id = id;
        this.procedimientoId = procedimientoId;
        this.productoId = productoId;
        this.cantidadUsada = cantidadUsada;
        this.observaciones = observaciones;
    }

    public InsumoProcedimiento(int productoId, int cantidadUsada, String observaciones) {
        this(null, null, productoId, cantidadUsada, observaciones);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProcedimientoId() { return procedimientoId; }
    public void setProcedimientoId(Integer procedimientoId) { this.procedimientoId = procedimientoId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public int getCantidadUsada() { return cantidadUsada; }
    public void setCantidadUsada(int cantidadUsada) { this.cantidadUsada = cantidadUsada; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    @Override
    public String toString() {
        return String.format("InsumoProcedimiento[id=%d, producto='%s', cantidadUsada=%d %s]",
                id, nombreProducto != null ? nombreProducto : productoId, cantidadUsada,
                unidadMedida != null ? unidadMedida : "unidades");
    }
}
