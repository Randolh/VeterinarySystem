package randolh.veterinarysystem.core.exportador.carnet;

import randolh.veterinarysystem.consulta.ProcedimientoEspecial;
import randolh.veterinarysystem.core.exportador.ExportadorDocumento;
import randolh.veterinarysystem.core.exportador.FormatoExportacion;
import randolh.veterinarysystem.mascota.MascotaDAO.VacunaHistorialItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

// Estrategia de exportacion de carnet de salud a texto plano estructurado
public class CarnetSaludTextoExportador implements ExportadorDocumento<CarnetSaludExportData> {

    private static final String CARPETA_CARNETS = "exportaciones/carnets";

    public CarnetSaludTextoExportador() {
        new File(CARPETA_CARNETS).mkdirs();
    }

    @Override
    public FormatoExportacion getFormato() {
        return FormatoExportacion.TEXTO;
    }

    @Override
    public String exportar(CarnetSaludExportData datos) {
        String nombreArchivo = "carnet_mascota_" + datos.getMascotaId() + ".txt";
        File archivo = new File(CARPETA_CARNETS, nombreArchivo);

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            pw.println("================================================================================");
            pw.println("                   " + datos.getClinicaNombre().toUpperCase());
            pw.println("                  NIT: " + datos.getClinicaNit() + " - Tel: " + datos.getClinicaTel());
            pw.println("                       " + datos.getClinicaDir());
            pw.println("================================================================================");
            pw.println("                     CARNET DE SALUD Y CONTROL CLINICO");
            pw.println("--------------------------------------------------------------------------------");
            pw.printf(" Paciente ID   : #%d%n", datos.getMascotaId());
            pw.printf(" Nombre        : %s%n", datos.getMascotaNombre());
            pw.printf(" Especie / Raza: %s / %s%n", datos.getEspecie(), datos.getRaza());
            pw.printf(" Sexo          : %s%n", datos.getSexo());
            pw.printf(" Nacimiento    : %s (Edad: %s)%n", datos.getFechaNacimiento(), datos.getEdadCalculada());
            pw.printf(" Peso Actual   : %.2f kg%n", datos.getPesoActual());
            pw.printf(" Microchip     : %s%n", datos.getMicrochip() != null ? datos.getMicrochip() : "No implantado");
            pw.printf(" Tatuaje       : %s%n", datos.getTatuaje() != null ? datos.getTatuaje() : "No registrado");
            pw.println("--------------------------------------------------------------------------------");
            pw.println(" INFORMACION DEL PROPIETARIO:");
            pw.printf(" Responsable   : %s (ID Cliente: #%d)%n", datos.getDuenoNombre(), datos.getDuenoId());
            pw.println("--------------------------------------------------------------------------------");
            pw.println(" ANTECEDENTES CLINICOS:");
            pw.printf(" Alergias      : %s%n", datos.getAlergias() != null ? datos.getAlergias() : "Ninguna reportada");
            pw.printf(" Condiciones   : %s%n", datos.getCondicionesPreexistentes() != null ? datos.getCondicionesPreexistentes() : "Sin condiciones preexistentes");
            pw.println("--------------------------------------------------------------------------------");

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // 1. Historial de Vacunacion y Biologicos
            pw.println(" HISTORIAL DE VACUNACION Y BIOLOGICOS:");
            pw.println("--------------------------------------------------------------------------------");
            if (datos.getVacunas().isEmpty()) {
                pw.println("  (No se registran vacunas aplicadas para esta mascota)");
            } else {
                String sepVacunas = "+------------+------------------------------+--------------+-------------+----------------------+";
                pw.println(sepVacunas);
                pw.printf("| %-10s | %-28s | %-12s | %-11s | %-20s |%n",
                        "Fecha Apl.", "Vacuna / Biologico", "Lote", "Prox. Dosis", "Profesional");
                pw.println(sepVacunas);
                for (VacunaHistorialItem v : datos.getVacunas()) {
                    String fApl = v.getFechaAplicacion() != null ? v.getFechaAplicacion().format(dtfFecha) : "N/D";
                    String fProx = v.getProximaDosis() != null ? v.getProximaDosis().format(dtfFecha) : "N/D";
                    pw.printf("| %-10s | %-28s | %-12s | %-11s | %-20s |%n",
                            fApl,
                            truncar(v.getNombreVacuna(), 28),
                            truncar(v.getLote(), 12),
                            fProx,
                            truncar(v.getVeterinario(), 20));
                }
                pw.println(sepVacunas);
            }
            pw.println("--------------------------------------------------------------------------------");

            // 2. Historial de Procedimientos Quirurgicos y Cirugias
            pw.println(" HISTORIAL DE CIRUGIAS Y PROCEDIMIENTOS ESPECIALES:");
            pw.println("--------------------------------------------------------------------------------");
            if (datos.getCirugias().isEmpty()) {
                pw.println("  (No se registran procedimientos quirurgicos para esta mascota)");
            } else {
                String sepCirugias = "+------------------+----------------------------------+--------------+----------------------+";
                pw.println(sepCirugias);
                pw.printf("| %-16s | %-32s | %-12s | %-20s |%n",
                        "Fecha / Hora", "Procedimiento", "Estado", "Cirujano");
                pw.println(sepCirugias);
                for (ProcedimientoEspecial p : datos.getCirugias()) {
                    String fProc = p.getFechaHora() != null ? p.getFechaHora().format(dtfHora) : "N/D";
                    pw.printf("| %-16s | %-32s | %-12s | %-20s |%n",
                            fProc,
                            truncar(p.getNombreProcedimiento(), 32),
                            p.getEstado() != null ? p.getEstado().getValorDb() : "N/D",
                            truncar(p.getNombreVeterinario() != null ? p.getNombreVeterinario() : "No asignado", 20));
                    if (p.getDetalleProcedimiento() != null && !p.getDetalleProcedimiento().trim().isEmpty()) {
                        pw.println("   Detalle       : " + p.getDetalleProcedimiento());
                    }
                    if (p.getSeguimientoPostoperatorio() != null && !p.getSeguimientoPostoperatorio().trim().isEmpty()) {
                        pw.println("   Postoperatorio: " + p.getSeguimientoPostoperatorio());
                    }
                    if (p.getProximoControl() != null) {
                        pw.println("   Proximo Control: " + p.getProximoControl().format(dtfFecha));
                    }
                }
                pw.println(sepCirugias);
            }
            pw.println("--------------------------------------------------------------------------------");
            pw.printf(" Fecha Emision : %s%n", datos.getFechaEmision().format(dtfFecha));
            pw.println();
            pw.println("               _____________________________________________");
            pw.println("                         Firma del Medico Veterinario");
            pw.println("================================================================================");

            return archivo.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el archivo de carnet en texto: " + e.getMessage(), e);
        }
    }

    private String truncar(String valor, int max) {
        if (valor == null) return "";
        return valor.length() <= max ? valor : valor.substring(0, max - 3) + "...";
    }
}

