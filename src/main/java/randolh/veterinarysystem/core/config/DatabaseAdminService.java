package randolh.veterinarysystem.core.config;

import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Servicio de administracion, diagnostico y orquestacion SQL
public class DatabaseAdminService {

    private static final String MODULO = "DatabaseAdminService";

    private static final String[] ARCHIVOS_SCRIPTS = {
            "01_ddl_schema.sql",
            "02_programacion_bd.sql",
            "03_dml_seeds.sql"
    };

    // DTO de resultado de conexion
    public static class DiagnosticoConexion {
        private final boolean exitoso;
        private final String mensaje;
        private final long latenciaMs;
        private final String versionMySQL;
        private final boolean baseDatosExiste;
        private final int cantidadTablas;

        public DiagnosticoConexion(boolean exitoso, String mensaje, long latenciaMs,
                                  String versionMySQL, boolean baseDatosExiste, int cantidadTablas) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.latenciaMs = latenciaMs;
            this.versionMySQL = versionMySQL;
            this.baseDatosExiste = baseDatosExiste;
            this.cantidadTablas = cantidadTablas;
        }

        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public long getLatenciaMs() { return latenciaMs; }
        public String getVersionMySQL() { return versionMySQL; }
        public boolean isBaseDatosExiste() { return baseDatosExiste; }
        public int getCantidadTablas() { return cantidadTablas; }
    }

    // DTO de inicializacion de base de datos
    public static class ResultadoInicializacion {
        private final boolean exitoso;
        private final int totalSentencias;
        private final long tiempoMs;
        private final String mensaje;

        public ResultadoInicializacion(boolean exitoso, int totalSentencias, long tiempoMs, String mensaje) {
            this.exitoso = exitoso;
            this.totalSentencias = totalSentencias;
            this.tiempoMs = tiempoMs;
            this.mensaje = mensaje;
        }

        public boolean isExitoso() { return exitoso; }
        public int getTotalSentencias() { return totalSentencias; }
        public long getTiempoMs() { return tiempoMs; }
        public String getMensaje() { return mensaje; }
    }

    // Diagnostico de conexion con MySQL
    public DiagnosticoConexion probarConexion() {
        ConexionBD config = ConexionBD.getInstancia();
        long inicio = System.currentTimeMillis();

        String urlServidor = String.format("jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                config.getHost(), config.getPort());

        try (Connection conn = DriverManager.getConnection(urlServidor, config.getUser(), config.getPassword())) {
            long latencia = System.currentTimeMillis() - inicio;
            DatabaseMetaData meta = conn.getMetaData();
            String version = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();

            boolean bdExiste = false;
            int totalTablas = 0;

            try (ResultSet rs = conn.getMetaData().getCatalogs()) {
                while (rs.next()) {
                    if (config.getDbName().equalsIgnoreCase(rs.getString(1))) {
                        bdExiste = true;
                        break;
                    }
                }
            }

            if (bdExiste) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("USE `" + config.getDbName() + "`");
                    try (ResultSet rsTablas = stmt.executeQuery("SHOW TABLES")) {
                        while (rsTablas.next()) {
                            totalTablas++;
                        }
                    }
                }
            }

            return new DiagnosticoConexion(true, "Conexion establecida correctamente con MySQL.",
                    latencia, version, bdExiste, totalTablas);

        } catch (SQLException e) {
            long latencia = System.currentTimeMillis() - inicio;
            LoggerManager.error(MODULO, "Fallo al probar conexion con servidor MySQL", e);
            return new DiagnosticoConexion(false, e.getMessage(), latencia, "Desconocida", false, 0);
        }
    }

    // Inicializa la base de datos ejecutando DDL, programacion y semillas
    public ResultadoInicializacion inicializarBaseDatos(Consumer<String> logger) {
        Consumer<String> log = logger != null ? logger : s -> {};
        ConexionBD config = ConexionBD.getInstancia();
        long tiempoInicio = System.currentTimeMillis();
        int sentenciasEjecutadas = 0;

        log.accept("Iniciando orquestacion de base de datos...");
        LoggerManager.info(MODULO, "Iniciando despliegue de base de datos en " + config.getHost() + ":" + config.getPort());

        String urlServidor = String.format("jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true",
                config.getHost(), config.getPort());

        try (Connection conn = DriverManager.getConnection(urlServidor, config.getUser(), config.getPassword());
             Statement stmt = conn.createStatement()) {

            for (String script : ARCHIVOS_SCRIPTS) {
                log.accept("Cargando script: " + script);
                List<String> sentencias = cargarYParsearScript(script);

                log.accept(String.format("Ejecutando %d sentencias de %s...", sentencias.size(), script));

                for (int i = 0; i < sentencias.size(); i++) {
                    String sql = sentencias.get(i);
                    try {
                        stmt.execute(sql);
                        sentenciasEjecutadas++;
                    } catch (SQLException e) {
                        String errorMsg = String.format("Error en sentencia #%d de [%s]: %s\nSQL: %s",
                                i + 1, script, e.getMessage(), truncar(sql, 150));
                        LoggerManager.error(MODULO, errorMsg, e);
                        log.accept("[FALLO] " + errorMsg);
                        throw new BaseDatosException(errorMsg, e);
                    }
                }
                log.accept("[OK] Script completado: " + script);
            }

            config.cargarPropiedadesCascada();

            long totalTiempo = System.currentTimeMillis() - tiempoInicio;
            String msgExito = String.format("Base de datos inicializada exitosamente (%d sentencias en %d ms).",
                    sentenciasEjecutadas, totalTiempo);
            log.accept(msgExito);
            LoggerManager.info(MODULO, msgExito);

            return new ResultadoInicializacion(true, sentenciasEjecutadas, totalTiempo, msgExito);

        } catch (SQLException e) {
            long totalTiempo = System.currentTimeMillis() - tiempoInicio;
            String msgError = "Fallo critico al conectar con servidor: " + e.getMessage();
            LoggerManager.error(MODULO, msgError, e);
            log.accept("[ERROR] " + msgError);
            return new ResultadoInicializacion(false, sentenciasEjecutadas, totalTiempo, msgError);
        } catch (Exception e) {
            long totalTiempo = System.currentTimeMillis() - tiempoInicio;
            String msgError = "Error inesperado en despliegue: " + e.getMessage();
            LoggerManager.error(MODULO, msgError, e);
            log.accept("[ERROR] " + msgError);
            return new ResultadoInicializacion(false, sentenciasEjecutadas, totalTiempo, msgError);
        }
    }

    // Carga script con prioridad classpath embebido y luego sistema de archivos
    public List<String> cargarYParsearScript(String nombreArchivo) throws IOException {
        InputStream is = abrirStreamScript(nombreArchivo);
        if (is == null) {
            throw new IOException("No se encontro el script SQL '" + nombreArchivo + "' en classpath ni en sistema de archivos.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return parsearSentenciasSQL(reader);
        }
    }

    private InputStream abrirStreamScript(String nombreArchivo) {
        // Prioridad 1: Classpath
        InputStream is = getClass().getClassLoader().getResourceAsStream("scripts_sql/" + nombreArchivo);
        if (is != null) {
            LoggerManager.info(MODULO, "Cargando script desde classpath: scripts_sql/" + nombreArchivo);
            return is;
        }

        is = getClass().getClassLoader().getResourceAsStream("/scripts_sql/" + nombreArchivo);
        if (is != null) {
            LoggerManager.info(MODULO, "Cargando script desde classpath raiz: /scripts_sql/" + nombreArchivo);
            return is;
        }

        // Prioridad 2: Sistema de archivos
        String[] rutasPosibles = {
                "./scripts_sql/" + nombreArchivo,
                "./src/main/resources/scripts_sql/" + nombreArchivo,
                "../scripts_sql/" + nombreArchivo,
                "VeterinarySystem/scripts_sql/" + nombreArchivo,
                "VeterinarySystem/src/main/resources/scripts_sql/" + nombreArchivo
        };

        for (String ruta : rutasPosibles) {
            File f = new File(ruta);
            if (f.exists() && f.isFile()) {
                try {
                    LoggerManager.info(MODULO, "Cargando script desde sistema de archivos: " + f.getAbsolutePath());
                    return new FileInputStream(f);
                } catch (IOException ignored) {
                }
            }
        }

        return null;
    }

    // Parser que respeta directivas DELIMITER y bloques de triggers/procedimientos
    public List<String> parsearSentenciasSQL(BufferedReader reader) throws IOException {
        List<String> sentencias = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        String delimitadorActual = ";";
        boolean enComentarioBloque = false;

        String linea;
        while ((linea = reader.readLine()) != null) {
            String lineaTrim = linea.trim();

            if (enComentarioBloque) {
                if (lineaTrim.contains("*/")) {
                    int finIdx = lineaTrim.indexOf("*/");
                    lineaTrim = lineaTrim.substring(finIdx + 2).trim();
                    enComentarioBloque = false;
                } else {
                    continue;
                }
            }

            if (lineaTrim.startsWith("/*") && !lineaTrim.startsWith("/*!")) {
                if (lineaTrim.contains("*/")) {
                    int finIdx = lineaTrim.indexOf("*/");
                    lineaTrim = lineaTrim.substring(finIdx + 2).trim();
                } else {
                    enComentarioBloque = true;
                    continue;
                }
            }

            if (lineaTrim.isEmpty() || lineaTrim.startsWith("--") || lineaTrim.startsWith("#")) {
                continue;
            }

            if (lineaTrim.toUpperCase().startsWith("DELIMITER")) {
                String nuevoDelim = lineaTrim.substring(9).trim();
                if (!nuevoDelim.isEmpty()) {
                    delimitadorActual = nuevoDelim;
                }
                continue;
            }

            buffer.append(linea).append("\n");

            String bufferStr = buffer.toString().trim();
            if (bufferStr.endsWith(delimitadorActual)) {
                String sqlLimpio = bufferStr.substring(0, bufferStr.length() - delimitadorActual.length()).trim();
                if (!sqlLimpio.isEmpty()) {
                    sentencias.add(sqlLimpio);
                }
                buffer.setLength(0);
            }
        }

        String remanente = buffer.toString().trim();
        if (!remanente.isEmpty()) {
            if (remanente.endsWith(delimitadorActual)) {
                remanente = remanente.substring(0, remanente.length() - delimitadorActual.length()).trim();
            }
            if (!remanente.isEmpty()) {
                sentencias.add(remanente);
            }
        }

        return sentencias;
    }

    private String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        if (texto.length() <= maxLen) return texto;
        return texto.substring(0, maxLen) + "...";
    }
}
