package randolh.veterinarysystem.cita;

import java.time.LocalDateTime;

// Modelo entidad de citas medicas con ciclo de vida gobernado por el patron State
public class Cita {

    private Integer id;
    private int mascotaId;
    private Integer veterinarioId;
    private LocalDateTime fechaHora;
    private String motivo;
    private EstadoCitaEnum estado;
    private String notas;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;
    private LocalDateTime fechaCreacion;

    // Campos auxiliares para visualizacion de consultas
    private String nombreMascota;
    private String nombreDueno;
    private String telefonoDueno;
    private String nombreVeterinario;

    // Estado interno del patron State
    private EstadoCitaState estadoState;

    public Cita() {
        this.estado = EstadoCitaEnum.PROGRAMADA;
        this.estadoState = EstadoCitaState.desdeEnum(this.estado);
        this.fechaCreacion = LocalDateTime.now();
    }

    public Cita(Integer id, int mascotaId, Integer veterinarioId, LocalDateTime fechaHora,
                String motivo, EstadoCitaEnum estado, String notas, LocalDateTime fechaCancelacion,
                String motivoCancelacion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.veterinarioId = veterinarioId;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = estado != null ? estado : EstadoCitaEnum.PROGRAMADA;
        this.estadoState = EstadoCitaState.desdeEnum(this.estado);
        this.notas = notas;
        this.fechaCancelacion = fechaCancelacion;
        this.motivoCancelacion = motivoCancelacion;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
    }

    public Cita(int mascotaId, Integer veterinarioId, LocalDateTime fechaHora, String motivo, String notas) {
        this(null, mascotaId, veterinarioId, fechaHora, motivo, EstadoCitaEnum.PROGRAMADA, notas,
                null, null, LocalDateTime.now());
    }

    // Metodos de transicion delegados al patron State
    public void confirmar() {
        estadoState.confirmar(this);
    }

    public void iniciarAtencion() {
        estadoState.iniciarAtencion(this);
    }

    public void finalizar() {
        estadoState.finalizar(this);
    }

    public void cancelar(String motivo) {
        estadoState.cancelar(this, motivo);
    }

    public void marcarNoAsistio() {
        estadoState.marcarNoAsistio(this);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getMascotaId() { return mascotaId; }
    public void setMascotaId(int mascotaId) { this.mascotaId = mascotaId; }

    public Integer getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Integer veterinarioId) { this.veterinarioId = veterinarioId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoCitaEnum getEstado() { return estado; }
    public void setEstado(EstadoCitaEnum estado) {
        this.estado = estado != null ? estado : EstadoCitaEnum.PROGRAMADA;
        this.estadoState = EstadoCitaState.desdeEnum(this.estado);
    }

    public EstadoCitaState getEstadoState() { return estadoState; }
    public void setEstadoState(EstadoCitaState nuevoState) {
        this.estadoState = nuevoState;
        this.estado = nuevoState.getEstadoEnum();
    }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(LocalDateTime fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getTelefonoDueno() { return telefonoDueno; }
    public void setTelefonoDueno(String telefonoDueno) { this.telefonoDueno = telefonoDueno; }

    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }

    @Override
    public String toString() {
        return String.format("Cita[id=%d, mascota='%s', vet='%s', fechaHora=%s, estado=%s]",
                id, nombreMascota != null ? nombreMascota : mascotaId,
                nombreVeterinario != null ? nombreVeterinario : veterinarioId,
                fechaHora, estado);
    }
}
