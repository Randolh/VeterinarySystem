package randolh.veterinarysystem.inventario;

import java.time.LocalDateTime;

// Modelo entidad para proveedores y distribuidores farmaceuticos
public class Proveedor {

    private Integer id;
    private String nombreEmpresa;
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private String sitioWeb;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    public Proveedor() {
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Proveedor(Integer id, String nombreEmpresa, String contacto, String telefono,
                     String email, String direccion, String sitioWeb, boolean activo,
                     LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombreEmpresa = nombreEmpresa;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.sitioWeb = sitioWeb;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDateTime.now();
    }

    public Proveedor(String nombreEmpresa, String contacto, String telefono, String email,
                     String direccion, String sitioWeb) {
        this(null, nombreEmpresa, contacto, telefono, email, direccion, sitioWeb, true, LocalDateTime.now());
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getSitioWeb() { return sitioWeb; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return String.format("Proveedor[id=%d, empresa='%s', contacto='%s', tel='%s', activo=%b]",
                id, nombreEmpresa, contacto, telefono, activo);
    }
}
