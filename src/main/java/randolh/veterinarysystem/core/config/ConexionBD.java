package randolh.veterinarysystem.core.config;

import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Gestor Singleton de conexion a MySQL con reconexion activa
public final class ConexionBD {

    private static final String MODULO = "ConexionBD";
    private static final String ARCHIVO_PROPIEDADES_LOCAL = "database.properties";
    private static final String ARCHIVO_PROPIEDADES_CLASSPATH = "database.properties";
    private static final String ARCHIVO_EJEMPLO_CLASSPATH = "database.properties.example";

    private static ConexionBD instancia;
    private Connection conexion;

    // Propiedades en memoria
    private final Properties propiedades = new Properties();

    // Parametros de base de datos
    private String host;
    private int port;
    private String dbName;
    private String user;
    private String password;
    private String useSSL;
    private String allowPublicKeyRetrieval;
    private String serverTimezone;

    // Parametros institucionales
    private String clinicaNombre;
    private String clinicaNit;
    private String clinicaTelefono;
    private String clinicaDireccion;
    private double clinicaIvaPorcentaje;

    private ConexionBD() {
        cargarPropiedadesCascada();
    }

    // Instancia unica Singleton
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    // Carga propiedades en cascada (local -> classpath -> env -> default)
    public synchronized void cargarPropiedadesCascada() {
        propiedades.clear();
        boolean cargado = false;

        // 1. Archivo local
        File archivoLocal = new File(ARCHIVO_PROPIEDADES_LOCAL);
        if (archivoLocal.exists() && archivoLocal.isFile()) {
            try (InputStream is = new FileInputStream(archivoLocal)) {
                propiedades.load(is);
                LoggerManager.info(MODULO, "Configuracion cargada desde archivo local: " + archivoLocal.getAbsolutePath());
                cargado = true;
            } catch (IOException e) {
                LoggerManager.warning(MODULO, "No se pudo leer archivo local database.properties: " + e.getMessage());
            }
        }

        // 2. Recurso en classpath
        if (!cargado) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(ARCHIVO_PROPIEDADES_CLASSPATH)) {
                if (is != null) {
                    propiedades.load(is);
                    LoggerManager.info(MODULO, "Configuracion cargada desde classpath: " + ARCHIVO_PROPIEDADES_CLASSPATH);
                    cargado = true;
                }
            } catch (IOException e) {
                LoggerManager.warning(MODULO, "No se pudo leer recurso classpath database.properties: " + e.getMessage());
            }
        }

        // Fallback recurso example
        if (!cargado) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(ARCHIVO_EJEMPLO_CLASSPATH)) {
                if (is != null) {
                    propiedades.load(is);
                    LoggerManager.info(MODULO, "Configuracion cargada desde classpath example: " + ARCHIVO_EJEMPLO_CLASSPATH);
                    cargado = true;
                }
            } catch (IOException e) {
                LoggerManager.warning(MODULO, "No se pudo leer recurso classpath database.properties.example: " + e.getMessage());
            }
        }

        // 3. Variables de entorno y 4. Valores por defecto
        this.host = obtenerConfig("db.host", "DB_HOST", "localhost");
        this.port = Integer.parseInt(obtenerConfig("db.port", "DB_PORT", "3306"));
        this.dbName = obtenerConfig("db.name", "DB_NAME", "happy_feet_veterinaria");
        this.user = obtenerConfig("db.user", "DB_USER", "root");
        this.password = obtenerConfig("db.password", "DB_PASSWORD", "");
        this.useSSL = propiedades.getProperty("db.useSSL", "false");
        this.allowPublicKeyRetrieval = propiedades.getProperty("db.allowPublicKeyRetrieval", "true");
        this.serverTimezone = propiedades.getProperty("db.serverTimezone", "UTC");

        this.clinicaNombre = propiedades.getProperty("clinica.nombre", "Clinica Veterinaria Happy Feet");
        this.clinicaNit = propiedades.getProperty("clinica.nit", "900.123.456-7");
        this.clinicaTelefono = propiedades.getProperty("clinica.telefono", "(+57) 300 123 4567");
        this.clinicaDireccion = propiedades.getProperty("clinica.direccion", "Calle 123 #45-67, Bogota");

        try {
            this.clinicaIvaPorcentaje = Double.parseDouble(propiedades.getProperty("clinica.iva.porcentaje", "19.0"));
        } catch (NumberFormatException e) {
            this.clinicaIvaPorcentaje = 19.0;
        }

        cerrarConexion();
    }

    private String obtenerConfig(String clavePropiedad, String variableEntorno, String valorDefecto) {
        String env = System.getenv(variableEntorno);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String prop = propiedades.getProperty(clavePropiedad);
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        return valorDefecto;
    }

    // Obtiene conexion activa con reconexion automatica ante timeouts o caidas
    public synchronized Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
                establecerNuevaConexion();
            }
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Enlace JDBC inactivo o cerrado, intentando restablecer enlace...");
            try {
                establecerNuevaConexion();
            } catch (SQLException ex) {
                LoggerManager.error(MODULO, "Fallo critico al restablecer enlace con MySQL en " + host + ":" + port, ex);
                throw new BaseDatosException("No fue posible conectar con el servidor MySQL (" + host + ":" + port + "): " + ex.getMessage(), ex);
            }
        }
        return conexion;
    }

    private void establecerNuevaConexion() throws SQLException {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignored) {
            }
        }

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s&characterEncoding=UTF-8",
                host, port, dbName, useSSL, allowPublicKeyRetrieval, serverTimezone);

        conexion = DriverManager.getConnection(url, user, password);
        LoggerManager.info(MODULO, "Enlace JDBC establecido exitosamente con: " + url + " [Usuario: " + user + "]");
    }

    // Prueba si la conexion es exitosa sin alterar el estado actual
    public synchronized boolean probarConectividad() {
        try (Connection testConn = DriverManager.getConnection(
                String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s",
                        host, port, dbName, useSSL, allowPublicKeyRetrieval, serverTimezone),
                user, password)) {
            return testConn.isValid(3);
        } catch (SQLException e) {
            LoggerManager.warning(MODULO, "Prueba de conexion fallida: " + e.getMessage());
            return false;
        }
    }

    // Cierra limpiamente la conexion activa
    public synchronized void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    LoggerManager.info(MODULO, "Conexion JDBC cerrada limpiamente.");
                }
            } catch (SQLException e) {
                LoggerManager.error(MODULO, "Error al cerrar la conexion JDBC", e);
            } finally {
                conexion = null;
            }
        }
    }

    // Getters de Base de Datos
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDbName() { return dbName; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
    }

    // Getters Institucionales
    public String getNombreClinica() { return clinicaNombre; }
    public String getNitClinica() { return clinicaNit; }
    public String getTelefonoClinica() { return clinicaTelefono; }
    public String getDireccionClinica() { return clinicaDireccion; }
    public double getIvaPorcentaje() { return clinicaIvaPorcentaje; }
    public Properties getPropiedades() { return (Properties) propiedades.clone(); }
}
