package randolh.veterinarysystem.actividad;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Registro individual de aplicacion de biologicos en jornadas masivas
public class RegistroJornadaVacunacion {

    private Integer id;
    private int jornadaId;
    private Integer mascotaId;
    private Integer duenoId;
    private String nombreMascotaTemporal;
    private int especieId;
    private boolean esCallejeroComunitario;
    private String nombreResponsableTemporal;
    private String telefonoResponsableTemporal;
    private int vacunaId;
    private Integer veterinarioId;
    private LocalDateTime fechaHora;
    private String loteVacuna;
    private LocalDate proximaDosis;
    private String observaciones;

    // Campos auxiliares para visualizacion
    private String nombreMascota;
    private String nombreResponsable;
    private String nombreVacuna;
    private String nombreVeterinario;
    private String nombreEspecie;

    public RegistroJornadaVacunacion() {
        this.fechaHora = LocalDateTime.now();
        this.esCallejeroComunitario = false;
        this.proximaDosis = LocalDate.now().plusYears(1);
    }

    public RegistroJornadaVacunacion(Integer id, int jornadaId, Integer mascotaId, Integer duenoId,
                                     String nombreMascotaTemporal, int especieId, boolean esCallejeroComunitario,
                                     String nombreResponsableTemporal, String telefonoResponsableTemporal,
                                     int vacunaId, Integer veterinarioId, LocalDateTime fechaHora,
                                     String loteVacuna, LocalDate proximaDosis, String observaciones) {
        this.id = id;
        this.jornadaId = jornadaId;
        this.mascotaId = mascotaId;
        this.duenoId = duenoId;
        this.nombreMascotaTemporal = nombreMascotaTemporal;
        this.especieId = especieId;
        this.esCallejeroComunitario = esCallejeroComunitario;
        this.nombreResponsableTemporal = nombreResponsableTemporal;
        this.telefonoResponsableTemporal = telefonoResponsableTemporal;
        this.vacunaId = vacunaId;
        this.veterinarioId = veterinarioId;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
        this.loteVacuna = loteVacuna;
        this.proximaDosis = proximaDosis != null ? proximaDosis : LocalDate.now().plusYears(1);
        this.observaciones = observaciones;
    }

    // Constructor rapido para paciente de clinica registrado
    public static RegistroJornadaVacunacion paraPacienteClinica(int jornadaId, int mascotaId, int duenoId,
                                                               int especieId, int vacunaId, Integer veterinarioId,
                                                               String loteVacuna, String observaciones) {
        RegistroJornadaVacunacion reg = new RegistroJornadaVacunacion(
                null, jornadaId, mascotaId, duenoId, null, especieId, false,
                null, null, vacunaId, veterinarioId, LocalDateTime.now(),
                loteVacuna, LocalDate.now().plusYears(1), observaciones
        );
        return reg;
    }

    // Constructor rapido para registro comunitario con vecino responsable
    public static RegistroJornadaVacunacion paraRegistroComunitario(int jornadaId, String nombreAnimal, int especieId,
                                                                    String nombreVecino, String telefonoVecino,
                                                                    int vacunaId, Integer veterinarioId,
                                                                    String loteVacuna, String observaciones) {
        RegistroJornadaVacunacion reg = new RegistroJornadaVacunacion(
                null, jornadaId, null, null, nombreAnimal, especieId, false,
                nombreVecino, telefonoVecino, vacunaId, veterinarioId, LocalDateTime.now(),
                loteVacuna, LocalDate.now().plusYears(1), observaciones
        );
        return reg;
    }

    // Constructor rapido para animal callejero / comunitario sin titular
    public static RegistroJornadaVacunacion paraAnimalCallejero(int jornadaId, String nombreTemporal, int especieId,
                                                               String nombrePadrino, String telefonoPadrino,
                                                               int vacunaId, Integer veterinarioId,
                                                               String loteVacuna, String observaciones) {
        RegistroJornadaVacunacion reg = new RegistroJornadaVacunacion(
                null, jornadaId, null, null, nombreTemporal, especieId, true,
                nombrePadrino, telefonoPadrino, vacunaId, veterinarioId, LocalDateTime.now(),
                loteVacuna, LocalDate.now().plusYears(1), observaciones
        );
        return reg;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getJornadaId() { return jornadaId; }
    public void setJornadaId(int jornadaId) { this.jornadaId = jornadaId; }

    public Integer getMascotaId() { return mascotaId; }
    public void setMascotaId(Integer mascotaId) { this.mascotaId = mascotaId; }

    public Integer getDuenoId() { return duenoId; }
    public void setDuenoId(Integer duenoId) { this.duenoId = duenoId; }

    public String getNombreMascotaTemporal() { return nombreMascotaTemporal; }
    public void setNombreMascotaTemporal(String nombreMascotaTemporal) { this.nombreMascotaTemporal = nombreMascotaTemporal; }

    public int getEspecieId() { return especieId; }
    public void setEspecieId(int especieId) { this.especieId = especieId; }

    public boolean isEsCallejeroComunitario() { return esCallejeroComunitario; }
    public void setEsCallejeroComunitario(boolean esCallejeroComunitario) { this.esCallejeroComunitario = esCallejeroComunitario; }

    public String getNombreResponsableTemporal() { return nombreResponsableTemporal; }
    public void setNombreResponsableTemporal(String nombreResponsableTemporal) { this.nombreResponsableTemporal = nombreResponsableTemporal; }

    public String getTelefonoResponsableTemporal() { return telefonoResponsableTemporal; }
    public void setTelefonoResponsableTemporal(String telefonoResponsableTemporal) { this.telefonoResponsableTemporal = telefonoResponsableTemporal; }

    public int getVacunaId() { return vacunaId; }
    public void setVacunaId(int vacunaId) { this.vacunaId = vacunaId; }

    public Integer getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Integer veterinarioId) { this.veterinarioId = veterinarioId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getLoteVacuna() { return loteVacuna; }
    public void setLoteVacuna(String loteVacuna) { this.loteVacuna = loteVacuna; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreMascota() {
        if (nombreMascota != null && !nombreMascota.isEmpty()) return nombreMascota;
        return nombreMascotaTemporal != null ? nombreMascotaTemporal : "Animal #" + (mascotaId != null ? mascotaId : id);
    }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getNombreResponsable() {
        if (nombreResponsable != null && !nombreResponsable.isEmpty()) return nombreResponsable;
        if (esCallejeroComunitario) return "(Comunitario / Callejero sin dueno)";
        return nombreResponsableTemporal != null ? nombreResponsableTemporal : "(Sin datos)";
    }
    public void setNombreResponsable(String nombreResponsable) { this.nombreResponsable = nombreResponsable; }

    public String getNombreVacuna() { return nombreVacuna; }
    public void setNombreVacuna(String nombreVacuna) { this.nombreVacuna = nombreVacuna; }

    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }

    public String getNombreEspecie() { return nombreEspecie; }
    public void setNombreEspecie(String nombreEspecie) { this.nombreEspecie = nombreEspecie; }

    @Override
    public String toString() {
        return String.format("[%d] %s | Responsable: %s | Vacuna: %s (Lote: %s)",
                id, getNombreMascota(), getNombreResponsable(),
                nombreVacuna != null ? nombreVacuna : "Vacuna #" + vacunaId, loteVacuna);
    }
}
