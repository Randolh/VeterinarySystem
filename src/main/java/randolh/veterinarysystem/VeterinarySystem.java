package randolh.veterinarysystem;

import randolh.veterinarysystem.core.config.ConfiguradorConsola;

// Punto de entrada de la aplicacion
public class VeterinarySystem {

    public static void main(String[] args) {
        ConfiguradorConsola configurador = new ConfiguradorConsola();
        configurador.mostrarMenu();
    }
}
