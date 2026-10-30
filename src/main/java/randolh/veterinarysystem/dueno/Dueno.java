package randolh.veterinarysystem.dueno;

import java.time.LocalDateTime;

// Modelo entidad de duenos y clientes
public class Dueno {

    private Integer id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String direccion;
    private String telefono;
    private String email;
    private String contactoEmergencia;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public Dueno() {
        this.tipoDocumento = "CC";
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Dueno(Integer id, String tipoDocumento, String numeroDocumento, String nombreCompleto,
                 String direccion, String telefono, String email, String contactoEmergencia,
                 LocalDateTime fechaRegistro, boolean activo) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombreCompleto = nombreCompleto;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.contactoEmergencia = contactoEmergencia;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDateTime.now();
        this.activo = activo;
    }

    public Dueno(String tipoDocumento, String numeroDocumento, String nombreCompleto,
                 String direccion, String telefono, String email, String contactoEmergencia) {
        this(null, tipoDocumento, numeroDocumento, nombreCompleto, direccion, telefono, email,
                contactoEmergencia, LocalDateTime.now(), true);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return String.format("Dueno[id=%d, doc=%s-%s, nombre='%s', tel='%s', email='%s', activo=%b]",
                id, tipoDocumento, numeroDocumento, nombreCompleto, telefono, email, activo);
    }
}
