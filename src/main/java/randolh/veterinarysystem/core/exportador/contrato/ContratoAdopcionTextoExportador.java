package randolh.veterinarysystem.core.exportador.contrato;

import randolh.veterinarysystem.core.exportador.ExportadorDocumento;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Estrategia de exportacion de contratos de adopcion a texto plano estructurado
public class ContratoAdopcionTextoExportador implements ExportadorDocumento<ContratoAdopcionExportData> {

    private static final String MODULO = "ContratoAdopcionTextoExportador";
    private static final String CARPETA_CONTRATOS = "exportaciones/contratos";

    public ContratoAdopcionTextoExportador() {
        new File(CARPETA_CONTRATOS).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.TEXTO;
    }

    @Override
    public String exportar(ContratoAdopcionExportData datos) {
        String nombreArchivo = "contrato_adopcion_ADOP-" + datos.getAdopcionId() + ".txt";
        File archivo = new File(CARPETA_CONTRATOS, nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("================================================================================\n");
            writer.write(centrarTexto(datos.getClinicaNombre(), 80) + "\n");
            writer.write(centrarTexto("NIT: " + datos.getClinicaNit() + " | Tel: " + datos.getClinicaTel(), 80) + "\n");
            writer.write(centrarTexto(datos.getClinicaDir(), 80) + "\n");
            writer.write("================================================================================\n");
            writer.write(centrarTexto("CONTRATO LEGAL DE ADOPCION RESPONSABLE Y TUTELA ANIMAL", 80) + "\n");
            writer.write(String.format(" CONTRATO NRO: ADOP-%05d                            FECHA: %s\n",
                    datos.getAdopcionId(), datos.getFechaAdopcion()));
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" I. DATOS DEL ADOPTANTE / TUTOR LEGAL:\n");
            writer.write(String.format(" Nombre Completo:     %-32s Documento: %s\n",
                    datos.getAdoptanteNombre(), datos.getAdoptanteDoc()));
            writer.write(String.format(" Direccion Domicilio: %-32s Telefono:  %s\n",
                    datos.getAdoptanteDir() != null ? datos.getAdoptanteDir() : "-", datos.getAdoptanteTel()));
            writer.write(String.format(" Correo Electronico:  %s\n",
                    datos.getAdoptanteEmail() != null ? datos.getAdoptanteEmail() : "-"));
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" II. DATOS DEL ANIMAL ADOPTADO:\n");
            writer.write(String.format(" Nombre:       %-35s Especie: %s\n",
                    datos.getMascotaNombre(), datos.getEspecie()));
            writer.write(String.format(" Raza:         %-35s Sexo:    %s\n",
                    datos.getRaza() != null ? datos.getRaza() : "Mestizo",
                    datos.getSexo() != null ? datos.getSexo() : "-"));
            writer.write(String.format(" Edad Estim.:  %-35s Temperamento: %s\n",
                    datos.getEdad(), datos.getTemperamento() != null ? datos.getTemperamento() : "-"));
            if (datos.getHistoria() != null && !datos.getHistoria().isEmpty()) {
                writer.write(" Antecedentes: " + datos.getHistoria() + "\n");
            }
            if (datos.getNecesidades() != null && !datos.getNecesidades().isEmpty()) {
                writer.write(" Cuidados Esp: " + datos.getNecesidades() + "\n");
            }
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(" III. CLAUSULAS DE COMPROMISO Y BIENESTAR ANIMAL:\n");
            writer.write(" 1. BIENESTAR Y SALUD: El adoptante se compromete a proveer alimentacion balanceada,\n" +
                         "    atencion veterinaria preventiva, cuadro de vacunas y desparasitacion al dia.\n");
            writer.write(" 2. TENENCIA RESPONSABLE: Queda terminantemente prohibido mantener al animal atado,\n" +
                         "    en azoteas o a la intemperie. El animal debe convivir como miembro del hogar.\n");
            writer.write(" 3. NO REPRODUCCION NI COMERCIO: Queda prohibida la reproduccion comercial o cruza.\n" +
                         "    El animal debe ser esterilizado segun protocolo clinico veterinario.\n");
            writer.write(" 4. PROHIBICION DE ABANDONO: Bajo ninguna circunstancia se abandonara o cedera a\n" +
                         "    terceros. En caso de fuerza mayor, debera restituirse a la institucion.\n");
            writer.write(" 5. SEGUIMIENTO: El adoptante autoriza visitas periodicas del comite veterinario.\n" +
                         "    Fecha programada de primer seguimiento post-adopcion: " +
                         (datos.getFechaSeguimiento() != null ? datos.getFechaSeguimiento() : "En 30 dias") + "\n");
            if (datos.getCondicionesEspeciales() != null && !datos.getCondicionesEspeciales().isEmpty()) {
                writer.write(" 6. CONDICIONES PARTICULARES ACORDADAS: " + datos.getCondicionesEspeciales() + "\n");
            }
            writer.write("--------------------------------------------------------------------------------\n\n\n");
            writer.write("   ___________________________                 ___________________________\n");
            writer.write("        FIRMA DEL ADOPTANTE                         DIRECCION MEDICA\n");
            writer.write("   Doc: " + datos.getAdoptanteDoc() + "                Clinica " + datos.getClinicaNombre() + "\n");
            writer.write("================================================================================\n");

            LoggerManager.info(MODULO, "Contrato de adopcion TXT generado en: " + archivo.getAbsolutePath());
            return archivo.getAbsolutePath();

        } catch (IOException e) {
            LoggerManager.error(MODULO, "Error al generar contrato de adopcion de texto", e);
            return null;
        }
    }

    private String centrarTexto(String texto, int ancho) {
        if (texto == null) return "";
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int espacios = (ancho - texto.length()) / 2;
        return " ".repeat(espacios) + texto;
    }
}

