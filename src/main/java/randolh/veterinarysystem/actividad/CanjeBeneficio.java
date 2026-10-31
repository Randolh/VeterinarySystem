package randolh.veterinarysystem.actividad;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Entidad representativa de la redencion o canje de beneficios por puntos
public class CanjeBeneficio {

    private Integer id;
    private int clubMascotasId;
    private int beneficioId;
    private LocalDateTime fechaCanje;
    private int puntosCanjeados;
    private String estado;
    private LocalDate fechaExpiracion;
    private Integer facturaId;

    // Campos auxiliares para visualizacion
    private String nombreBeneficio;
    private String nombreDueno;
    private String nivelRequerido;

    public CanjeBeneficio() {
        this.fechaCanje = LocalDateTime.now();
        this.estado = "Aplicado";
        this.fechaExpiracion = LocalDate.now().plusMonths(1);
    }

    public CanjeBeneficio(Integer id, int clubMascotasId, int beneficioId, LocalDateTime fechaCanje,
                          int puntosCanjeados, String estado, LocalDate fechaExpiracion, Integer facturaId) {
        this.id = id;
        this.clubMascotasId = clubMascotasId;
        this.beneficioId = beneficioId;
        this.fechaCanje = fechaCanje != null ? fechaCanje : LocalDateTime.now();
        this.puntosCanjeados = puntosCanjeados;
        this.estado = estado != null ? estado : "Aplicado";
        this.fechaExpiracion = fechaExpiracion != null ? fechaExpiracion : LocalDate.now().plusMonths(1);
        this.facturaId = facturaId;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getClubMascotasId() { return clubMascotasId; }
    public void setClubMascotasId(int clubMascotasId) { this.clubMascotasId = clubMascotasId; }

    public int getBeneficioId() { return beneficioId; }
    public void setBeneficioId(int beneficioId) { this.beneficioId = beneficioId; }

    public LocalDateTime getFechaCanje() { return fechaCanje; }
    public void setFechaCanje(LocalDateTime fechaCanje) { this.fechaCanje = fechaCanje; }

    public int getPuntosCanjeados() { return puntosCanjeados; }
    public void setPuntosCanjeados(int puntosCanjeados) { this.puntosCanjeados = puntosCanjeados; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDate fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public Integer getFacturaId() { return facturaId; }
    public void setFacturaId(Integer facturaId) { this.facturaId = facturaId; }

    public String getNombreBeneficio() { return nombreBeneficio; }
    public void setNombreBeneficio(String nombreBeneficio) { this.nombreBeneficio = nombreBeneficio; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getNivelRequerido() { return nivelRequerido; }
    public void setNivelRequerido(String nivelRequerido) { this.nivelRequerido = nivelRequerido; }

    @Override
    public String toString() {
        return String.format("[%d] Beneficio: %s | Puntos: -%d pts | Estado: %s | Fecha: %s",
                id, nombreBeneficio != null ? nombreBeneficio : "Beneficio #" + beneficioId,
                puntosCanjeados, estado, fechaCanje.toLocalDate());
    }
}
