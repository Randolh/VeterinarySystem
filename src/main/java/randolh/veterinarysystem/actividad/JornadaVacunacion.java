package randolh.veterinarysystem.actividad;

import java.time.LocalDate;
import java.time.LocalTime;

// Entidad representativa de campanas y jornadas masivas de vacunacion comunitaria
public class JornadaVacunacion {

    private Integer id;
    private String nombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String ubicacion;
    private String descripcion;
    private Integer capacidadMaxima;
    private EstadoJornadaEnum estado;

    // Campos auxiliares para calculo de atencion y avance
    private int totalRegistrados;

    public JornadaVacunacion() {
        this.fecha = LocalDate.now();
        this.horaInicio = LocalTime.of(8, 0);
        this.horaFin = LocalTime.of(16, 0);
        this.capacidadMaxima = 100;
        this.estado = EstadoJornadaEnum.PLANIFICADA;
    }

    public JornadaVacunacion(Integer id, String nombre, LocalDate fecha, LocalTime horaInicio,
                             LocalTime horaFin, String ubicacion, String descripcion,
                             Integer capacidadMaxima, EstadoJornadaEnum estado) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha != null ? fecha : LocalDate.now();
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.capacidadMaxima = capacidadMaxima != null ? capacidadMaxima : 100;
        this.estado = estado != null ? estado : EstadoJornadaEnum.PLANIFICADA;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public EstadoJornadaEnum getEstado() { return estado; }
    public void setEstado(EstadoJornadaEnum estado) { this.estado = estado; }

    public int getTotalRegistrados() { return totalRegistrados; }
    public void setTotalRegistrados(int totalRegistrados) { this.totalRegistrados = totalRegistrados; }

    public double getPorcentajeAvance() {
        if (capacidadMaxima == null || capacidadMaxima <= 0) return 0.0;
        return Math.min(100.0, (totalRegistrados * 100.0) / capacidadMaxima);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s, %s) - %d/%d vacunas [%.1f%%] - %s",
                id, nombre, fecha, ubicacion, totalRegistrados,
                capacidadMaxima != null ? capacidadMaxima : 0,
                getPorcentajeAvance(), estado);
    }
}
