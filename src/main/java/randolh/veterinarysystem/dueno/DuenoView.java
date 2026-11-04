package randolh.veterinarysystem.dueno;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;

import java.util.List;
import java.util.Optional;

// Vista de consola para la gestion de duenos y clientes
public class DuenoView {

    private final DuenoController controller;

    public DuenoView() {
        this.controller = new DuenoController();
    }

    public DuenoView(DuenoController controller) {
        this.controller = controller;
    }

    // Muestra el menu principal del modulo de duenos
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE GESTION DE DUENOS Y CLIENTES");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Registrar nuevo dueno / cliente" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Listar todos los duenos registrados (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Buscar dueno por numero de documento" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Buscar duenos por nombre o apellido" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Actualizar informacion de contacto de un dueno" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Desactivar / dar de baja dueno (eliminacion logica)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-6]: ", 0, 6);

            switch (opcion) {
                case 1 -> registrarNuevoDueno();
                case 2 -> listarDuenosPaginado();
                case 3 -> buscarPorDocumento();
                case 4 -> buscarPorNombre();
                case 5 -> actualizarDueno();
                case 6 -> desactivarDueno();
                case 0 -> {
                    ConsolaUtils.imprimirInfo("Regresando al menu principal...");
                    continuar = false;
                }
                default -> ConsolaUtils.imprimirError("Opcion no reconocida.");
            }

