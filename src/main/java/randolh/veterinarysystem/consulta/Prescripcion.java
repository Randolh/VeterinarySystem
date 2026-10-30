package randolh.veterinarysystem.consulta;

import java.time.LocalDateTime;

// Modelo entidad para prescripciones y recetas medicas
public class Prescripcion {

    private Integer id;
    private Integer consultaId;
    private Integer procedimientoId;
    private int productoId;
    private int cantidad;
    private String dosis;
    private String frecuencia;
    private Integer duracionDias;
    private String instrucciones;
    private LocalDateTime fechaPrescripcion;

    // Campos auxiliares para visualizacion
    private String nombreProducto;
    private String unidadMedida;

    public Prescripcion() {
        this.fechaPrescripcion = LocalDateTime.now();
    }

    public Prescripcion(Integer id, Integer consultaId, Integer procedimientoId, int productoId,
                        int cantidad, String dosis, String frecuencia, Integer duracionDias,
                        String instrucciones, LocalDateTime fechaPrescripcion) {
        this.id = id;
        this.consultaId = consultaId;
        this.procedimientoId = procedimientoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracionDias = duracionDias;
        this.instrucciones = instrucciones;
        this.fechaPrescripcion = fechaPrescripcion != null ? fechaPrescripcion : LocalDateTime.now();
    }

    public Prescripcion(int productoId, int cantidad, String dosis, String frecuencia,
                        Integer duracionDias, String instrucciones) {
        this(null, null, null, productoId, cantidad, dosis, frecuencia, duracionDias, instrucciones, LocalDateTime.now());
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getConsultaId() { return consultaId; }
    public void setConsultaId(Integer consultaId) { this.consultaId = consultaId; }

    public Integer getProcedimientoId() { return procedimientoId; }
    public void setProcedimientoId(Integer procedimientoId) { this.procedimientoId = procedimientoId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }

    public LocalDateTime getFechaPrescripcion() { return fechaPrescripcion; }
    public void setFechaPrescripcion(LocalDateTime fechaPrescripcion) { this.fechaPrescripcion = fechaPrescripcion; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    @Override
    public String toString() {
        return String.format("Prescripcion[id=%d, producto='%s', cant=%d, dosis='%s', frec='%s']",
                id, nombreProducto != null ? nombreProducto : productoId, cantidad, dosis, frecuencia);
    }
}
