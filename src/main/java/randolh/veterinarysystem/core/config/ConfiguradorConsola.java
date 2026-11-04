package randolh.veterinarysystem.core.config;

import randolh.veterinarysystem.core.exception.OperacionCanceladaException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

// Asistente interactivo en consola para diagnostico y configuracion JDBC
public class ConfiguradorConsola {

    private static final String MODULO = "ConfiguradorConsola";
    private final DatabaseAdminService adminService;

    public ConfiguradorConsola() {
        this.adminService = new DatabaseAdminService();
    }

    // Muestra el menu interactivo de administracion de base de datos
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("PANEL DE DIAGNOSTICO Y CONFIGURACION DE BASE DE DATOS");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Probar conectividad con MySQL y verificar esquema actual" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Ver parametros de conexion e informacion institucional" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Reconfigurar credenciales de acceso a MySQL (persistencia local)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Recrear / Inicializar base de datos completa desde scripts SQL" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu principal" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            try {
                menu.run();
                int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-4]: ", 0, 4);

                switch (opcion) {
                    case 1 -> probarConectividad();
                    case 2 -> mostrarParametrosActuales();
                    case 3 -> reconfigurarCredenciales();
                    case 4 -> ejecutarReconstruccionBaseDatos();
                    case 0 -> {
                        ConsolaUtils.imprimirInfo("Regresando al menu anterior...");
                        continuar = false;
                    }
                    default -> ConsolaUtils.imprimirError("Opcion no reconocida.");
                }

