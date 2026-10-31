package randolh.veterinarysystem;

import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.core.utils.LoggerManager;

// Punto de entrada principal y bootstrap del sistema clinico veterinario Happy Feet
public class VeterinarySystem {

    private static final String MODULO = "Bootstrap";

    public static void main(String[] args) {
        // Configuracion de codificacion UTF-8 para consola y flujos de entrada/salida
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        // Capturador global de excepciones no controladas con registro auditado en disco
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            LoggerManager.error(MODULO, "Fallo no controlado en el hilo: " + hilo.getName(), error);
            ConsolaUtils.imprimirError("Se ha producido una excepcion no controlada: " + error.getMessage());
            ConsolaUtils.imprimirInfo("Consulte app_veterinaria.log para mayor detalle tecnico.");
        });

        // Banner institucional corporativo de inicio
        mostrarBannerBienvenida();

        // Diagnostico inicial no bloqueante de enlace con la base de datos
        LoggerManager.info(MODULO, "Iniciando sistema veterinario Happy Feet v1.0.0...");
        try {
            boolean bdLista = ConexionBD.getInstancia().probarConectividad();
            if (bdLista) {
                LoggerManager.info(MODULO, "Conexion inicial con MySQL verificada satisfactoriamente.");
            } else {
                LoggerManager.warning(MODULO, "Base de datos MySQL no disponible en el arranque. Iniciando en modo Offline.");
            }
        } catch (Exception e) {
            LoggerManager.warning(MODULO, "No fue posible verificar el enlace inicial con MySQL: " + e.getMessage());
        }

        // Delegacion al menu principal estructurado
        MenuPrincipal menu = new MenuPrincipal();
        menu.iniciar();

        LoggerManager.info(MODULO, "Finalizacion del proceso y apagado del sistema.");
    }

    // Muestra el banner corporativo en texto estilizado ANSI sin emojis
    private static void mostrarBannerBienvenida() {
        System.out.println(ConsolaUtils.COLOR_TITULO + """
        ============================================================================
           _   _   ___   ____  ____  __   __   _____ _____ _____ _____ 
          | | | | / _ \\ |  _ \\|  _ \\ \\ \\ / /  |  ___| ____| ____|_   _|
          | |_| |/ /_\\ \\| |_) | |_) | \\ V /   | |_  |  _| |  _|   | |  
          |  _  ||  _  ||  __/|  __/   | |    |  _| | |___| |___  | |  
          |_| |_||_| |_||_|   |_|      |_|    |_|   |_____|_____| |_|  
                           CLINICA VETERINARIA INTEGRAL                      
                        Version 1.0.0 Enterprise Edition                     
        ============================================================================
        """ + ConsolaUtils.ANSI_RESET);
    }
}
