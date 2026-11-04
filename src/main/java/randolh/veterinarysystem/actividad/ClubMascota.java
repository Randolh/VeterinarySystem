package randolh.veterinarysystem.actividad;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Entidad representativa de la membresia y estado de fidelizacion en el Club de Mascotas
public class ClubMascota {

    private Integer id;
    private int duenoId;
    private int puntosAcumulados;
    private int puntosCanjeados;
    private int puntosDisponibles;
    private NivelClubEnum nivel;
    private LocalDate fechaInscripcion;
    private LocalDateTime fechaUltimaActualizacion;
    private boolean activo;

    // Campos auxiliares para visualizacion y tablas
    private String nombreDueno;
    private String documentoDueno;
    private String telefonoDueno;
    private String emailDueno;

    public ClubMascota() {
        this.nivel = NivelClubEnum.BRONCE;
        this.fechaInscripcion = LocalDate.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
        this.activo = true;
    }

    public ClubMascota(Integer id, int duenoId, int puntosAcumulados, int puntosCanjeados,
                       int puntosDisponibles, NivelClubEnum nivel, LocalDate fechaInscripcion,
                       LocalDateTime fechaUltimaActualizacion, boolean activo) {
        this.id = id;
        this.duenoId = duenoId;
        this.puntosAcumulados = puntosAcumulados;
        this.puntosCanjeados = puntosCanjeados;
        this.puntosDisponibles = puntosDisponibles;
        this.nivel = nivel != null ? nivel : NivelClubEnum.BRONCE;
        this.fechaInscripcion = fechaInscripcion != null ? fechaInscripcion : LocalDate.now();
        this.fechaUltimaActualizacion = fechaUltimaActualizacion != null ? fechaUltimaActualizacion : LocalDateTime.now();
        this.activo = activo;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getDuenoId() { return duenoId; }
    public void setDuenoId(int duenoId) { this.duenoId = duenoId; }

    public int getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(int puntosAcumulados) { this.puntosAcumulados = puntosAcumulados; }

    public int getPuntosCanjeados() { return puntosCanjeados; }
    public void setPuntosCanjeados(int puntosCanjeados) { this.puntosCanjeados = puntosCanjeados; }

    public int getPuntosDisponibles() { return puntosDisponibles; }
    public void setPuntosDisponibles(int puntosDisponibles) { this.puntosDisponibles = puntosDisponibles; }

    public NivelClubEnum getNivel() { return nivel; }
    public void setNivel(NivelClubEnum nivel) { this.nivel = nivel; }

    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    public LocalDateTime getFechaUltimaActualizacion() { return fechaUltimaActualizacion; }
    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) { this.fechaUltimaActualizacion = fechaUltimaActualizacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getDocumentoDueno() { return documentoDueno; }
    public void setDocumentoDueno(String documentoDueno) { this.documentoDueno = documentoDueno; }

    public String getTelefonoDueno() { return telefonoDueno; }
    public void setTelefonoDueno(String telefonoDueno) { this.telefonoDueno = telefonoDueno; }

    public String getEmailDueno() { return emailDueno; }
    public void setEmailDueno(String emailDueno) { this.emailDueno = emailDueno; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Nivel: %s | Puntos Disponibles: %d (Totales: %d)",
                id, nombreDueno != null ? nombreDueno : "Dueno #" + duenoId,
                documentoDueno != null ? documentoDueno : "-", nivel, puntosDisponibles, puntosAcumulados);
    }
}
