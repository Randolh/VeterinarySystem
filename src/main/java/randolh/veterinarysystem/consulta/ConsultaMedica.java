package randolh.veterinarysystem.consulta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Modelo entidad para la atencion y consulta clinica veterinaria
public class ConsultaMedica {

    private Integer id;
    private int mascotaId;
    private int veterinarioId;
    private Integer citaId;
    private LocalDateTime fechaHora;
    private String motivo;
    private String sintomas;
    private String diagnostico;
    private String recomendaciones;
    private String observaciones;
    private BigDecimal pesoRegistrado;
    private BigDecimal temperatura;

    // Campos auxiliares para visualizacion
    private String nombreMascota;
    private String nombreDueno;
    private String nombreVeterinario;

    // Lista de prescripciones asociadas
    private List<Prescripcion> prescripciones;

    public ConsultaMedica() {
        this.fechaHora = LocalDateTime.now();
        this.prescripciones = new ArrayList<>();
    }

    public ConsultaMedica(Integer id, int mascotaId, int veterinarioId, Integer citaId,
                          LocalDateTime fechaHora, String motivo, String sintomas,
                          String diagnostico, String recomendaciones, String observaciones,
                          BigDecimal pesoRegistrado, BigDecimal temperatura) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.veterinarioId = veterinarioId;
        this.citaId = citaId;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
        this.motivo = motivo;
        this.sintomas = sintomas;
        this.diagnostico = diagnostico;
        this.recomendaciones = recomendaciones;
        this.observaciones = observaciones;
        this.pesoRegistrado = pesoRegistrado;
        this.temperatura = temperatura;
        this.prescripciones = new ArrayList<>();
    }

    public ConsultaMedica(int mascotaId, int veterinarioId, Integer citaId, String motivo,
                          String sintomas, String diagnostico, String recomendaciones,
                          String observaciones, BigDecimal pesoRegistrado, BigDecimal temperatura) {
        this(null, mascotaId, veterinarioId, citaId, LocalDateTime.now(), motivo,
                sintomas, diagnostico, recomendaciones, observaciones, pesoRegistrado, temperatura);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getMascotaId() { return mascotaId; }
    public void setMascotaId(int mascotaId) { this.mascotaId = mascotaId; }

    public int getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(int veterinarioId) { this.veterinarioId = veterinarioId; }

    public Integer getCitaId() { return citaId; }
    public void setCitaId(Integer citaId) { this.citaId = citaId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public BigDecimal getPesoRegistrado() { return pesoRegistrado; }
    public void setPesoRegistrado(BigDecimal pesoRegistrado) { this.pesoRegistrado = pesoRegistrado; }

    public BigDecimal getTemperatura() { return temperatura; }
    public void setTemperatura(BigDecimal temperatura) { this.temperatura = temperatura; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }

    public List<Prescripcion> getPrescripciones() { return prescripciones; }
    public void setPrescripciones(List<Prescripcion> prescripciones) {
        this.prescripciones = prescripciones != null ? prescripciones : new ArrayList<>();
    }

    public void agregarPrescripcion(Prescripcion p) {
        if (this.prescripciones == null) {
            this.prescripciones = new ArrayList<>();
        }
        this.prescripciones.add(p);
    }

    @Override
    public String toString() {
        return String.format("ConsultaMedica[id=%d, mascota='%s', vet='%s', fecha=%s, diagnostico='%s']",
                id, nombreMascota != null ? nombreMascota : mascotaId,
                nombreVeterinario != null ? nombreVeterinario : veterinarioId,
                fechaHora, diagnostico);
    }
}
