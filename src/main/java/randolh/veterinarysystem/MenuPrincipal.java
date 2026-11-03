package randolh.veterinarysystem;

import randolh.veterinarysystem.actividad.ActividadView;
import randolh.veterinarysystem.alerta.AlertaController;
import randolh.veterinarysystem.alerta.AlertaView;
import randolh.veterinarysystem.cita.CitaView;
import randolh.veterinarysystem.consulta.ConsultaView;
import randolh.veterinarysystem.core.config.ConfiguradorConsola;
import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.OperacionCanceladaException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.core.utils.LoggerManager;
import randolh.veterinarysystem.dueno.DuenoView;
import randolh.veterinarysystem.facturacion.FacturacionView;
import randolh.veterinarysystem.inventario.InventarioView;
import randolh.veterinarysystem.mascota.MascotaView;
import randolh.veterinarysystem.reporte.ReporteView;
import randolh.veterinarysystem.veterinario.VeterinarioView;

// Menu principal del sistema veterinario Happy Feet con integracion e2e y modo offline tolerante
public class MenuPrincipal {

    private static final String MODULO = "MenuPrincipal";

    // Vistas con inicializacion perezosa (lazy) para arranque ultrarrapido y modo offline robusto
    private MascotaView mascotaView;
    private DuenoView duenoView;
    private VeterinarioView veterinarioView;
    private CitaView citaView;
    private ConsultaView consultaView;
    private InventarioView inventarioView;
    private AlertaView alertaView;
    private AlertaController alertaController;
    private FacturacionView facturacionView;
    private ActividadView actividadView;
    private ReporteView reporteView;
    private ConfiguradorConsola configuradorConsola;

    public MenuPrincipal() {
        // Constructor liviano sin llamadas prematuras a persistencia
    }

    private MascotaView getMascotaView() {
        if (mascotaView == null) mascotaView = new MascotaView();
        return mascotaView;
    }

    private DuenoView getDuenoView() {
        if (duenoView == null) duenoView = new DuenoView();
        return duenoView;
    }

    private VeterinarioView getVeterinarioView() {
        if (veterinarioView == null) veterinarioView = new VeterinarioView();
        return veterinarioView;
    }

    private CitaView getCitaView() {
        if (citaView == null) citaView = new CitaView();
        return citaView;
    }

    private ConsultaView getConsultaView() {
        if (consultaView == null) consultaView = new ConsultaView();
        return consultaView;
    }

    private InventarioView getInventarioView() {
        if (inventarioView == null) inventarioView = new InventarioView();
        return inventarioView;
    }

    private AlertaController getAlertaController() {
        if (alertaController == null) alertaController = new AlertaController();
        return alertaController;
    }

    private AlertaView getAlertaView() {
        if (alertaView == null) alertaView = new AlertaView(getAlertaController());
        return alertaView;
    }

    private FacturacionView getFacturacionView() {
        if (facturacionView == null) facturacionView = new FacturacionView();
        return facturacionView;
    }

    private ActividadView getActividadView() {
        if (actividadView == null) actividadView = new ActividadView();
        return actividadView;
    }

    private ReporteView getReporteView() {
        if (reporteView == null) reporteView = new ReporteView();
        return reporteView;
    }

    private ConfiguradorConsola getConfiguradorConsola() {
        if (configuradorConsola == null) configuradorConsola = new ConfiguradorConsola();
        return configuradorConsola;
    }

