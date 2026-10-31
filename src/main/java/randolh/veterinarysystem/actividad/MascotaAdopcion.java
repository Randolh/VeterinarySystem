package randolh.veterinarysystem.actividad;

import java.time.LocalDate;

// Entidad para animales albergados y disponibles para adopcion responsable
public class MascotaAdopcion {

    private Integer id;
    private int mascotaId;
    private String historia;
    private String temperamento;
    private String necesidadesEspeciales;
    private LocalDate fechaIngreso;
    private EstadoAdopcionEnum estado;
    private LocalDate fechaAdopcion;
    private Integer duenoAdoptanteId;
    private String fotoAdicionalUrl;

    // Campos auxiliares para visualizacion y reportes
    private String nombreMascota;
    private String especieMascota;
    private String razaMascota;
    private String sexoMascota;
    private Integer edadMascota;
    private String nombreAdoptante;
    private String documentoAdoptante;
    private String telefonoAdoptante;

    public MascotaAdopcion() {
        this.fechaIngreso = LocalDate.now();
        this.estado = EstadoAdopcionEnum.DISPONIBLE;
    }

    public MascotaAdopcion(Integer id, int mascotaId, String historia, String temperamento,
                           String necesidadesEspeciales, LocalDate fechaIngreso, EstadoAdopcionEnum estado,
                           LocalDate fechaAdopcion, Integer duenoAdoptanteId, String fotoAdicionalUrl) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.historia = historia;
        this.temperamento = temperamento;
        this.necesidadesEspeciales = necesidadesEspeciales;
        this.fechaIngreso = fechaIngreso != null ? fechaIngreso : LocalDate.now();
        this.estado = estado != null ? estado : EstadoAdopcionEnum.DISPONIBLE;
        this.fechaAdopcion = fechaAdopcion;
        this.duenoAdoptanteId = duenoAdoptanteId;
        this.fotoAdicionalUrl = fotoAdicionalUrl;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getMascotaId() { return mascotaId; }
    public void setMascotaId(int mascotaId) { this.mascotaId = mascotaId; }

    public String getHistoria() { return historia; }
    public void setHistoria(String historia) { this.historia = historia; }

    public String getTemperamento() { return temperamento; }
    public void setTemperamento(String temperamento) { this.temperamento = temperamento; }

    public String getNecesidadesEspeciales() { return necesidadesEspeciales; }
    public void setNecesidadesEspeciales(String necesidadesEspeciales) { this.necesidadesEspeciales = necesidadesEspeciales; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public EstadoAdopcionEnum getEstado() { return estado; }
    public void setEstado(EstadoAdopcionEnum estado) { this.estado = estado; }

    public LocalDate getFechaAdopcion() { return fechaAdopcion; }
    public void setFechaAdopcion(LocalDate fechaAdopcion) { this.fechaAdopcion = fechaAdopcion; }

    public Integer getDuenoAdoptanteId() { return duenoAdoptanteId; }
    public void setDuenoAdoptanteId(Integer duenoAdoptanteId) { this.duenoAdoptanteId = duenoAdoptanteId; }

    public String getFotoAdicionalUrl() { return fotoAdicionalUrl; }
    public void setFotoAdicionalUrl(String fotoAdicionalUrl) { this.fotoAdicionalUrl = fotoAdicionalUrl; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getEspecieMascota() { return especieMascota; }
    public void setEspecieMascota(String especieMascota) { this.especieMascota = especieMascota; }

    public String getRazaMascota() { return razaMascota; }
    public void setRazaMascota(String razaMascota) { this.razaMascota = razaMascota; }

    public String getSexoMascota() { return sexoMascota; }
    public void setSexoMascota(String sexoMascota) { this.sexoMascota = sexoMascota; }

    public Integer getEdadMascota() { return edadMascota; }
    public void setEdadMascota(Integer edadMascota) { this.edadMascota = edadMascota; }

    public String getNombreAdoptante() { return nombreAdoptante; }
    public void setNombreAdoptante(String nombreAdoptante) { this.nombreAdoptante = nombreAdoptante; }

    public String getDocumentoAdoptante() { return documentoAdoptante; }
    public void setDocumentoAdoptante(String documentoAdoptante) { this.documentoAdoptante = documentoAdoptante; }

    public String getTelefonoAdoptante() { return telefonoAdoptante; }
    public void setTelefonoAdoptante(String telefonoAdoptante) { this.telefonoAdoptante = telefonoAdoptante; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s, %s) - Estado: %s",
                id, nombreMascota != null ? nombreMascota : "Mascota #" + mascotaId,
                especieMascota != null ? especieMascota : "Especie ?",
                razaMascota != null ? razaMascota : "Mestizo", estado);
    }
}
