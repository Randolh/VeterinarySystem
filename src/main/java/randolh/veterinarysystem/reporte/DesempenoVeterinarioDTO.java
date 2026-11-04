package randolh.veterinarysystem.reporte;

// DTO para el analisis de productividad y desempeno del equipo veterinario
public class DesempenoVeterinarioDTO {

    private int veterinarioId;
    private String nombreCompleto;
    private String licenciaProfesional;
    private String especialidad;
    private int totalConsultas;
    private int totalProcedimientos;
    private int totalAtenciones;
    private double ingresosGenerados;

    public DesempenoVeterinarioDTO() {
    }

    public DesempenoVeterinarioDTO(int veterinarioId, String nombreCompleto, String licenciaProfesional,
                                   String especialidad, int totalConsultas, int totalProcedimientos,
                                   int totalAtenciones, double ingresosGenerados) {
        this.veterinarioId = veterinarioId;
        this.nombreCompleto = nombreCompleto;
        this.licenciaProfesional = licenciaProfesional;
        this.especialidad = especialidad != null ? especialidad : "General";
        this.totalConsultas = totalConsultas;
        this.totalProcedimientos = totalProcedimientos;
        this.totalAtenciones = totalAtenciones;
        this.ingresosGenerados = ingresosGenerados;
    }

    public int getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(int veterinarioId) { this.veterinarioId = veterinarioId; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getLicenciaProfesional() { return licenciaProfesional; }
    public void setLicenciaProfesional(String licenciaProfesional) { this.licenciaProfesional = licenciaProfesional; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public int getTotalConsultas() { return totalConsultas; }
    public void setTotalConsultas(int totalConsultas) { this.totalConsultas = totalConsultas; }

    public int getTotalProcedimientos() { return totalProcedimientos; }
    public void setTotalProcedimientos(int totalProcedimientos) { this.totalProcedimientos = totalProcedimientos; }

    public int getTotalAtenciones() { return totalAtenciones; }
    public void setTotalAtenciones(int totalAtenciones) { this.totalAtenciones = totalAtenciones; }

    public double getIngresosGenerados() { return ingresosGenerados; }
    public void setIngresosGenerados(double ingresosGenerados) { this.ingresosGenerados = ingresosGenerados; }

    @Override
    public String toString() {
        return String.format("%-26s | %-16s | %3d cons. | %3d proc. | %3d aten. | $ %10.2f",
                nombreCompleto, especialidad, totalConsultas, totalProcedimientos, totalAtenciones, ingresosGenerados);
    }
}
