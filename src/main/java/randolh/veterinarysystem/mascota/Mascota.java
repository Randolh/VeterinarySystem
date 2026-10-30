package randolh.veterinarysystem.mascota;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

// Modelo entidad de pacientes mascotas con patron Builder
public class Mascota {

    private Integer id;
    private int duenoId;
    private String nombre;
    private int especieId;
    private int razaId;
    private LocalDate fechaNacimiento;
    private SexoEnum sexo;
    private double pesoActual;
    private String microchip;
    private String tatuaje;
    private String fotoUrl;
    private String alergias;
    private String condicionesPreexistentes;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    // Campos auxiliares para visualizacion
    private String nombreEspecie;
    private String nombreRaza;
    private String nombreDueno;

    public Mascota() {
        this.sexo = SexoEnum.MACHO;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    private Mascota(Builder builder) {
        this.id = builder.id;
        this.duenoId = builder.duenoId;
        this.nombre = builder.nombre;
        this.especieId = builder.especieId;
        this.razaId = builder.razaId;
        this.fechaNacimiento = builder.fechaNacimiento;
        this.sexo = builder.sexo != null ? builder.sexo : SexoEnum.MACHO;
        this.pesoActual = builder.pesoActual;
        this.microchip = builder.microchip;
        this.tatuaje = builder.tatuaje;
        this.fotoUrl = builder.fotoUrl;
        this.alergias = builder.alergias;
        this.condicionesPreexistentes = builder.condicionesPreexistentes;
        this.fechaRegistro = builder.fechaRegistro != null ? builder.fechaRegistro : LocalDateTime.now();
        this.activo = builder.activo;
        this.nombreEspecie = builder.nombreEspecie;
        this.nombreRaza = builder.nombreRaza;
        this.nombreDueno = builder.nombreDueno;
    }

    // Calcula los anos y meses de edad a partir de la fecha de nacimiento
    public String getEdadCalculada() {
        if (fechaNacimiento == null) return "Desconocida";
        Period p = Period.between(fechaNacimiento, LocalDate.now());
        if (p.getYears() > 0) {
            return String.format("%d anos, %d meses", p.getYears(), p.getMonths());
        }
        return String.format("%d meses, %d dias", p.getMonths(), p.getDays());
    }

    public static Builder builder() {
        return new Builder();
    }

    // Builder estatico interno
    public static class Builder {
        private Integer id;
        private int duenoId;
        private String nombre;
        private int especieId;
        private int razaId;
        private LocalDate fechaNacimiento;
        private SexoEnum sexo = SexoEnum.MACHO;
        private double pesoActual;
        private String microchip;
        private String tatuaje;
        private String fotoUrl;
        private String alergias;
        private String condicionesPreexistentes;
        private LocalDateTime fechaRegistro = LocalDateTime.now();
        private boolean activo = true;
        private String nombreEspecie;
        private String nombreRaza;
        private String nombreDueno;

        public Builder conId(Integer id) { this.id = id; return this; }
        public Builder conDueno(int duenoId) { this.duenoId = duenoId; return this; }
        public Builder conNombre(String nombre) { this.nombre = nombre; return this; }
        public Builder conEspecie(int especieId) { this.especieId = especieId; return this; }
        public Builder conRaza(int razaId) { this.razaId = razaId; return this; }
        public Builder conFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; return this; }
        public Builder conSexo(SexoEnum sexo) { this.sexo = sexo; return this; }
        public Builder conPeso(double pesoActual) { this.pesoActual = pesoActual; return this; }
        public Builder conMicrochip(String microchip) { this.microchip = microchip; return this; }
        public Builder conTatuaje(String tatuaje) { this.tatuaje = tatuaje; return this; }
        public Builder conFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; return this; }
        public Builder conAlergias(String alergias) { this.alergias = alergias; return this; }
        public Builder conCondiciones(String condiciones) { this.condicionesPreexistentes = condiciones; return this; }
        public Builder conFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; return this; }
        public Builder conActivo(boolean activo) { this.activo = activo; return this; }
        public Builder conNombreEspecie(String nombreEspecie) { this.nombreEspecie = nombreEspecie; return this; }
        public Builder conNombreRaza(String nombreRaza) { this.nombreRaza = nombreRaza; return this; }
        public Builder conNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; return this; }

        public Mascota build() {
            return new Mascota(this);
        }
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getDuenoId() { return duenoId; }
    public void setDuenoId(int duenoId) { this.duenoId = duenoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getEspecieId() { return especieId; }
    public void setEspecieId(int especieId) { this.especieId = especieId; }

    public int getRazaId() { return razaId; }
    public void setRazaId(int razaId) { this.razaId = razaId; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public SexoEnum getSexo() { return sexo; }
    public void setSexo(SexoEnum sexo) { this.sexo = sexo; }

    public double getPesoActual() { return pesoActual; }
    public void setPesoActual(double pesoActual) { this.pesoActual = pesoActual; }

    public String getMicrochip() { return microchip; }
    public void setMicrochip(String microchip) { this.microchip = microchip; }

    public String getTatuaje() { return tatuaje; }
    public void setTatuaje(String tatuaje) { this.tatuaje = tatuaje; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getCondicionesPreexistentes() { return condicionesPreexistentes; }
    public void setCondicionesPreexistentes(String condicionesPreexistentes) { this.condicionesPreexistentes = condicionesPreexistentes; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreEspecie() { return nombreEspecie; }
    public void setNombreEspecie(String nombreEspecie) { this.nombreEspecie = nombreEspecie; }

    public String getNombreRaza() { return nombreRaza; }
    public void setNombreRaza(String nombreRaza) { this.nombreRaza = nombreRaza; }

    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String nombreDueno) { this.nombreDueno = nombreDueno; }

    @Override
    public String toString() {
        return String.format("Mascota[id=%d, nombre='%s', especie=%s, raza=%s, duenoId=%d, peso=%.2f kg, activo=%b]",
                id, nombre, nombreEspecie != null ? nombreEspecie : especieId,
                nombreRaza != null ? nombreRaza : razaId, duenoId, pesoActual, activo);
    }
}