            if (continuar) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Formulario de registro de dueno
    private void registrarNuevoDueno() {
        ConsolaUtils.imprimirSubtitulo("Registrar Nuevo Dueno / Cliente");

        try {
            String numDoc = ConsolaUtils.leerTextoObligatorio("Numero de documento: ", 20);
            String nombre = ConsolaUtils.leerTextoObligatorio("Nombre completo: ", 150);
            String telefono = ConsolaUtils.leerTextoObligatorio("Numero de telefono: ", 20);
            String email = ConsolaUtils.leerTextoObligatorio("Correo electronico: ", 100);
            String direccion = ConsolaUtils.leerTextoConLimite("Direccion de residencia (opcional): ", 255);
            String contactoEmergencia = ConsolaUtils.leerTextoConLimite("Contacto de emergencia (nombre/telefono opcional): ", 100);

            Dueno nuevo = new Dueno(null, numDoc, nombre, direccion.isEmpty() ? null : direccion,
                    telefono, email, contactoEmergencia.isEmpty() ? null : contactoEmergencia);

            Dueno registrado = controller.registrarDueno(nuevo);
            String docInfo = (registrado.getTipoDocumento() != null ? registrado.getTipoDocumento() + " " : "")
                    + registrado.getNumeroDocumento();
            ConsolaUtils.imprimirExito(String.format("Cliente registrado exitosamente con ID: %d (%s)",
                    registrado.getId(), docInfo));

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError("Error de validacion: " + e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Fallo inesperado al registrar dueno: " + e.getMessage());
        }
    }

    // Listado tabular con paginacion
    private void listarDuenosPaginado() {
        List<Dueno> duenos = controller.listarTodos();
        ConsolaUtils.mostrarTablaPaginada(duenos, 5, this::renderizarTablaDuenos);
    }

    // Renderiza una sublista de duenos en formato tabla
    private void renderizarTablaDuenos(List<Dueno> lista) {
        String formatoEncabezado = "| %-4s | %-4s | %-12s | %-22s | %-12s | %-24s | %-7s |%n";
        String formatoFila = "| %-4d | %-4s | %-12s | %-22s | %-12s | %-24s | %-7s |%n";
        String separador = "+------+------+--------------+------------------------+--------------+--------------------------+---------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "TIPO", "DOCUMENTO", "NOMBRE COMPLETO", "TELEFONO", "CORREO ELECTRONICO", "ESTADO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Dueno d : lista) {
            String nombreCorto = truncar(d.getNombreCompleto(), 22);
            String emailCorto = truncar(d.getEmail(), 24);
            String estado = d.isActivo() ? "ACTIVO" : "INACT";

            System.out.printf(formatoFila, d.getId(), d.getTipoDocumento(), d.getNumeroDocumento(),
                    nombreCorto, d.getTelefono(), emailCorto, estado);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    // Busqueda por cedula o numero de documento
    private void buscarPorDocumento() {
        ConsolaUtils.imprimirSubtitulo("Buscar Dueno por Documento");
        String documento = ConsolaUtils.leerTexto("Ingrese el numero de documento a consultar: ");

        try {
            Optional<Dueno> opt = controller.obtenerPorDocumento(documento);
            if (opt.isPresent()) {
                mostrarFichaDetallada(opt.get());
            } else {
                ConsolaUtils.imprimirAdvertencia("No se encontro ningun cliente con el documento: " + documento);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Busqueda por nombre o apellido
    private void buscarPorNombre() {
        ConsolaUtils.imprimirSubtitulo("Buscar Duenos por Nombre");
        String termino = ConsolaUtils.leerTexto("Ingrese nombre o parte del apellido: ");

        try {
            List<Dueno> resultados = controller.buscarPorNombre(termino);
            if (resultados.isEmpty()) {
                ConsolaUtils.imprimirAdvertencia("No se encontraron clientes coincidentes con: '" + termino + "'");
            } else {
                ConsolaUtils.mostrarTablaPaginada(resultados, 5, this::renderizarTablaDuenos);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Actualizacion interactiva de contacto
    private void actualizarDueno() {
        ConsolaUtils.imprimirSubtitulo("Actualizar Informacion de Contacto");
        int id = ConsolaUtils.leerEntero("Ingrese el ID del cliente a actualizar: ");

        Optional<Dueno> opt = controller.obtenerPorId(id);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro ningun cliente con el ID: " + id);
            return;
        }

        Dueno actual = opt.get();
        mostrarFichaDetallada(actual);
        ConsolaUtils.imprimirInfo("Presione Enter para conservar el valor actual mostrado entre corchetes.");

        try {
            String nuevoTipo = ConsolaUtils.leerTextoOpcional("Tipo de documento", actual.getTipoDocumento()).toUpperCase();
            String nuevoDoc = ConsolaUtils.leerTextoOpcional("Numero de documento", actual.getNumeroDocumento());
            String nuevoNombre = ConsolaUtils.leerTextoOpcional("Nombre completo", actual.getNombreCompleto());
            String nuevoTel = ConsolaUtils.leerTextoOpcional("Telefono", actual.getTelefono());
            String nuevoEmail = ConsolaUtils.leerTextoOpcional("Correo electronico", actual.getEmail());
            String nuevaDir = ConsolaUtils.leerTextoOpcional("Direccion", actual.getDireccion() != null ? actual.getDireccion() : "");
            String nuevoEmerg = ConsolaUtils.leerTextoOpcional("Contacto de emergencia",
                    actual.getContactoEmergencia() != null ? actual.getContactoEmergencia() : "");
            boolean nuevoActivo = ConsolaUtils.leerBooleano("Desea mantener activo al cliente?");

            actual.setTipoDocumento(nuevoTipo);
            actual.setNumeroDocumento(nuevoDoc);
            actual.setNombreCompleto(nuevoNombre);
            actual.setTelefono(nuevoTel);
            actual.setEmail(nuevoEmail);
            actual.setDireccion(nuevaDir.isEmpty() ? null : nuevaDir);
            actual.setContactoEmergencia(nuevoEmerg.isEmpty() ? null : nuevoEmerg);
            actual.setActivo(nuevoActivo);

            boolean exito = controller.actualizarDueno(actual);
            if (exito) {
                ConsolaUtils.imprimirExito("Datos del cliente actualizados exitosamente.");
            } else {
                ConsolaUtils.imprimirError("No se pudo actualizar el registro.");
            }

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError("Error de validacion: " + e.getMessage());
        }
    }

    // Desactivacion logica
    private void desactivarDueno() {
        ConsolaUtils.imprimirSubtitulo("Desactivar Dueno (Baja Logica)");
        int id = ConsolaUtils.leerEntero("Ingrese el ID del cliente a desactivar: ");

        Optional<Dueno> opt = controller.obtenerPorId(id);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro ningun cliente con el ID: " + id);
            return;
        }

        mostrarFichaDetallada(opt.get());
        boolean confirmar = ConsolaUtils.leerBooleano("Confirma que desea dar de baja a este cliente?");
        if (confirmar) {
            try {
                boolean exito = controller.desactivarDueno(id);
                if (exito) {
                    ConsolaUtils.imprimirExito("Cliente desactivado correctamente en el sistema.");
                } else {
                    ConsolaUtils.imprimirError("No fue posible desactivar el registro.");
                }
            } catch (ValidacionException e) {
                ConsolaUtils.imprimirError(e.getMessage());
            }
        } else {
            ConsolaUtils.imprimirAdvertencia("Operacion cancelada.");
        }
    }

    // Ficha detallada de un dueno
    private void mostrarFichaDetallada(Dueno d) {
        System.out.println();
        System.out.println(ConsolaUtils.COLOR_TITULO + "  FICHA DEL CLIENTE #" + d.getId() + ConsolaUtils.ANSI_RESET);
        System.out.println("  Identificacion  : " + d.getTipoDocumento() + " " + d.getNumeroDocumento());
        System.out.println("  Nombre Completo : " + d.getNombreCompleto());
        System.out.println("  Telefono        : " + d.getTelefono());
        System.out.println("  Correo          : " + d.getEmail());
        System.out.println("  Direccion       : " + (d.getDireccion() != null ? d.getDireccion() : "(sin registrar)"));
        System.out.println("  Contacto Emerg. : " + (d.getContactoEmergencia() != null ? d.getContactoEmergencia() : "(sin registrar)"));
        System.out.println("  Fecha Registro  : " + d.getFechaRegistro());
        System.out.println("  Estado Actual   : " + (d.isActivo() ? ConsolaUtils.badgeExito("ACTIVO") : ConsolaUtils.badgeError("INACTIVO")));
        System.out.println();
    }

    private String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        if (texto.length() <= maxLen) return texto;
        return texto.substring(0, maxLen - 2) + "..";
    }
}
