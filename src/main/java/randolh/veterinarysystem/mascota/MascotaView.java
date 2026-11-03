package randolh.veterinarysystem.mascota;

import randolh.veterinarysystem.consulta.ProcedimientoEspecial;
import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Vista de consola para la gestion de mascotas y pacientes clinicos
public class MascotaView {

    private static final String MODULO = "MascotaView";
    private final MascotaController controller;

    public MascotaView() {
        this.controller = new MascotaController();
    }

    public MascotaView(MascotaController controller) {
        this.controller = controller;
    }

    // Menu principal del modulo de mascotas
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE GESTION DE MASCOTAS");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Registrar nueva mascota" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Listar todas las mascotas registradas (tabla paginada)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Buscar mascota por ID" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "4. Buscar mascota por codigo de microchip" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "5. Buscar mascotas por nombre" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "6. Consultar mascotas de un dueno especifico" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "7. Transferir formalmente la propiedad de una mascota" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "8. Generar y exportar Carnet de Salud de la mascota" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "9. Desactivar / dar de baja mascota (eliminacion logica)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-9]: ", 0, 9);

            switch (opcion) {
                case 1 -> registrarNuevaMascota();
                case 2 -> listarPacientesPaginado();
                case 3 -> buscarPorId();
                case 4 -> buscarPorMicrochip();
                case 5 -> buscarPorNombre();
                case 6 -> listarPorDueno();
                case 7 -> transferirPropiedad();
                case 8 -> exportarCarnetSalud();
                case 9 -> desactivarPaciente();
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

    // Formulario interactivo para registrar nueva mascota
    private void registrarNuevaMascota() {
        ConsolaUtils.imprimirSubtitulo("Alta de Mascota");

        try {
            int duenoId = ConsolaUtils.leerEntero("ID del dueno responsable: ");
            String nombre = ConsolaUtils.leerTextoObligatorio("Nombre de la mascota: ", 100);

            // Seleccion interactiva de especie
            List<MascotaDAO.CatalogoItem> especies = controller.listarEspecies();
            System.out.println(ConsolaUtils.ANSI_CYAN + "Especies disponibles:" + ConsolaUtils.ANSI_RESET);
            for (MascotaDAO.CatalogoItem esp : especies) {
                System.out.printf("  [%d] %s%n", esp.getId(), esp.getNombre());
            }
            int especieId = ConsolaUtils.leerEntero("Seleccione el ID de la especie: ");

            // Seleccion interactiva de raza segun la especie elegida
            List<MascotaDAO.CatalogoItem> razas = controller.listarRazasPorEspecie(especieId);
            int razaId;
            if (razas.isEmpty()) {
                ConsolaUtils.imprimirAdvertencia("No hay razas parametrizadas para esta especie. Se asignara raza por defecto.");
                razaId = 1;
            } else {
                System.out.println(ConsolaUtils.ANSI_CYAN + "Razas disponibles:" + ConsolaUtils.ANSI_RESET);
                for (MascotaDAO.CatalogoItem rz : razas) {
                    System.out.printf("  [%d] %s%n", rz.getId(), rz.getNombre());
                }
                razaId = ConsolaUtils.leerEntero("Seleccione el ID de la raza: ");
            }

            LocalDate fechaNac = capturarFechaNacimientoOEdadEstimada();
            boolean esHembra = ConsolaUtils.leerBooleano("La mascota es Hembra?");
            SexoEnum sexo = esHembra ? SexoEnum.HEMBRA : SexoEnum.MACHO;

            double peso = ConsolaUtils.leerDecimalEnRango("Peso actual en kilogramos (ej. 14.5): ", 0.02, 999.0);
            String microchip = ConsolaUtils.leerTextoConLimite("Numero de microchip (opcional): ", 50);
            String tatuaje = ConsolaUtils.leerTextoConLimite("Codigo de tatuaje (opcional): ", 50);
            String fotoUrl = ConsolaUtils.leerTextoConLimite("URL de fotografia (opcional): ", 255);
            String alergias = ConsolaUtils.leerTexto("Alergias conocidas (opcional): ");
            String condiciones = ConsolaUtils.leerTexto("Condiciones preexistentes o cirugias previas (opcional): ");

            // Construccion de la entidad con Mascota.Builder
            Mascota nuevaMascota = Mascota.builder()
                    .conDueno(duenoId)
                    .conNombre(nombre)
                    .conEspecie(especieId)
                    .conRaza(razaId)
                    .conFechaNacimiento(fechaNac)
                    .conSexo(sexo)
                    .conPeso(peso)
                    .conMicrochip(microchip.isEmpty() ? null : microchip)
                    .conTatuaje(tatuaje.isEmpty() ? null : tatuaje)
                    .conFotoUrl(fotoUrl.isEmpty() ? null : fotoUrl)
                    .conAlergias(alergias.isEmpty() ? null : alergias)
                    .conCondiciones(condiciones.isEmpty() ? null : condiciones)
                    .build();

            Mascota guardada = controller.registrarMascota(nuevaMascota);
            ConsolaUtils.imprimirExito(String.format("Mascota '%s' registrada exitosamente con ID: %d",
                    guardada.getNombre(), guardada.getId()));

        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError("Error de validacion: " + e.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar mascota: " + e.getMessage());
        }
    }

    // Captura fecha exacta o calcula la fecha a partir de edad estimada en anos y meses
    private LocalDate capturarFechaNacimientoOEdadEstimada() {
        boolean conoceFecha = ConsolaUtils.leerBooleano("¿Conoce la fecha exacta de nacimiento de la mascota?");
        if (conoceFecha) {
            return ConsolaUtils.leerFecha("Fecha de nacimiento");
        }

        System.out.println(ConsolaUtils.ANSI_CYAN + "Estimacion de edad de la mascota:" + ConsolaUtils.ANSI_RESET);
        int anios = ConsolaUtils.leerEnteroEnRango("Edad estimada en anos (0 a 30): ", 0, 30);
        int meses = ConsolaUtils.leerEnteroEnRango("Meses adicionales (0 a 11): ", 0, 11);

        LocalDate fechaCalculada;
        if (anios == 0 && meses == 0) {
            fechaCalculada = LocalDate.now();
        } else {
            fechaCalculada = LocalDate.now().minusYears(anios).minusMonths(meses);
        }
        ConsolaUtils.imprimirInfo("Fecha aproximada calculada: " + fechaCalculada);
        return fechaCalculada;
    }

    // Listado tabular paginado de pacientes
    private void listarPacientesPaginado() {
        List<Mascota> pacientes = controller.listarTodas();
        ConsolaUtils.mostrarTablaPaginada(pacientes, 5, this::renderizarTablaMascotas);
    }

    // Renderiza la tabla formateada
    private void renderizarTablaMascotas(List<Mascota> lista) {
        String formatoEncabezado = "| %-4s | %-16s | %-10s | %-14s | %-6s | %-8s | %-16s | %-6s |%n";
        String formatoFila = "| %-4d | %-16s | %-10s | %-14s | %-6s | %-8.2f | %-16s | %-6s |%n";
        String separador = "+------+------------------+------------+----------------+--------+----------+------------------+--------+";

        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
        System.out.printf(ConsolaUtils.COLOR_TITULO + formatoEncabezado + ConsolaUtils.ANSI_RESET,
                "ID", "NOMBRE", "ESPECIE", "RAZA", "SEXO", "PESO(KG)", "DUENO", "ESTADO");
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);

        for (Mascota m : lista) {
            String nombreCorto = truncar(m.getNombre(), 16);
            String esp = m.getNombreEspecie() != null ? truncar(m.getNombreEspecie(), 10) : String.valueOf(m.getEspecieId());
            String rz = m.getNombreRaza() != null ? truncar(m.getNombreRaza(), 14) : String.valueOf(m.getRazaId());
            String dueno = m.getNombreDueno() != null ? truncar(m.getNombreDueno(), 16) : String.valueOf(m.getDuenoId());
            String estado = m.isActivo() ? "ACT" : "INACT";

            System.out.printf(formatoFila, m.getId(), nombreCorto, esp, rz, m.getSexo().getValorDb(),
                    m.getPesoActual(), dueno, estado);
        }
        System.out.println(ConsolaUtils.COLOR_BORDE + separador + ConsolaUtils.ANSI_RESET);
    }

    // Busqueda por ID
    private void buscarPorId() {
        ConsolaUtils.imprimirSubtitulo("Buscar Mascota por ID");
        int id = ConsolaUtils.leerEntero("Ingrese el ID de la mascota: ");

        Optional<Mascota> opt = controller.obtenerPorId(id);
        if (opt.isPresent()) {
            mostrarFichaClinica(opt.get());
        } else {
            ConsolaUtils.imprimirAdvertencia("No se encontro ninguna mascota con el ID: " + id);
        }
    }

    // Busqueda por microchip
    private void buscarPorMicrochip() {
        ConsolaUtils.imprimirSubtitulo("Buscar Mascota por Microchip");
        String chip = ConsolaUtils.leerTexto("Ingrese el codigo de microchip: ");

        try {
            Optional<Mascota> opt = controller.obtenerPorMicrochip(chip);
            if (opt.isPresent()) {
                mostrarFichaClinica(opt.get());
            } else {
                ConsolaUtils.imprimirAdvertencia("No se encontro ninguna mascota con el microchip: " + chip);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Busqueda por nombre
    private void buscarPorNombre() {
        ConsolaUtils.imprimirSubtitulo("Buscar Mascotas por Nombre");
        String nombre = ConsolaUtils.leerTexto("Ingrese el nombre a buscar: ");

        try {
            List<Mascota> lista = controller.buscarPorNombre(nombre);
            if (lista.isEmpty()) {
                ConsolaUtils.imprimirAdvertencia("No se encontraron mascotas con el nombre: " + nombre);
            } else {
                ConsolaUtils.mostrarTablaPaginada(lista, 5, this::renderizarTablaMascotas);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Listar pacientes de un dueno
    private void listarPorDueno() {
        ConsolaUtils.imprimirSubtitulo("Mascotas de un Dueno / Cliente");
        int duenoId = ConsolaUtils.leerEntero("Ingrese el ID del cliente: ");

        try {
            List<Mascota> lista = controller.listarPorDueno(duenoId);
            if (lista.isEmpty()) {
                ConsolaUtils.imprimirAdvertencia("El cliente no tiene mascotas registradas.");
            } else {
                ConsolaUtils.mostrarTablaPaginada(lista, 5, this::renderizarTablaMascotas);
            }
        } catch (ValidacionException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        }
    }

    // Flujo interactivo de transferencia de propiedad
    private void transferirPropiedad() {
        ConsolaUtils.imprimirSubtitulo("Transferencia Formal de Propiedad");

        int mascotaId = ConsolaUtils.leerEntero("Ingrese el ID de la mascota a transferir: ");
        Optional<Mascota> opt = controller.obtenerPorId(mascotaId);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro la mascota ID: " + mascotaId);
            return;
        }

        Mascota m = opt.get();
        mostrarFichaClinica(m);

        int nuevoDuenoId = ConsolaUtils.leerEntero("Ingrese el ID del nuevo dueno receptor: ");
        String motivo = ConsolaUtils.leerTextoObligatorio("Motivo de la transferencia (adopcion, cesion, mudanza): ", 255);

        boolean confirmar = ConsolaUtils.leerBooleano(String.format("Confirma transferir '%s' al cliente ID %d?",
                m.getNombre(), nuevoDuenoId));

        if (confirmar) {
            try {
                controller.transferirPropiedad(mascotaId, nuevoDuenoId, motivo);
                ConsolaUtils.imprimirExito("Transferencia de propiedad realizada con exito y registrada en historial.");
            } catch (ValidacionException e) {
                ConsolaUtils.imprimirError("Error de validacion: " + e.getMessage());
            } catch (Exception e) {
                ConsolaUtils.imprimirError("Error al transferir: " + e.getMessage());
            }
        } else {
            ConsolaUtils.imprimirAdvertencia("Transferencia cancelada.");
        }
    }

    // Exporta el carnet oficial de salud en texto limpio estructurado
    private void exportarCarnetSalud() {
        ConsolaUtils.imprimirSubtitulo("Exportar Carnet de Salud de la Mascota");
        int id = ConsolaUtils.leerEntero("Ingrese el ID de la mascota: ");

        Optional<Mascota> opt = controller.obtenerPorId(id);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro ninguna mascota con el ID: " + id);
            return;
        }

        Mascota m = opt.get();
        ConexionBD config = ConexionBD.getInstancia();

        File dir = new File("exportaciones/carnets");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File archivo = new File(dir, "carnet_mascota_" + m.getId() + ".txt");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            pw.println("================================================================================");
            pw.println("                   " + config.getNombreClinica().toUpperCase());
            pw.println("                  NIT: " + config.getNitClinica() + " - Tel: " + config.getTelefonoClinica());
            pw.println("                       " + config.getDireccionClinica());
            pw.println("================================================================================");
            pw.println("                     CARNET DE SALUD Y CONTROL CLINICO");
            pw.println("--------------------------------------------------------------------------------");
            pw.printf(" Paciente ID   : #%d%n", m.getId());
            pw.printf(" Nombre        : %s%n", m.getNombre());
            pw.printf(" Especie / Raza: %s / %s%n", m.getNombreEspecie(), m.getNombreRaza());
            pw.printf(" Sexo          : %s%n", m.getSexo().getValorDb());
            pw.printf(" Nacimiento    : %s (Edad: %s)%n", m.getFechaNacimiento(), m.getEdadCalculada());
            pw.printf(" Peso Actual   : %.2f kg%n", m.getPesoActual());
            pw.printf(" Microchip     : %s%n", m.getMicrochip() != null ? m.getMicrochip() : "No implantado");
            pw.printf(" Tatuaje       : %s%n", m.getTatuaje() != null ? m.getTatuaje() : "No registrado");
            pw.println("--------------------------------------------------------------------------------");
            pw.println(" INFORMACION DEL PROPIETARIO:");
            pw.printf(" Responsable   : %s (ID Cliente: #%d)%n", m.getNombreDueno(), m.getDuenoId());
            pw.println("--------------------------------------------------------------------------------");
            pw.println(" ANTECEDENTES CLINICOS:");
            pw.printf(" Alergias      : %s%n", m.getAlergias() != null ? m.getAlergias() : "Ninguna reportada");
            pw.printf(" Condiciones   : %s%n", m.getCondicionesPreexistentes() != null ? m.getCondicionesPreexistentes() : "Sin condiciones preexistentes");
            pw.println("--------------------------------------------------------------------------------");

            DateTimeFormatter dtfFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // 1. Historial de Vacunacion y Biologicos
            List<MascotaDAO.VacunaHistorialItem> vacunas = controller.listarVacunasPorMascota(m.getId());
            pw.println(" HISTORIAL DE VACUNACION Y BIOLOGICOS:");
            pw.println("--------------------------------------------------------------------------------");
            if (vacunas.isEmpty()) {
                pw.println("  (No se registran vacunas aplicadas para esta mascota)");
            } else {
                pw.printf("| %-10s | %-28s | %-12s | %-10s | %-20s |%n",
                        "Fecha Apl.", "Vacuna / Biologico", "Lote", "Prox. Dosis", "Profesional");
                pw.println("+------------+------------------------------+--------------+------------+----------------------+");
                for (MascotaDAO.VacunaHistorialItem v : vacunas) {
                    String fApl = v.getFechaAplicacion() != null ? v.getFechaAplicacion().format(dtfFecha) : "N/D";
                    String fProx = v.getProximaDosis() != null ? v.getProximaDosis().format(dtfFecha) : "N/D";
                    pw.printf("| %-10s | %-28s | %-12s | %-10s | %-20s |%n",
                            fApl,
                            truncar(v.getNombreVacuna(), 28),
                            truncar(v.getLote(), 12),
                            fProx,
                            truncar(v.getVeterinario(), 20));
                }
            }
            pw.println("--------------------------------------------------------------------------------");

            // 2. Historial de Procedimientos Quirurgicos y Cirugias
            List<ProcedimientoEspecial> cirugias = controller.listarProcedimientosPorMascota(m.getId());
            pw.println(" HISTORIAL DE CIRUGIAS Y PROCEDIMIENTOS ESPECIALES:");
            pw.println("--------------------------------------------------------------------------------");
            if (cirugias.isEmpty()) {
                pw.println("  (No se registran procedimientos quirurgicos para esta mascota)");
            } else {
                pw.printf("| %-16s | %-32s | %-12s | %-20s |%n",
                        "Fecha / Hora", "Procedimiento", "Estado", "Cirujano");
                pw.println("+------------------+----------------------------------+--------------+----------------------+");
                for (ProcedimientoEspecial p : cirugias) {
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
            }
            pw.println("--------------------------------------------------------------------------------");
            pw.printf(" Fecha Emision : %s%n", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            pw.println();
            pw.println("               _____________________________________________");
            pw.println("                         Firma del Medico Veterinario");
            pw.println("================================================================================");

            ConsolaUtils.imprimirExito("Carnet de salud generado exitosamente en: " + archivo.getAbsolutePath());
            LoggerManager.info(MODULO, "Carnet exportado para paciente ID " + m.getId());

        } catch (IOException e) {
            ConsolaUtils.imprimirError("Fallo al escribir el carnet: " + e.getMessage());
            LoggerManager.error(MODULO, "Error al exportar carnet", e);
        }
    }

    // Desactivacion logica
    private void desactivarPaciente() {
        ConsolaUtils.imprimirSubtitulo("Desactivar Mascota (Baja Logica)");
        int id = ConsolaUtils.leerEntero("Ingrese el ID de la mascota a dar de baja: ");

        Optional<Mascota> opt = controller.obtenerPorId(id);
        if (opt.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No se encontro la mascota ID: " + id);
            return;
        }

        mostrarFichaClinica(opt.get());
        boolean confirmar = ConsolaUtils.leerBooleano("Confirma que desea dar de baja a esta mascota?");
        if (confirmar) {
            try {
                boolean ok = controller.desactivarMascota(id);
                if (ok) {
                    ConsolaUtils.imprimirExito("Mascota desactivada correctamente.");
                } else {
                    ConsolaUtils.imprimirError("No se pudo desactivar la mascota.");
                }
            } catch (ValidacionException e) {
                ConsolaUtils.imprimirError(e.getMessage());
            }
        }
    }

    // Ficha clinica enmarcada en caja ANSI
    private void mostrarFichaClinica(Mascota m) {
        System.out.println();
        System.out.println(ConsolaUtils.COLOR_TITULO + "  FICHA CLINICA DE MASCOTA #" + m.getId() + ConsolaUtils.ANSI_RESET);
        System.out.println("  Nombre          : " + m.getNombre());
        System.out.printf("  Especie / Raza  : %s / %s%n", m.getNombreEspecie(), m.getNombreRaza());
        System.out.printf("  Sexo / Peso     : %s / %.2f kg%n", m.getSexo().getValorDb(), m.getPesoActual());
        System.out.printf("  Nacimiento/Edad : %s (%s)%n", m.getFechaNacimiento(), m.getEdadCalculada());
        System.out.printf("  Dueno Titular   : %s (ID: #%d)%n", m.getNombreDueno(), m.getDuenoId());
        System.out.println("  Microchip       : " + (m.getMicrochip() != null ? m.getMicrochip() : "(sin microchip)"));
        System.out.println("  Tatuaje         : " + (m.getTatuaje() != null ? m.getTatuaje() : "(sin tatuaje)"));
        System.out.println("  Alergias        : " + (m.getAlergias() != null ? m.getAlergias() : "Ninguna"));
        System.out.println("  Condiciones     : " + (m.getCondicionesPreexistentes() != null ? m.getCondicionesPreexistentes() : "Ninguna"));
        System.out.println("  Estado          : " + (m.isActivo() ? ConsolaUtils.badgeExito("ACTIVO") : ConsolaUtils.badgeError("INACTIVO")));
        System.out.println();
    }

    private String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        if (texto.length() <= maxLen) return texto;
        return texto.substring(0, maxLen - 2) + "..";
    }
}