                if (continuar) {
                    ConsolaUtils.pausar();
                }
            } catch (OperacionCanceladaException e) {
                ConsolaUtils.imprimirAdvertencia(e.getMessage() + " Regresando al menu principal...");
                continuar = false;
            }
        }
    }

    private void probarConectividad() {
        ConsolaUtils.imprimirSubtitulo("Diagnostico de Enlace MySQL");
        ConsolaUtils.imprimirInfo("Probando comunicacion con servidor MySQL...");

        DatabaseAdminService.DiagnosticoConexion diag = adminService.probarConexion();
        ConexionBD config = ConexionBD.getInstancia();

        System.out.println();
        System.out.printf("  Servidor destino    : %s:%d%n", config.getHost(), config.getPort());
        System.out.printf("  Usuario autenticado : %s%n", config.getUser());
        System.out.printf("  Base de datos       : %s%n", config.getDbName());
        System.out.printf("  Latencia de respuesta: %d ms%n", diag.getLatenciaMs());
        System.out.printf("  Version del motor   : %s%n", diag.getVersionMySQL());
        System.out.printf("  Esquema detectado   : %s%n", diag.isBaseDatosExiste() ? "EXISTE" : "NO ENCONTRADO");
        System.out.printf("  Tablas activas      : %d tablas%n", diag.getCantidadTablas());
        System.out.println();

        if (diag.isExitoso() && diag.isBaseDatosExiste()) {
            ConsolaUtils.imprimirExito("Conectividad comprobada. La base de datos se encuentra lista y operativa.");
        } else if (diag.isExitoso() && !diag.isBaseDatosExiste()) {
            ConsolaUtils.imprimirAdvertencia("El servidor MySQL responde, pero la base de datos '" + config.getDbName()
                    + "' aun no ha sido creada. Ejecute la opcion 4 para inicializarla.");
        } else {
            ConsolaUtils.imprimirError("No se pudo conectar a MySQL: " + diag.getMensaje());
            ConsolaUtils.imprimirAdvertencia("Verifique que el servicio mysqld este iniciado y que las credenciales sean validas.");
        }
    }

    private void mostrarParametrosActuales() {
        ConexionBD config = ConexionBD.getInstancia();
        ConsolaUtils.imprimirSubtitulo("Parametros de Configuracion del Sistema");

        System.out.println(ConsolaUtils.ANSI_CYAN + "[Conexion JDBC]" + ConsolaUtils.ANSI_RESET);
        System.out.println("  Host        : " + config.getHost());
        System.out.println("  Puerto      : " + config.getPort());
        System.out.println("  Base Datos  : " + config.getDbName());
        System.out.println("  Usuario     : " + config.getUser());
        System.out.println("  Contrasena  : " + (config.getPassword().isEmpty() ? "(sin contrasena)" : "********"));
        System.out.println("  URL JDBC    : " + config.getJdbcUrl());

        System.out.println();
        System.out.println(ConsolaUtils.ANSI_CYAN + "[Datos Institucionales de la Clinica]" + ConsolaUtils.ANSI_RESET);
        System.out.println("  Nombre      : " + config.getNombreClinica());
        System.out.println("  NIT/RUC     : " + config.getNitClinica());
        System.out.println("  Telefono    : " + config.getTelefonoClinica());
        System.out.println("  Direccion   : " + config.getDireccionClinica());
        System.out.printf("  IVA Aplicable: %.2f%%%n", config.getIvaPorcentaje());
    }

    private void reconfigurarCredenciales() {
        ConexionBD config = ConexionBD.getInstancia();
        ConsolaUtils.imprimirSubtitulo("Reconfigurar Credenciales MySQL");
        ConsolaUtils.imprimirInfo("Presione [Enter] en cualquier campo para mantener el valor actual mostrado entre corchetes.");

        try {
            String nuevoHost = ConsolaUtils.leerTextoOpcional("Host de MySQL", config.getHost());
            int nuevoPuerto = ConsolaUtils.leerEnteroEnRango("Puerto de MySQL [" + config.getPort() + "]: ", 1, 65535);
            String nuevaBd = ConsolaUtils.leerTextoOpcional("Nombre de la Base de Datos", config.getDbName());
            String nuevoUser = ConsolaUtils.leerTextoOpcional("Usuario de MySQL", config.getUser());
            String nuevoPass = ConsolaUtils.leerTextoOpcional("Contrasena de MySQL", config.getPassword());

            boolean guardar = ConsolaUtils.leerBooleano("Desea guardar estos cambios en 'database.properties'?");
            if (guardar) {
                persistirArchivoPropiedades(nuevoHost, nuevoPuerto, nuevaBd, nuevoUser, nuevoPass, config);
                config.cargarPropiedadesCascada();
                ConsolaUtils.imprimirExito("Configuracion actualizada y recargada en memoria exitosamente.");
                probarConectividad();
            } else {
                ConsolaUtils.imprimirAdvertencia("Cambios descartados.");
            }
        } catch (OperacionCanceladaException e) {
            ConsolaUtils.imprimirAdvertencia("Reconfiguracion de credenciales cancelada. No se aplicaron cambios.");
        }
    }

    private void persistirArchivoPropiedades(String host, int port, String dbName,
                                            String user, String pass, ConexionBD actual) {
        File archivo = new File("database.properties");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            writer.println("# Parametros MySQL");
            writer.println("db.host=" + host);
            writer.println("db.port=" + port);
            writer.println("db.name=" + dbName);
            writer.println("db.user=" + user);
            writer.println("db.password=" + pass);
            writer.println();
            writer.println("# Parametros Adicionales JDBC");
            writer.println("db.useSSL=false");
            writer.println("db.allowPublicKeyRetrieval=true");
            writer.println("db.serverTimezone=UTC");
            writer.println();
            writer.println("# Parametros Institucionales");
            writer.println("clinica.nombre=" + actual.getNombreClinica());
            writer.println("clinica.nit=" + actual.getNitClinica());
            writer.println("clinica.telefono=" + actual.getTelefonoClinica());
            writer.println("clinica.direccion=" + actual.getDireccionClinica());
            writer.println("clinica.iva.porcentaje=" + actual.getIvaPorcentaje());

            LoggerManager.info(MODULO, "Archivo database.properties persistido exitosamente en " + archivo.getAbsolutePath());
        } catch (IOException e) {
            ConsolaUtils.imprimirError("Error al escribir 'database.properties': " + e.getMessage());
            LoggerManager.error(MODULO, "Fallo al escribir database.properties", e);
        }
    }

    private void ejecutarReconstruccionBaseDatos() {
        ConsolaUtils.imprimirSubtitulo("Reconstruccion Integral de Base de Datos");
        System.out.println(ConsolaUtils.COLOR_ADVERTENCIA + "ATENCION: Esta operacion borrara y recreara la base de datos");
        System.out.println("ejecutando secuencialmente el esquema DDL, triggers/SP y datos semilla DML." + ConsolaUtils.ANSI_RESET);

        try {
            boolean confirmacion = ConsolaUtils.leerBooleano("Esta seguro de continuar?");
            if (!confirmacion) {
                ConsolaUtils.imprimirAdvertencia("Operacion cancelada por el usuario.");
                return;
            }

            System.out.println();
            DatabaseAdminService.ResultadoInicializacion resultado = adminService.inicializarBaseDatos(mensaje -> {
                if (mensaje.startsWith("[OK]")) {
                    ConsolaUtils.imprimirExito(mensaje.substring(4).trim());
                } else if (mensaje.startsWith("[ERROR]") || mensaje.startsWith("[FALLO]")) {
                    ConsolaUtils.imprimirError(mensaje);
                } else {
                    ConsolaUtils.imprimirInfo(mensaje);
                }
            });

            System.out.println();
            if (resultado.isExitoso()) {
                ConsolaUtils.imprimirExito(resultado.getMensaje());
            } else {
                ConsolaUtils.imprimirError("Fallo en la inicializacion: " + resultado.getMensaje());
            }
        } catch (OperacionCanceladaException e) {
            ConsolaUtils.imprimirAdvertencia("Inicializacion de base de datos cancelada por el usuario.");
        }
    }
}
