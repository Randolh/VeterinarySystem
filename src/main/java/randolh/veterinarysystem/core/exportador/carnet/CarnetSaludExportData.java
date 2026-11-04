package randolh.veterinarysystem.core.exportador.carnet;

import randolh.veterinarysystem.consulta.ProcedimientoEspecial;
import randolh.veterinarysystem.mascota.MascotaDAO.VacunaHistorialItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Modelo inmutable que encapsula los datos requeridos para exportar el carnet de salud
public class CarnetSaludExportData {

    private final String clinicaNombre;
    private final String clinicaNit;
    private final String clinicaTel;
    private final String clinicaDir;

    private final int mascotaId;
    private final String mascotaNombre;
    private final String especie;
    private final String raza;
    private final String sexo;
    private final String fechaNacimiento;
    private final String edadCalculada;
    private final double pesoActual;
    private final String microchip;
    private final String tatuaje;
    private final String fotoUrl;
    private final String alergias;
    private final String condicionesPreexistentes;

    private final int duenoId;
    private final String duenoNombre;

    private final List<VacunaHistorialItem> vacunas;
    private final List<ProcedimientoEspecial> cirugias;
    private final LocalDate fechaEmision;

    public CarnetSaludExportData(Builder builder) {
        this.clinicaNombre = builder.clinicaNombre != null ? builder.clinicaNombre : "";
        this.clinicaNit = builder.clinicaNit != null ? builder.clinicaNit : "";
        this.clinicaTel = builder.clinicaTel != null ? builder.clinicaTel : "";
        this.clinicaDir = builder.clinicaDir != null ? builder.clinicaDir : "";

        this.mascotaId = builder.mascotaId;
        this.mascotaNombre = builder.mascotaNombre != null ? builder.mascotaNombre : "N/D";
        this.especie = builder.especie != null ? builder.especie : "N/D";
        this.raza = builder.raza != null ? builder.raza : "Mestizo";
        this.sexo = builder.sexo != null ? builder.sexo : "N/D";
        this.fechaNacimiento = builder.fechaNacimiento != null ? builder.fechaNacimiento : "N/D";
        this.edadCalculada = builder.edadCalculada != null ? builder.edadCalculada : "N/D";
        this.pesoActual = builder.pesoActual;
        this.microchip = builder.microchip;
        this.tatuaje = builder.tatuaje;
        this.fotoUrl = builder.fotoUrl;
        this.alergias = builder.alergias;
        this.condicionesPreexistentes = builder.condicionesPreexistentes;

        this.duenoId = builder.duenoId;
        this.duenoNombre = builder.duenoNombre != null ? builder.duenoNombre : "N/D";

        this.vacunas = builder.vacunas != null ? Collections.unmodifiableList(new ArrayList<>(builder.vacunas)) : Collections.emptyList();
        this.cirugias = builder.cirugias != null ? Collections.unmodifiableList(new ArrayList<>(builder.cirugias)) : Collections.emptyList();
        this.fechaEmision = builder.fechaEmision != null ? builder.fechaEmision : LocalDate.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClinicaNombre() { return clinicaNombre; }
    public String getClinicaNit() { return clinicaNit; }
    public String getClinicaTel() { return clinicaTel; }
    public String getClinicaDir() { return clinicaDir; }

    public int getMascotaId() { return mascotaId; }
    public String getMascotaNombre() { return mascotaNombre; }
    public String getEspecie() { return especie; }
    public String getRaza() { return raza; }
    public String getSexo() { return sexo; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getEdadCalculada() { return edadCalculada; }
    public double getPesoActual() { return pesoActual; }
    public String getMicrochip() { return microchip; }
    public String getTatuaje() { return tatuaje; }
    public String getFotoUrl() { return fotoUrl; }
    public String getAlergias() { return alergias; }
    public String getCondicionesPreexistentes() { return condicionesPreexistentes; }

    public int getDuenoId() { return duenoId; }
    public String getDuenoNombre() { return duenoNombre; }

    public List<VacunaHistorialItem> getVacunas() { return vacunas; }
    public List<ProcedimientoEspecial> getCirugias() { return cirugias; }
    public LocalDate getFechaEmision() { return fechaEmision; }

    public static class Builder {
        private String clinicaNombre;
        private String clinicaNit;
        private String clinicaTel;
        private String clinicaDir;

        private int mascotaId;
        private String mascotaNombre;
        private String especie;
        private String raza;
        private String sexo;
        private String fechaNacimiento;
        private String edadCalculada;
        private double pesoActual;
        private String microchip;
        private String tatuaje;
        private String fotoUrl;
        private String alergias;
        private String condicionesPreexistentes;

        private int duenoId;
        private String duenoNombre;

        private List<VacunaHistorialItem> vacunas;
        private List<ProcedimientoEspecial> cirugias;
        private LocalDate fechaEmision;

        public Builder conClinicaNombre(String clinicaNombre) { this.clinicaNombre = clinicaNombre; return this; }
        public Builder conClinicaNit(String clinicaNit) { this.clinicaNit = clinicaNit; return this; }
        public Builder conClinicaTel(String clinicaTel) { this.clinicaTel = clinicaTel; return this; }
        public Builder conClinicaDir(String clinicaDir) { this.clinicaDir = clinicaDir; return this; }

        public Builder conMascotaId(int mascotaId) { this.mascotaId = mascotaId; return this; }
        public Builder conMascotaNombre(String mascotaNombre) { this.mascotaNombre = mascotaNombre; return this; }
        public Builder conEspecie(String especie) { this.especie = especie; return this; }
        public Builder conRaza(String raza) { this.raza = raza; return this; }
        public Builder conSexo(String sexo) { this.sexo = sexo; return this; }
        public Builder conFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; return this; }
        public Builder conEdadCalculada(String edadCalculada) { this.edadCalculada = edadCalculada; return this; }
        public Builder conPesoActual(double pesoActual) { this.pesoActual = pesoActual; return this; }
        public Builder conMicrochip(String microchip) { this.microchip = microchip; return this; }
        public Builder conTatuaje(String tatuaje) { this.tatuaje = tatuaje; return this; }
        public Builder conFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; return this; }
        public Builder conAlergias(String alergias) { this.alergias = alergias; return this; }
        public Builder conCondicionesPreexistentes(String condicionesPreexistentes) { this.condicionesPreexistentes = condicionesPreexistentes; return this; }

        public Builder conDuenoId(int duenoId) { this.duenoId = duenoId; return this; }
        public Builder conDuenoNombre(String duenoNombre) { this.duenoNombre = duenoNombre; return this; }

        public Builder conVacunas(List<VacunaHistorialItem> vacunas) { this.vacunas = vacunas; return this; }
        public Builder conCirugias(List<ProcedimientoEspecial> cirugias) { this.cirugias = cirugias; return this; }
        public Builder conFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; return this; }

        public CarnetSaludExportData build() {
            return new CarnetSaludExportData(this);
        }
    }
}

