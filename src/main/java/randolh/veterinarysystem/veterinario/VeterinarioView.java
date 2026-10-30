package randolh.veterinarysystem.veterinario;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Vista de consola para la gestion del equipo medico veterinario
public class VeterinarioView {

    private final VeterinarioController controller;

    public VeterinarioView() {
        this.controller = new VeterinarioController();
    }

    public VeterinarioView(VeterinarioController controller) {
        this.controller = controller;
    }

    // Menu principal del modulo de veterinarios
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE GESTION DE MEDICOS VETERINARIOS");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Registrar nuevo medico veterinario" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Listar todo el equipo medico (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Buscar medico por numero de licencia profesional" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Filtrar medicos por especialidad" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Buscar medico por ID" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Actualizar informacion de un profesional" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "7. Desactivar / dar de baja medico (eliminacion logica)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-7]: ", 0, 7);

            switch (opcion) {
                case 1 -> registrarNuevoVeterinario();
                case 2 -> listarMedicosPaginado();
                case 3 -> buscarPorLicencia();
                case 4 -> filtrarPorEspecialidad();
                case 5 -> buscarPorId();
                case 6 -> actualizarVeterinario();
                case 7 -> desactivarVeterinario();
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

    // Formulario de alta para profesional veterinario
    private void registrarNuevoVeterinario() {
        ConsolaUtils.imprimirSubtitulo("Registrar Nuevo Medico Veterinario");

        try {
            String tipoDoc = ConsolaUtils.leerTextoOpcional("Tipo de documento (CC, CE, PAS)", "CC").toUpperCase();
            String numDoc = ConsolaUtils.leerTextoObligatorio("Numero de documento: ", 20);
            String nombre = ConsolaUtils.leerTextoObligatorio("Nombre completo del medico: ", 150);
            String licencia = ConsolaUtils.leerTextoObligatorio("Numero de tarjeta/licencia profesional: ", 50);

            System.out.println(ConsolaUtils.ANSI_CYAN + "Especialidades frecuentes: Medicina General, Cirugia, Dermatologia, Anestesiologia, Odontologia" + ConsolaUtils.ANSI_RESET);
            String especialidad = ConsolaUtils.leerTextoObligatorio("Especialidad medica: ", 100);

            String telefono = ConsolaUtils.leerTextoObligatorio("Telefono de contacto: ", 20);
            String email = ConsolaUtils.leerTextoObligatorio("Correo electronico institucional: ", 100);
            LocalDate fContratacion = ConsolaUtils.leerFechaOpcional("Fecha de contratacion");

            Veterinario nuevo = new Veterinario(tipoDoc, numDoc, nombre, licencia, especialidad,
                    telefono, email, fContratacion != null ? fContratacion : LocalDate.now());

            Veterinario registrado = controller.registrarVeterinario(nuevo);
            ConsolaUtils.imprimirExito(String.format("Medico '%s' registrado exitosamente con ID #%d (Licencia: %s)",
                    registrado.getNombreCompleto(), registrado.getId(), registrado.getLicenciaProfesional()));

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError("Error de validacion: " + e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar medico veterinario: " + e.getMessage());
        }
    }

    // Listado paginado de medicos veterinarios
    private void listarMedicosPaginado() {
        List<Veterinario> medicos = controller.listarTodos();
        ConsolaUtils.mostrarTablaPaginada(medicos, 5, this::renderizarTablaVeterinarios);
    }

    // Renderiza la tabla formateada
    private void renderizarTablaVeterinarios(List<Veterinario> lista) {
        String formatoEncabezado = "| %-4s | %-14s | %-22s | %-18s | %-12s | %-24s | %-6s |%n";
        String formatoFila = "| %-4d | %-14s | %-22s | %-18s | %-12s | %-24s | %-6s |%n";
        String separador = "+------+----------------+------------------------+--------------------+--------------+--------------------------+--------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "LICENCIA", "NOMBRE COMPLETO", "ESPECIALIDAD", "TELEFONO", "CORREO INSTITUCIONAL", "ESTADO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Veterinario v : lista) {
            String nombreCorto = truncar(v.getNombreCompleto(), 22);
            String espCorto = truncar(v.getEspecialidad(), 18);
            String emailCorto = truncar(v.getEmail(), 24);
            String estado = v.isActivo() ? "ACT" : "INACT";

            System.out.printf(formatoFila, v.getId(), v.getLicenciaProfesional(), nombreCorto,
                    espCorto, v.getTelefono(), emailCorto, estado);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    // Busqueda por numero de licencia profesional
    private void buscarPorLicencia() {
        ConsolaUtils.imprimirSubtitulo("Buscar Veterinario por Licencia");
        String licencia = ConsolaUtils.leerTexto("Ingrese el numero de licencia profesional: ");

        try {
            Optional<Veterinario> opt = controller.obtenerPorLicencia(licencia);
            if (opt.isPresent()) {
                mostrarFichaProfesional(opt.get());
            } else {
                ConsolaUtils.imprimirAdvertencia("No se encontro ningun profesional con la licencia: " + licencia);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Filtrar por especialidad
    private void filtrarPorEspecialidad() {
        ConsolaUtils.imprimirSubtitulo("Filtrar Equipo Medico por Especialidad");
        String esp = ConsolaUtils.leerTexto("Ingrese especialidad (Cirugia, Dermatologia, etc.): ");

        try {
            List<Veterinario> resultados = controller.listarPorEspecialidad(esp);
            if (resultados.isEmpty()) {
                ConsolaUtils.imprimirAdvertencia("No se encontraron profesionales con la especialidad: " + esp);
            } else {
                ConsolaUtils.mostrarTablaPaginada(resultados, 5, this::renderizarTablaVeterinarios);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Busqueda por ID
    private void buscarPorId() {
        ConsolaUtils.imprimirSubtitulo("Buscar Veterinario por ID");
        int id = ConsolaUtils.leerEntero("Ingrese el ID del profesional: ");

        Optional<Veterinario> opt = controller.obtenerPorId(id);
        if (opt.isPresent()) {
            mostrarFichaProfesional(opt.get());
        } else {
            ConsolaUtils.imprimirAdvertencia("No se encontro ningun profesional con el ID: " + id);
        }
    }

    // Actualizacion interactiva de profesional
    private void actualizarVeterinario() {
        ConsolaUtils.imprimirSubtitulo("Actualizar Informacion Profesional");
        int id = ConsolaUtils.leerEntero("Ingrese el ID del veterinario a actualizar: ");

        Optional<Veterinario> opt = controller.obtenerPorId(id);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro ningun profesional con el ID: " + id);
            return;
        }

        Veterinario actual = opt.get();
        mostrarFichaProfesional(actual);
        ConsolaUtils.imprimirInfo("Presione Enter para conservar el valor actual mostrado entre corchetes.");

        try {
            String nuevoTipo = ConsolaUtils.leerTextoOpcional("Tipo de documento", actual.getTipoDocumento()).toUpperCase();
            String nuevoDoc = ConsolaUtils.leerTextoOpcional("Numero de documento", actual.getNumeroDocumento());
            String nuevoNombre = ConsolaUtils.leerTextoOpcional("Nombre completo", actual.getNombreCompleto());
            String nuevaLic = ConsolaUtils.leerTextoOpcional("Licencia profesional", actual.getLicenciaProfesional());
            String nuevaEsp = ConsolaUtils.leerTextoOpcional("Especialidad", actual.getEspecialidad());
            String nuevoTel = ConsolaUtils.leerTextoOpcional("Telefono", actual.getTelefono());
            String nuevoEmail = ConsolaUtils.leerTextoOpcional("Correo electronico", actual.getEmail());
            boolean nuevoActivo = ConsolaUtils.leerBooleano("El medico continuara activo en la clinica?");

            actual.setTipoDocumento(nuevoTipo);
            actual.setNumeroDocumento(nuevoDoc);
            actual.setNombreCompleto(nuevoNombre);
            actual.setLicenciaProfesional(nuevaLic);
            actual.setEspecialidad(nuevaEsp);
            actual.setTelefono(nuevoTel);
            actual.setEmail(nuevoEmail);
            actual.setActivo(nuevoActivo);

            boolean exito = controller.actualizarVeterinario(actual);
            if (exito) {
                ConsolaUtils.imprimirExito("Datos del medico veterinario actualizados exitosamente.");
            } else {
                ConsolaUtils.imprimirError("No se pudo actualizar el registro.");
            }

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError("Error de validacion: " + e.getMessage());
        }
    }

    // Desactivacion logica
    private void desactivarVeterinario() {
        ConsolaUtils.imprimirSubtitulo("Desactivar Veterinario (Baja Logica)");
        int id = ConsolaUtils.leerEntero("Ingrese el ID del medico a dar de baja: ");

        Optional<Veterinario> opt = controller.obtenerPorId(id);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro ningun medico con el ID: " + id);
            return;
        }

        mostrarFichaProfesional(opt.get());
        boolean confirmar = ConsolaUtils.leerBooleano("Confirma dar de baja a este profesional?");
        if (confirmar) {
            try {
                boolean ok = controller.desactivarVeterinario(id);
                if (ok) {
                    ConsolaUtils.imprimirExito("Medico veterinario desactivado correctamente.");
                } else {
                    ConsolaUtils.imprimirError("No se pudo desactivar el registro.");
                }
            } catch (ValidacionException e) {
                ConsolaUtils.imprimirError(e.getMessage());
            }
        }
    }

    // Ficha detallada del profesional
    private void mostrarFichaProfesional(Veterinario v) {
        System.out.println();
        System.out.println(ConsolaUtils.COLOR_TITULO + "  FICHA DEL PROFESIONAL MEDICO #" + v.getId() + ConsolaUtils.ANSI_RESET);
        System.out.println("  Nombre Completo : " + v.getNombreCompleto());
        System.out.println("  Licencia Prof.  : " + v.getLicenciaProfesional());
        System.out.println("  Especialidad    : " + v.getEspecialidad());
        System.out.println("  Identificacion  : " + v.getTipoDocumento() + " " + v.getNumeroDocumento());
        System.out.println("  Telefono        : " + v.getTelefono());
        System.out.println("  Correo Inst.    : " + v.getEmail());
        System.out.println("  Contratacion    : " + (v.getFechaContratacion() != null ? v.getFechaContratacion() : "(no registrada)"));
        System.out.println("  Estado Actual   : " + (v.isActivo() ? ConsolaUtils.badgeExito("ACTIVO") : ConsolaUtils.badgeError("INACTIVO")));
        System.out.println();
    }

    private String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        if (texto.length() <= maxLen) return texto;
        return texto.substring(0, maxLen - 2) + "..";
    }
}