    // Inicia el bucle de navegacion principal de la aplicacion
    public void iniciar() {
        Runnable menu = () -> {
            boolean online = ConexionBD.getInstancia().probarConectividad();
            ConsolaUtils.imprimirTitulo("SISTEMA DE GESTION INTEGRAL VETERINARIA HAPPY FEET");

            int alertasPendientes = 0;
            if (online) {
                try {
                    alertasPendientes = getAlertaController().contarAlertasPendientes();
                } catch (Exception ignored) {}
            }

            // Indicador de modo en linea o fuera de linea y notificaciones
            if (online) {
                System.out.print(ConsolaUtils.COLOR_EXITO + "[ESTADO: ONLINE - BASE DE DATOS OPERATIVA]" + ConsolaUtils.ANSI_RESET);
                if (alertasPendientes > 0) {
                    System.out.println(ConsolaUtils.COLOR_ERROR + "  [!] TIENE " + alertasPendientes + " NOTIFICACION(ES) PENDIENTE(S)" + ConsolaUtils.ANSI_RESET);
                } else {
                    System.out.println(ConsolaUtils.COLOR_BORDE + "  [Sin notificaciones pendientes]" + ConsolaUtils.ANSI_RESET);
                }
            } else {
                System.out.println(ConsolaUtils.COLOR_ADVERTENCIA + "[ESTADO: OFFLINE - MODO FUERA DE LINEA / CONEXION BD INACTIVA]" + ConsolaUtils.ANSI_RESET);
            }
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);

            System.out.println(ConsolaUtils.COLOR_INFO + "1.  Modulo de Gestion de Mascotas y Duenos" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2.  Modulo de Personal Veterinario" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3.  Modulo de Servicios Medicos y Citas (Agenda)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4.  Modulo de Consultas Medicas y Quirofano (Cirugias y Deduccion)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5.  Modulo de Inventario, Farmacia y Proveedores" + ConsolaUtils.ANSI_RESET);

            String badgeAlerta = (online && alertasPendientes > 0) ?
                    ConsolaUtils.COLOR_ERROR + " [" + alertasPendientes + " PENDIENTES]" + ConsolaUtils.ANSI_RESET : "";
            System.out.println(ConsolaUtils.COLOR_INFO + "6.  Modulo de Alertas del Sistema y Notificaciones" + badgeAlerta + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "7.  Modulo de Facturacion y Finanzas" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "8.  Modulo de Actividades Especiales (Adopciones, Jornadas y Club)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "9.  Modulo de Reportes Gerenciales e Inteligencia de Negocio" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "10. Asistente y Diagnostico de Base de Datos" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0.  Salir del Sistema" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean ejecutando = true;
        while (ejecutando) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione un modulo [0-10]: ", 0, 10);

            switch (opcion) {
                case 1 -> ejecutarModuloSeguro(this::mostrarSubmenuPacientes);
                case 2 -> ejecutarModuloSeguro(() -> getVeterinarioView().mostrarMenu());
                case 3 -> ejecutarModuloSeguro(() -> getCitaView().mostrarMenu());
                case 4 -> ejecutarModuloSeguro(() -> getConsultaView().mostrarMenu());
                case 5 -> ejecutarModuloSeguro(() -> getInventarioView().mostrarMenu());
                case 6 -> ejecutarModuloSeguro(() -> getAlertaView().mostrarMenu());
                case 7 -> ejecutarModuloSeguro(() -> getFacturacionView().mostrarMenu());
                case 8 -> ejecutarModuloSeguro(() -> getActividadView().mostrarMenu());
                case 9 -> ejecutarModuloSeguro(() -> getReporteView().mostrarMenu());
                case 10 -> {
                    // El asistente de base de datos siempre es accesible en linea y fuera de linea
                    getConfiguradorConsola().mostrarMenu();
                    ConsolaUtils.setMenuActual(menu);
                }
                case 0 -> {
                    ConsolaUtils.imprimirInfo("Cerrando sesion de Happy Feet. Hasta pronto.");
                    LoggerManager.info(MODULO, "Cierre voluntario del sistema por parte del usuario.");
                    ejecutando = false;
                }
                default -> ConsolaUtils.imprimirAdvertencia("Opcion fuera de rango.");
            }
        }
    }

    // Ejecuta la vista correspondiente validando previamente si hay conexion a base de datos
    private void ejecutarModuloSeguro(Runnable accionModulo) {
        boolean online = ConexionBD.getInstancia().probarConectividad();
        if (!online) {
            ConsolaUtils.imprimirAdvertencia("El modulo seleccionado requiere conexion activa a la base de datos MySQL.");
            ConsolaUtils.imprimirInfo("Por favor, ingrese a la opcion 10 (Asistente y Diagnostico de BD) para revisar credenciales o iniciar el esquema.");
            ConsolaUtils.pausar();
            return;
        }

        try {
            accionModulo.run();
        } catch (OperacionCanceladaException e) {
            ConsolaUtils.imprimirAdvertencia(e.getMessage() + " Regresando al menu principal...");
        } catch (Exception e) {
            LoggerManager.error(MODULO, "Error no capturado durante la ejecucion del modulo", e);
            ConsolaUtils.imprimirError("Ocurrio un problema al ejecutar la operacion: " + e.getMessage());
            ConsolaUtils.pausar();
        }
    }

    // Submenu para alternar entre mascotas y duenos
    private void mostrarSubmenuPacientes() {
        Runnable submenu = () -> {
            ConsolaUtils.imprimirTitulo("GESTION DE MASCOTAS Y PROPIETARIOS");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Gestion de Mascotas (Historias, Carnet de Salud)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Gestion de Tutores y Propietarios (Duenos, Contacto)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al Menu Principal" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(submenu);

        boolean enSubmenu = true;
        while (enSubmenu) {
            submenu.run();
            int op = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-2]: ", 0, 2);
            switch (op) {
                case 1 -> {
                    getMascotaView().mostrarMenu();
                    ConsolaUtils.setMenuActual(submenu);
                }
                case 2 -> {
                    getDuenoView().mostrarMenu();
                    ConsolaUtils.setMenuActual(submenu);
                }
                case 0 -> enSubmenu = false;
                default -> ConsolaUtils.imprimirAdvertencia("Opcion no valida.");
            }
        }
    }
}
