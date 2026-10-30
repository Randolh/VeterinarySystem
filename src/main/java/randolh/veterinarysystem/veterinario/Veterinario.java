package randolh.veterinarysystem.veterinario;

import java.time.LocalDate;

// Modelo entidad de medicos veterinarios
public class Veterinario {

    private Integer id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String licenciaProfesional;
    private String especialidad;
    private String telefono;
    private String email;
    private LocalDate fechaContratacion;
    private boolean activo;

    public Veterinario() {
        this.tipoDocumento = "CC";
        this.activo = true;
        this.fechaContratacion = LocalDate.now();
    }

    public Veterinario(Integer id, String tipoDocumento, String numeroDocumento, String nombreCompleto,
                       String licenciaProfesional, String especialidad, String telefono, String email,
                       LocalDate fechaContratacion, boolean activo) {
        this.id = id;
        this.tipoDocumento = tipoDocumento != null ? tipoDocumento : "CC";
        this.numeroDocumento = numeroDocumento;
        this.nombreCompleto = nombreCompleto;
        this.licenciaProfesional = licenciaProfesional;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
        this.fechaContratacion = fechaContratacion != null ? fechaContratacion : LocalDate.now();
        this.activo = activo;
    }

    public Veterinario(String tipoDocumento, String numeroDocumento, String nombreCompleto,
                       String licenciaProfesional, String especialidad, String telefono,
                       String email, LocalDate fechaContratacion) {
        this(null, tipoDocumento, numeroDocumento, nombreCompleto, licenciaProfesional, especialidad,
                telefono, email, fechaContratacion, true);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getLicenciaProfesional() { return licenciaProfesional; }
    public void setLicenciaProfesional(String licenciaProfesional) { this.licenciaProfesional = licenciaProfesional; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return String.format("Veterinario[id=%d, nombre='%s', licencia='%s', especialidad='%s', activo=%b]",
                id, nombreCompleto, licenciaProfesional, especialidad, activo);
    }
}
