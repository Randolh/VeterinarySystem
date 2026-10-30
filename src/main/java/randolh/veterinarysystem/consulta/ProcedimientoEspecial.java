package randolh.veterinarysystem.consulta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Modelo entidad para cirugias, sedaciones y procedimientos quirurgicos especiales
public class ProcedimientoEspecial {

    private Integer id;
    private int mascotaId;
    private int veterinarioId;
    private String tipoProcedimiento;
    private String nombreProcedimiento;
    private LocalDateTime fechaHora;
    private Integer duracionEstimadaMinutos;
    private String informacionPreoperatoria;
    private String detalleProcedimiento;
    private String complicaciones;
    private String seguimientoPostoperatorio;
    private LocalDate proximoControl;
    private EstadoProcedimientoEnum estado;
    private BigDecimal costoProcedimiento;

    // Campos auxiliares para visualizacion
    private String nombreMascota;
    private String nombreDueno;
    private String nombreVeterinario;

    // Lista de insumos quirurgicos utilizados
    private List<InsumoProcedimiento> insumos;

    public ProcedimientoEspecial() {
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoProcedimientoEnum.PROGRAMADO;
        this.insumos = new ArrayList<>();
    }

    public ProcedimientoEspecial(Integer id, int mascotaId, int veterinarioId, String tipoProcedimiento,
                                 String nombreProcedimiento, LocalDateTime fechaHora, Integer duracionEstimadaMinutos,
                                 String informacionPreoperatoria, String detalleProcedimiento, String complicaciones,
                                 String seguimientoPostoperatorio, LocalDate proximoControl,
                                 EstadoProcedimientoEnum estado, BigDecimal costoProcedimiento) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.veterinarioId = veterinarioId;
        this.tipoProcedimiento = tipoProcedimiento;
        this.nombreProcedimiento = nombreProcedimiento;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
        this.duracionEstimadaMinutos = duracionEstimadaMinutos;
        this.informacionPreoperatoria = informacionPreoperatoria;
        this.detalleProcedimiento = detalleProcedimiento;
        this.complicaciones = complicaciones;
        this.seguimientoPostoperatorio = seguimientoPostoperatorio;
        this.proximoControl = proximoControl;
        this.estado = estado != null ? estado : EstadoProcedimientoEnum.PROGRAMADO;
        this.costoProcedimiento = costoProcedimiento != null ? costoProcedimiento : BigDecimal.ZERO;
        this.insumos = new ArrayList<>();
    }

    public ProcedimientoEspecial(int mascotaId, int veterinarioId, String tipoProcedimiento,
                                 String nombreProcedimiento, String detalleProcedimiento,
                                 BigDecimal costoProcedimiento) {
        this(null, mascotaId, veterinarioId, tipoProcedimiento, nombreProcedimiento,
                LocalDateTime.now(), null, null, detalleProcedimiento, null, null, null,
                EstadoProcedimientoEnum.PROGRAMADO, costoProcedimiento);
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getMascotaId() { return mascotaId; }
    public void setMascotaId(int mascotaId) { this.mascotaId = mascotaId; }

    public int getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(int veterinarioId) { this.veterinarioId = veterinarioId; }

    public String getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(String tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }

    public String getNombreProcedimiento() { return nombreProcedimiento; }
    public void setNombreProcedimiento(String nombreProcedimiento) { this.nombreProcedimiento = nombreProcedimiento; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getDuracionEstimadaMinutos() { return duracionEstimadaMinutos; }
    public void setDuracionEstimadaMinutos(Integer duracionEstimadaMinutos) { this.duracionEstimadaMinutos = duracionEstimadaMinutos; }

    public String getInformacionPreoperatoria() { return informacionPreoperatoria; }
    public void setInformacionPreoperatoria(String informacionPreoperatoria) { this.informacionPreoperatoria = informacionPreoperatoria; }

    public String getDetalleProcedimiento() { return detalleProcedimiento; }
    public void setDetalleProcedimiento(String detalleProcedimiento) { this.detalleProcedimiento = detalleProcedimiento; }

    public String getComplicaciones() { return complicaciones; }
    public void setComplicaciones(String complicaciones) { this.complicaciones = complicaciones; }

    public String getSeguimientoPostoperatorio() { return seguimientoPostoperatorio; }
    public void setSeguimientoPostoperatorio(String seguimientoPostoperatorio) { this.seguimientoPostoperatorio = seguimientoPostoperatorio; }

    public LocalDate getProximoControl() { return proximoControl; }
    public void setProximoControl(LocalDate proximoControl) { this.proximoControl = proximoControl; }

    public EstadoProcedimientoEnum getEstado() { return estado; }
    public void setEstado(EstadoProcedimientoEnum estado) {
        this.estado = estado != null ? estado : EstadoProcedimientoEnum.PROGRAMADO;
    }

    public BigDecimal getCostoProcedimiento() { return costoProcedimiento; }
    public void setCostoProcedimiento(BigDecimal costoProcedimiento) { this.costoProcedimiento = costoProcedimiento; }

    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }

    public List<InsumoProcedimiento> getInsumos() { return insumos; }
    public void setInsumos(List<InsumoProcedimiento> insumos) {
        this.insumos = insumos != null ? insumos : new ArrayList<>();
    }

    public void agregarInsumo(InsumoProcedimiento insumo) {
        if (this.insumos == null) {
            this.insumos = new ArrayList<>();
        }
        this.insumos.add(insumo);
    }

    @Override
    public String toString() {
        return String.format("ProcedimientoEspecial[id=%d, tipo='%s', nombre='%s', paciente='%s', estado=%s]",
                id, tipoProcedimiento, nombreProcedimiento, nombreMascota != null ? nombreMascota : mascotaId, estado);
    }
}
