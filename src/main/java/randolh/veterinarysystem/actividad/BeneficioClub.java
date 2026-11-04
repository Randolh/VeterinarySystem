package randolh.veterinarysystem.actividad;

import java.math.BigDecimal;

// Beneficios y catalogo de canje disponibles en el Club de Mascotas
public class BeneficioClub {

    private Integer id;
    private String nombre;
    private String descripcion;
    private NivelClubEnum nivelRequerido;
    private int puntosNecesarios;
    private String tipoBeneficio;
    private BigDecimal valorBeneficio;
    private boolean activo;

    public BeneficioClub() {
        this.nivelRequerido = NivelClubEnum.BRONCE;
        this.activo = true;
    }

    public BeneficioClub(Integer id, String nombre, String descripcion, NivelClubEnum nivelRequerido,
                         int puntosNecesarios, String tipoBeneficio, BigDecimal valorBeneficio, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivelRequerido = nivelRequerido != null ? nivelRequerido : NivelClubEnum.BRONCE;
        this.puntosNecesarios = puntosNecesarios;
        this.tipoBeneficio = tipoBeneficio;
        this.valorBeneficio = valorBeneficio;
        this.activo = activo;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public NivelClubEnum getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(NivelClubEnum nivelRequerido) { this.nivelRequerido = nivelRequerido; }

    public int getPuntosNecesarios() { return puntosNecesarios; }
    public void setPuntosNecesarios(int puntosNecesarios) { this.puntosNecesarios = puntosNecesarios; }

    public String getTipoBeneficio() { return tipoBeneficio; }
    public void setTipoBeneficio(String tipoBeneficio) { this.tipoBeneficio = tipoBeneficio; }

    public BigDecimal getValorBeneficio() { return valorBeneficio; }
    public void setValorBeneficio(BigDecimal valorBeneficio) { this.valorBeneficio = valorBeneficio; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Requiere: %d pts [%s]",
                id, nombre, tipoBeneficio != null ? tipoBeneficio : "General",
                puntosNecesarios, nivelRequerido);
    }
}
