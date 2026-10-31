package randolh.veterinarysystem.actividad;

import java.time.LocalDate;

// Entidad para la formalizacion legal de contratos de adopcion
public class Adopcion {

    private Integer id;
    private int mascotaAdopcionId;
    private int duenoId;
    private LocalDate fechaAdopcion;
    private String contratoTexto;
    private String condicionesEspeciales;
    private boolean seguimientoRequerido;
    private LocalDate fechaPrimerSeguimiento;

    // Campos auxiliares para generacion de contratos y visualizacion
    private String nombreMascota;
    private String especieMascota;
    private String razaMascota;
    private String nombreAdoptante;
    private String documentoAdoptante;
    private String direccionAdoptante;
    private String telefonoAdoptante;
    private String emailAdoptante;

    public Adopcion() {
        this.fechaAdopcion = LocalDate.now();
        this.seguimientoRequerido = true;
        this.fechaPrimerSeguimiento = LocalDate.now().plusDays(30);
    }

    public Adopcion(Integer id, int mascotaAdopcionId, int duenoId, LocalDate fechaAdopcion,
                    String contratoTexto, String condicionesEspeciales, boolean seguimientoRequerido,
                    LocalDate fechaPrimerSeguimiento) {
        this.id = id;
        this.mascotaAdopcionId = mascotaAdopcionId;
        this.duenoId = duenoId;
        this.fechaAdopcion = fechaAdopcion != null ? fechaAdopcion : LocalDate.now();
        this.contratoTexto = contratoTexto;
        this.condicionesEspeciales = condicionesEspeciales;
        this.seguimientoRequerido = seguimientoRequerido;
        this.fechaPrimerSeguimiento = fechaPrimerSeguimiento != null ? fechaPrimerSeguimiento : LocalDate.now().plusDays(30);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getMascotaAdopcionId() { return mascotaAdopcionId; }
    public void setMascotaAdopcionId(int mascotaAdopcionId) { this.mascotaAdopcionId = mascotaAdopcionId; }

    public int getDuenoId() { return duenoId; }
    public void setDuenoId(int duenoId) { this.duenoId = duenoId; }

    public LocalDate getFechaAdopcion() { return fechaAdopcion; }
    public void setFechaAdopcion(LocalDate fechaAdopcion) { this.fechaAdopcion = fechaAdopcion; }

    public String getContratoTexto() { return contratoTexto; }
    public void setContratoTexto(String contratoTexto) { this.contratoTexto = contratoTexto; }

    public String getCondicionesEspeciales() { return condicionesEspeciales; }
    public void setCondicionesEspeciales(String condicionesEspeciales) { this.condicionesEspeciales = condicionesEspeciales; }

    public boolean isSeguimientoRequerido() { return seguimientoRequerido; }
    public void setSeguimientoRequerido(boolean seguimientoRequerido) { this.seguimientoRequerido = seguimientoRequerido; }

    public LocalDate getFechaPrimerSeguimiento() { return fechaPrimerSeguimiento; }
    public void setFechaPrimerSeguimiento(LocalDate fechaPrimerSeguimiento) { this.fechaPrimerSeguimiento = fechaPrimerSeguimiento; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getEspecieMascota() { return especieMascota; }
    public void setEspecieMascota(String especieMascota) { this.especieMascota = especieMascota; }

    public String getRazaMascota() { return razaMascota; }
    public void setRazaMascota(String razaMascota) { this.razaMascota = razaMascota; }

    public String getNombreAdoptante() { return nombreAdoptante; }
    public void setNombreAdoptante(String nombreAdoptante) { this.nombreAdoptante = nombreAdoptante; }

    public String getDocumentoAdoptante() { return documentoAdoptante; }
    public void setDocumentoAdoptante(String documentoAdoptante) { this.documentoAdoptante = documentoAdoptante; }

    public String getDireccionAdoptante() { return direccionAdoptante; }
    public void setDireccionAdoptante(String direccionAdoptante) { this.direccionAdoptante = direccionAdoptante; }

    public String getTelefonoAdoptante() { return telefonoAdoptante; }
    public void setTelefonoAdoptante(String telefonoAdoptante) { this.telefonoAdoptante = telefonoAdoptante; }

    public String getEmailAdoptante() { return emailAdoptante; }
    public void setEmailAdoptante(String emailAdoptante) { this.emailAdoptante = emailAdoptante; }

    @Override
    public String toString() {
        return String.format("Adopcion[id=%d, mascota=%s, adoptante=%s, fecha=%s]",
                id, nombreMascota, nombreAdoptante, fechaAdopcion);
    }
}
