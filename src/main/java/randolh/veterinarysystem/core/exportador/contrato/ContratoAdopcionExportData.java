package randolh.veterinarysystem.core.exportador.contrato;

// Modelo inmutable que encapsula los datos requeridos para exportar un contrato de adopcion
public class ContratoAdopcionExportData {

    private final String clinicaNombre;
    private final String clinicaNit;
    private final String clinicaTel;
    private final String clinicaDir;
    private final int adopcionId;
    private final String fechaAdopcion;
    private final String adoptanteNombre;
    private final String adoptanteDoc;
    private final String adoptanteTel;
    private final String adoptanteDir;
    private final String adoptanteEmail;
    private final String mascotaNombre;
    private final String especie;
    private final String raza;
    private final String sexo;
    private final String edad;
    private final String historia;
    private final String temperamento;
    private final String necesidades;
    private final String condicionesEspeciales;
    private final String fechaSeguimiento;

    public ContratoAdopcionExportData(Builder builder) {
        this.clinicaNombre = builder.clinicaNombre;
        this.clinicaNit = builder.clinicaNit;
        this.clinicaTel = builder.clinicaTel;
        this.clinicaDir = builder.clinicaDir;
        this.adopcionId = builder.adopcionId;
        this.fechaAdopcion = builder.fechaAdopcion;
        this.adoptanteNombre = builder.adoptanteNombre;
        this.adoptanteDoc = builder.adoptanteDoc;
        this.adoptanteTel = builder.adoptanteTel;
        this.adoptanteDir = builder.adoptanteDir;
        this.adoptanteEmail = builder.adoptanteEmail;
        this.mascotaNombre = builder.mascotaNombre;
        this.especie = builder.especie;
        this.raza = builder.raza;
        this.sexo = builder.sexo;
        this.edad = builder.edad;
        this.historia = builder.historia;
        this.temperamento = builder.temperamento;
        this.necesidades = builder.necesidades;
        this.condicionesEspeciales = builder.condicionesEspeciales;
        this.fechaSeguimiento = builder.fechaSeguimiento;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClinicaNombre() { return clinicaNombre; }
    public String getClinicaNit() { return clinicaNit; }
    public String getClinicaTel() { return clinicaTel; }
    public String getClinicaDir() { return clinicaDir; }
    public int getAdopcionId() { return adopcionId; }
    public String getFechaAdopcion() { return fechaAdopcion; }
    public String getAdoptanteNombre() { return adoptanteNombre; }
    public String getAdoptanteDoc() { return adoptanteDoc; }
    public String getAdoptanteTel() { return adoptanteTel; }
    public String getAdoptanteDir() { return adoptanteDir; }
    public String getAdoptanteEmail() { return adoptanteEmail; }
    public String getMascotaNombre() { return mascotaNombre; }
    public String getEspecie() { return especie; }
    public String getRaza() { return raza; }
    public String getSexo() { return sexo; }
    public String getEdad() { return edad; }
    public String getHistoria() { return historia; }
    public String getTemperamento() { return temperamento; }
    public String getNecesidades() { return necesidades; }
    public String getCondicionesEspeciales() { return condicionesEspeciales; }
    public String getFechaSeguimiento() { return fechaSeguimiento; }

    public static class Builder {
        private String clinicaNombre = "";
        private String clinicaNit = "";
        private String clinicaTel = "";
        private String clinicaDir = "";
        private int adopcionId = 0;
        private String fechaAdopcion = "";
        private String adoptanteNombre = "";
        private String adoptanteDoc = "";
        private String adoptanteTel = "";
        private String adoptanteDir = "";
        private String adoptanteEmail = "";
        private String mascotaNombre = "";
        private String especie = "";
        private String raza = "";
        private String sexo = "";
        private String edad = "";
        private String historia = "";
        private String temperamento = "";
        private String necesidades = "";
        private String condicionesEspeciales = "";
        private String fechaSeguimiento = "";

        public Builder conClinica(String nombre, String nit, String tel, String dir) {
            this.clinicaNombre = nombre;
            this.clinicaNit = nit;
            this.clinicaTel = tel;
            this.clinicaDir = dir;
            return this;
        }

        public Builder conAdopcion(int adopcionId, String fechaAdopcion, String fechaSeguimiento) {
            this.adopcionId = adopcionId;
            this.fechaAdopcion = fechaAdopcion;
            this.fechaSeguimiento = fechaSeguimiento;
            return this;
        }

        public Builder conAdoptante(String nombre, String doc, String tel, String dir, String email) {
            this.adoptanteNombre = nombre;
            this.adoptanteDoc = doc;
            this.adoptanteTel = tel;
            this.adoptanteDir = dir;
            this.adoptanteEmail = email;
            return this;
        }

        public Builder conMascota(String nombre, String especie, String raza, String sexo, String edad) {
            this.mascotaNombre = nombre;
            this.especie = especie;
            this.raza = raza;
            this.sexo = sexo;
            this.edad = edad;
            return this;
        }

        public Builder conDetalles(String historia, String temperamento, String necesidades, String condicionesEspeciales) {
            this.historia = historia;
            this.temperamento = temperamento;
            this.necesidades = necesidades;
            this.condicionesEspeciales = condicionesEspeciales;
            return this;
        }

        public ContratoAdopcionExportData build() {
            return new ContratoAdopcionExportData(this);
        }
    }
}

