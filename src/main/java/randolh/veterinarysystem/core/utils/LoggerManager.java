package randolh.veterinarysystem.core.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Bitacora de registro y auditoria en app_veterinaria.log
public final class LoggerManager {

    private static final String ARCHIVO_LOG = "app_veterinaria.log";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object BLOQUEO = new Object();

    private LoggerManager() {
    }

    // Registra mensaje informativo
    public static void info(String modulo, String mensaje) {
        escribir("INFO", modulo, mensaje, null);
    }

    // Registra advertencia
    public static void warning(String modulo, String mensaje) {
        escribir("WARN", modulo, mensaje, null);
    }

    // Registra error con excepcion
    public static void error(String modulo, String mensaje, Throwable throwable) {
        escribir("ERROR", modulo, mensaje, throwable);
    }

    private static void escribir(String nivel, String modulo, String mensaje, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATO_FECHA);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] [%s] [%s] - %s%n", timestamp, nivel, modulo, mensaje));

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                throwable.printStackTrace(pw);
            }
            sb.append(sw.toString());
        }

        synchronized (BLOQUEO) {
            try (FileWriter fw = new FileWriter(ARCHIVO_LOG, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.print(sb.toString());
            } catch (IOException e) {
                System.err.println("No se pudo escribir en el log (" + ARCHIVO_LOG + "): " + e.getMessage());
            }
        }
    }
}
