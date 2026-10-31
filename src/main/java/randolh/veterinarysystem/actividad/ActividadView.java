package randolh.veterinarysystem.actividad;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.ConsolaUtils;
import randolh.veterinarysystem.dueno.Dueno;
import randolh.veterinarysystem.inventario.Producto;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.veterinario.Veterinario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Vista de consola para actividades comunitarias: adopciones, jornadas de vacunacion y Club de Mascotas
public class ActividadView {

    private final ActividadController controller;

    public ActividadView() {
        this.controller = new ActividadController();
    }

    public ActividadView(ActividadController controller) {
        this.controller = controller != null ? controller : new ActividadController();
    }

    // Muestra el menu principal del modulo de actividades
    public void mostrarMenu() {
        Runnable menu = () -> {
            ConsolaUtils.imprimirTitulo("MODULO DE ACTIVIDADES ESPECIALES Y COMUNITARIAS");
            System.out.println(ConsolaUtils.COLOR_INFO + "1. Jornadas de Vacunacion Comunitaria (Fast-Track continuo)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "2. Dias de Adopcion y Contratos Legales (Texto y PDF)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_INFO + "3. Club de Mascotas Frecuentes (Ranking y Canje de Puntos)" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "0. Volver al menu anterior" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.COLOR_BORDE + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        };

        ConsolaUtils.setMenuActual(menu);

        boolean continuar = true;
        while (continuar) {
            menu.run();
            int opcion = ConsolaUtils.leerEnteroEnRango("Seleccione una opcion [0-3]: ", 0, 3);

            switch (opcion) {
                case 1 -> menuJornadasVacunacion();
                case 2 -> menuAdopciones();
                case 3 -> menuClubMascotas();
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

    // ==========================================
    // 1. SUBMODULO JORNADAS DE VACUNACION
    // ==========================================

    private void menuJornadasVacunacion() {
        boolean enSubmenu = true;
        while (enSubmenu) {
            ConsolaUtils.imprimirSubtitulo("SUBMODULO: JORNADAS DE VACUNACION COMUNITARIA");
            System.out.println("1. Iniciar sesion Fast-Track de atencion en cola continua");
            System.out.println("2. Planificar / crear nueva jornada de vacunacion");
            System.out.println("3. Listar jornadas de vacunacion y consultar avance");
            System.out.println("4. Ver registros de pacientes atendidos en una jornada");
            System.out.println("5. Cambiar estado de jornada (Planificada / En Curso / Finalizada)");
            System.out.println("0. Volver al menu de actividades");

            int opc = ConsolaUtils.leerEnteroEnRango("Seleccione opcion [0-5]: ", 0, 5);
            switch (opc) {
                case 1 -> iniciarSesionFastTrack();
                case 2 -> planificarJornada();
                case 3 -> listarJornadas();
                case 4 -> verRegistrosJornada();
                case 5 -> cambiarEstadoJornada();
                case 0 -> enSubmenu = false;
            }
            if (enSubmenu && opc != 1) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Interfaz Fast-Track de captura masiva continua en menos de 3 segundos por paciente
    private void iniciarSesionFastTrack() {
        ConsolaUtils.imprimirSubtitulo("SESION FAST-TRACK: VACUNACION MASIVA CONTINUA");
        List<JornadaVacunacion> jornadas = controller.listarJornadas();
        if (jornadas.isEmpty()) {
            ConsolaUtils.imprimirAdvertencia("No hay jornadas creadas. Planifique una jornada primero.");
            return;
        }

        System.out.println(ConsolaUtils.ANSI_BOLD + "Seleccione la jornada activa para la sesion de vacunacion:" + ConsolaUtils.ANSI_RESET);
        for (JornadaVacunacion j : jornadas) {
            System.out.printf("  [%d] %-40s (%s) - Estado: %s%n", j.getId(), j.getNombre(), j.getFecha(), j.getEstado());
        }

        int jornadaId = ConsolaUtils.leerEntero("ID de la jornada a operar: ");
        Optional<JornadaVacunacion> jOpt = controller.buscarJornada(jornadaId);
        if (jOpt.isEmpty()) {
            ConsolaUtils.imprimirError("Jornada no encontrada.");
            return;
        }
        JornadaVacunacion jornada = jOpt.get();

        // Preconfiguracion inicial unica para toda la sesion
        System.out.println("\n" + ConsolaUtils.COLOR_TITULO + "=== PRECONFIGURACION DE LA SESION MASIVA ===" + ConsolaUtils.ANSI_RESET);
        int vacunaId = ConsolaUtils.leerEntero("ID del biologico / vacuna a aplicar en la jornada: ");
        Optional<Producto> prodOpt = controller.getInventarioDAO().buscarProductoPorId(vacunaId);
        if (prodOpt.isEmpty()) {
            ConsolaUtils.imprimirError("Vacuna no encontrada en inventario.");
            return;
        }
        Producto vacuna = prodOpt.get();
        System.out.printf("Biologico seleccionado: %s | Lote: %s | Stock: %d dosis%n",
                vacuna.getNombreProducto(), vacuna.getLote(), vacuna.getCantidadStock());

        String loteVacuna = ConsolaUtils.leerTextoOpcional("Confirmar lote de vacuna [Enter para '" + vacuna.getLote() + "']: ", vacuna.getLote());

        Integer vetId = null;
        String vetStr = ConsolaUtils.leerTextoOpcional("ID del veterinario a cargo (Enter para omitir): ", "");
        if (!vetStr.isEmpty()) {
            try { vetId = Integer.parseInt(vetStr); } catch (NumberFormatException ignored) {}
        }

        ConsolaUtils.imprimirExito("Sesion Fast-Track iniciada para: " + jornada.getNombre());
        System.out.println(ConsolaUtils.badgeInfo("MODO COLA CONTINUA ACTIVO") +
                " " + ConsolaUtils.ANSI_GRAY + "Registre pacientes consecutivamente. Ingrese '0' en cualquier momento para salir." + ConsolaUtils.ANSI_RESET);

        int atendidosSesion = 0;
        boolean enCola = true;

        while (enCola) {
            System.out.println("\n" + ConsolaUtils.COLOR_BORDE + "-".repeat(70) + ConsolaUtils.ANSI_RESET);
            System.out.printf(ConsolaUtils.ANSI_BOLD + "Paciente #%d en cola | Vacuna: %s (Lote: %s)%n" + ConsolaUtils.ANSI_RESET,
                    (atendidosSesion + 1), vacuna.getNombreProducto(), loteVacuna);
            System.out.println("  1. Paciente registrado en clinica (ID o Microchip)");
            System.out.println("  2. Registro comunitario rapido (Nombre animal + Vecino responsable)");
            System.out.println("  3. Animal comunitario / callejero sin dueno (Fast)");
            System.out.println("  0. Cerrar sesion de vacunacion");

            int tipoPaciente = ConsolaUtils.leerEnteroEnRango("Opcion [0-3]: ", 0, 3);
            if (tipoPaciente == 0) {
                enCola = false;
                break;
            }

            try {
                switch (tipoPaciente) {
                    case 1 -> {
                        int mascotaId = ConsolaUtils.leerEntero("ID de la mascota registrada: ");
                        RegistroJornadaVacunacion r = controller.registrarPacienteClinica(
                                jornadaId, mascotaId, vacunaId, vetId, loteVacuna, "Atencion Fast-Track"
                        );
                        atendidosSesion++;
                        ConsolaUtils.imprimirExito(String.format("Dosis registrada para: %s [Reg #%d]", r.getNombreMascota(), r.getId()));
                    }
                    case 2 -> {
                        String nombreAnimal = ConsolaUtils.leerTexto("Nombre del animal: ");
                        int especieId = ConsolaUtils.leerEnteroEnRango("Especie [1: Canino, 2: Felino]: ", 1, 2);
                        String nombreVecino = ConsolaUtils.leerTexto("Nombre del vecino responsable: ");
                        String telVecino = ConsolaUtils.leerTextoOpcional("Telefono de contacto (opcional): ", "");

                        RegistroJornadaVacunacion r = controller.registrarPacienteComunitario(
                                jornadaId, nombreAnimal, especieId, nombreVecino, telVecino, vacunaId, vetId, loteVacuna, "Registro comunitario rapido"
                        );
                        atendidosSesion++;
                        ConsolaUtils.imprimirExito(String.format("Dosis registrada para: %s (Tutor: %s) [Reg #%d]", nombreAnimal, nombreVecino, r.getId()));
                    }
                    case 3 -> {
                        String nombreTemporal = ConsolaUtils.leerTextoOpcional("Apodo o descripcion temporal [Enter para autogenerar]: ", "");
                        int especieId = ConsolaUtils.leerEnteroEnRango("Especie [1: Canino, 2: Felino]: ", 1, 2);
                        String padrino = ConsolaUtils.leerTextoOpcional("Nombre de padrino / alimentador (opcional): ", "Comunidad del sector");

                        RegistroJornadaVacunacion r = controller.registrarAnimalCallejero(
                                jornadaId, nombreTemporal, especieId, padrino, null, vacunaId, vetId, loteVacuna, "Animal comunitario sin dueno"
                        );
                        atendidosSesion++;
                        ConsolaUtils.imprimirExito(String.format("Dosis comunitaria registrada: %s [Reg #%d]", r.getNombreMascota(), r.getId()));
                    }
                }
            } catch (ValidacionException ve) {
                ConsolaUtils.imprimirAdvertencia("Alerta: " + ve.getMessage());
            } catch (Exception e) {
                ConsolaUtils.imprimirError("Error al registrar atencion: " + e.getMessage());
            }
        }

        ConsolaUtils.imprimirExito("Sesion Fast-Track finalizada. Total de animales vacunados en esta sesion: " + atendidosSesion);
    }

    // Planifica una nueva jornada
    private void planificarJornada() {
        ConsolaUtils.imprimirSubtitulo("PLANIFICAR NUEVA JORNADA DE VACUNACION");
        String nombre = ConsolaUtils.leerTexto("Nombre de la jornada: ");
        LocalDate fecha = ConsolaUtils.leerFecha("Fecha de realizacion");
        LocalTime horaInicio = ConsolaUtils.leerHora("Hora de inicio");
        LocalTime horaFin = ConsolaUtils.leerHora("Hora de finalizacion");
        String ubicacion = ConsolaUtils.leerTexto("Ubicacion o direccion del puesto: ");
        String descripcion = ConsolaUtils.leerTextoOpcional("Descripcion o meta comunitaria: ", "");
        int capacidad = ConsolaUtils.leerEnteroEnRango("Capacidad maxima estimada de atencion: ", 10, 5000);

        try {
            JornadaVacunacion j = controller.crearJornada(nombre, fecha, horaInicio, horaFin, ubicacion, descripcion, capacidad);
            ConsolaUtils.imprimirExito("Jornada planificada con exito con ID: " + j.getId());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al planificar jornada: " + e.getMessage());
        }
    }

    // Lista todas las jornadas y su avance
    private void listarJornadas() {
        ConsolaUtils.imprimirSubtitulo("JORNADAS DE VACUNACION REGISTRADAS");
        List<JornadaVacunacion> lista = controller.listarJornadas();
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay jornadas registradas.");
            return;
        }

        ConsolaUtils.mostrarTablaPaginada(lista, 6, sublista -> {
            System.out.printf(ConsolaUtils.ANSI_BOLD + "%-4s | %-32s | %-12s | %-20s | %-10s | %-12s%n" + ConsolaUtils.ANSI_RESET,
                    "ID", "NOMBRE", "FECHA", "UBICACION", "AVANCE", "ESTADO");
            System.out.println(ConsolaUtils.COLOR_BORDE + "-".repeat(100) + ConsolaUtils.ANSI_RESET);

            for (JornadaVacunacion j : sublista) {
                String nom = j.getNombre().length() > 32 ? j.getNombre().substring(0, 29) + "..." : j.getNombre();
                String ubi = j.getUbicacion() != null && j.getUbicacion().length() > 20 ? j.getUbicacion().substring(0, 17) + "..." : j.getUbicacion();
                String avance = String.format("%d/%d", j.getTotalRegistrados(), j.getCapacidadMaxima() != null ? j.getCapacidadMaxima() : 0);

                String badge = switch (j.getEstado()) {
                    case EN_CURSO -> ConsolaUtils.badgeExito(j.getEstado().getValor());
                    case FINALIZADA -> ConsolaUtils.badgeInfo(j.getEstado().getValor());
                    case CANCELADA -> ConsolaUtils.badgeError(j.getEstado().getValor());
                    default -> ConsolaUtils.badgeAdvertencia(j.getEstado().getValor());
                };

                System.out.printf("%-4d | %-32s | %-12s | %-20s | %-10s | %s%n",
                        j.getId(), nom, j.getFecha(), ubi != null ? ubi : "-", avance, badge);
            }
        });
    }

    // Consulta los registros de pacientes atendidos en una jornada
    private void verRegistrosJornada() {
        ConsolaUtils.imprimirSubtitulo("REGISTROS DE ATENCION EN JORNADA");
        int jornadaId = ConsolaUtils.leerEntero("ID de la jornada a consultar: ");
        List<RegistroJornadaVacunacion> registros = controller.listarRegistrosDeJornada(jornadaId);
        if (registros.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay atenciones registradas para la jornada ID: " + jornadaId);
            return;
        }

        ConsolaUtils.mostrarTablaPaginada(registros, 8, sublista -> {
            System.out.printf(ConsolaUtils.ANSI_BOLD + "%-5s | %-22s | %-10s | %-24s | %-20s | %-12s%n" + ConsolaUtils.ANSI_RESET,
                    "ID", "ANIMAL", "ESPECIE", "RESPONSABLE", "VACUNA", "LOTE");
            System.out.println(ConsolaUtils.COLOR_BORDE + "-".repeat(102) + ConsolaUtils.ANSI_RESET);

            for (RegistroJornadaVacunacion r : sublista) {
                String animal = r.getNombreMascota();
                if (animal.length() > 22) animal = animal.substring(0, 19) + "...";
                String resp = r.getNombreResponsable();
                if (resp.length() > 24) resp = resp.substring(0, 21) + "...";
                String vac = r.getNombreVacuna() != null ? r.getNombreVacuna() : "Vacuna #" + r.getVacunaId();
                if (vac.length() > 20) vac = vac.substring(0, 17) + "...";

                System.out.printf("%-5d | %-22s | %-10s | %-24s | %-20s | %-12s%n",
                        r.getId(), animal, r.getNombreEspecie() != null ? r.getNombreEspecie() : "-", resp, vac, r.getLoteVacuna());
            }
        });
    }

    // Cambia el estado de una jornada
    private void cambiarEstadoJornada() {
        ConsolaUtils.imprimirSubtitulo("CAMBIAR ESTADO DE JORNADA");
        int jornadaId = ConsolaUtils.leerEntero("ID de la jornada: ");
        System.out.println("Seleccione nuevo estado:");
        System.out.println("  1. Planificada");
        System.out.println("  2. En Curso");
        System.out.println("  3. Finalizada");
        System.out.println("  4. Cancelada");

        int opc = ConsolaUtils.leerEnteroEnRango("Opcion [1-4]: ", 1, 4);
        EstadoJornadaEnum nuevoEstado = switch (opc) {
            case 2 -> EstadoJornadaEnum.EN_CURSO;
            case 3 -> EstadoJornadaEnum.FINALIZADA;
            case 4 -> EstadoJornadaEnum.CANCELADA;
            default -> EstadoJornadaEnum.PLANIFICADA;
        };

        try {
            boolean exito = controller.actualizarEstadoJornada(jornadaId, nuevoEstado);
            if (exito) {
                ConsolaUtils.imprimirExito("Estado de jornada actualizado a: " + nuevoEstado.getValor());
            } else {
                ConsolaUtils.imprimirError("No se pudo actualizar el estado de la jornada.");
            }
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. SUBMODULO DE ADOPCIONES RESPONSABLES
    // ==========================================

    private void menuAdopciones() {
        boolean enSubmenu = true;
        while (enSubmenu) {
            ConsolaUtils.imprimirSubtitulo("SUBMODULO: DIAS DE ADOPCION Y CONTRATOS LEGALES");
            System.out.println("1. Catalogo de mascotas albergadas disponibles");
            System.out.println("2. Registrar nueva mascota rescatada en el programa");
            System.out.println("3. Formalizar contrato de adopcion responsable (Texto y PDF)");
            System.out.println("4. Consultar historico de adopciones");
            System.out.println("0. Volver al menu de actividades");

            int opc = ConsolaUtils.leerEnteroEnRango("Seleccione opcion [0-4]: ", 0, 4);
            switch (opc) {
                case 1 -> listarMascotasDisponibles();
                case 2 -> registrarMascotaAdopcion();
                case 3 -> formalizarAdopcion();
                case 4 -> listarHistoricoAdopciones();
                case 0 -> enSubmenu = false;
            }
            if (enSubmenu) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Lista el catalogo de animales albergados disponibles
    private void listarMascotasDisponibles() {
        ConsolaUtils.imprimirSubtitulo("CATALOGO DE MASCOTAS DISPONIBLES PARA ADOPCION");
        List<MascotaAdopcion> disponibles = controller.listarMascotasDisponibles();
        if (disponibles.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay mascotas disponibles para adopcion en este momento.");
            return;
        }

        for (MascotaAdopcion ma : disponibles) {
            System.out.println(ConsolaUtils.COLOR_TITULO + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
            System.out.printf(ConsolaUtils.ANSI_BOLD + "  [%d] %s (%s, %s) - Sexo: %s | Edad: %s%n" + ConsolaUtils.ANSI_RESET,
                    ma.getId(), ma.getNombreMascota(), ma.getEspecieMascota(),
                    ma.getRazaMascota() != null ? ma.getRazaMascota() : "Mestizo",
                    ma.getSexoMascota(), ma.getEdadMascota() != null ? ma.getEdadMascota() + " anos" : "Desconocida");
            System.out.println(ConsolaUtils.ANSI_CYAN + "  Historia:      " + ConsolaUtils.ANSI_RESET + (ma.getHistoria() != null ? ma.getHistoria() : "-"));
            System.out.println(ConsolaUtils.ANSI_CYAN + "  Temperamento:  " + ConsolaUtils.ANSI_RESET + (ma.getTemperamento() != null ? ma.getTemperamento() : "-"));
            if (ma.getNecesidadesEspeciales() != null && !ma.getNecesidadesEspeciales().isEmpty()) {
                System.out.println(ConsolaUtils.ANSI_YELLOW + "  Cuidados Esp:  " + ConsolaUtils.ANSI_RESET + ma.getNecesidadesEspeciales());
            }
        }
        System.out.println(ConsolaUtils.COLOR_TITULO + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
    }

    // Registra una mascota rescatada
    private void registrarMascotaAdopcion() {
        ConsolaUtils.imprimirSubtitulo("REGISTRAR MASCOTA RESCATADA EN ADOPCION");
        int mascotaId = ConsolaUtils.leerEntero("ID de la mascota registrada: ");
        String historia = ConsolaUtils.leerTexto("Historia o antecedentes de rescate: ");
        String temperamento = ConsolaUtils.leerTexto("Temperamento y comportamiento: ");
        String necesidades = ConsolaUtils.leerTextoOpcional("Necesidades o cuidados especiales (opcional): ", "");
        String fotoUrl = ConsolaUtils.leerTextoOpcional("URL de fotografia (opcional): ", "");

        try {
            MascotaAdopcion ma = controller.registrarMascotaParaAdopcion(mascotaId, historia, temperamento, necesidades, fotoUrl);
            ConsolaUtils.imprimirExito("Mascota inscrita en programa de adopcion con ID: " + ma.getId());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al registrar mascota: " + e.getMessage());
        }
    }

    // Formaliza la adopcion y emite el contrato legal en texto y OpenPDF
    private void formalizarAdopcion() {
        ConsolaUtils.imprimirSubtitulo("FORMALIZACION DE CONTRATO DE ADOPCION RESPONSABLE");
        int mascotaAdopId = ConsolaUtils.leerEntero("ID de la mascota en adopcion (de la lista de disponibles): ");
        Optional<MascotaAdopcion> maOpt = controller.buscarMascotaAdopcion(mascotaAdopId);
        if (maOpt.isEmpty() || maOpt.get().getEstado() != EstadoAdopcionEnum.DISPONIBLE) {
            ConsolaUtils.imprimirError("La mascota no existe o no se encuentra disponible.");
            return;
        }
        MascotaAdopcion ma = maOpt.get();

        int adoptanteId = ConsolaUtils.leerEntero("ID del dueno / adoptante que asumira la tutela: ");
        Optional<Dueno> dOpt = controller.getDuenoDAO().buscarPorId(adoptanteId);
        if (dOpt.isEmpty()) {
            ConsolaUtils.imprimirError("Adoptante no encontrado.");
            return;
        }
        Dueno adoptante = dOpt.get();

        System.out.println("\n" + ConsolaUtils.ANSI_BOLD + "Detalle de Adopcion a Formalizar:" + ConsolaUtils.ANSI_RESET);
        System.out.printf("  Mascota:   %s (%s, %s)%n", ma.getNombreMascota(), ma.getEspecieMascota(), ma.getRazaMascota());
        System.out.printf("  Adoptante: %s (Doc: %s | Tel: %s)%n", adoptante.getNombreCompleto(), adoptante.getNumeroDocumento(), adoptante.getTelefono());

        String condiciones = ConsolaUtils.leerTextoOpcional("Condiciones o compromisos especiales acordados (opcional): ", "Compromiso de cuidado y bienestar animal integral");
        boolean seguimiento = ConsolaUtils.leerBooleano("Requiere visitas de seguimiento post-adopcion?");

        boolean confirmar = ConsolaUtils.leerBooleano("Confirma la formalizacion legal y emision del contrato?");
        if (!confirmar) {
            ConsolaUtils.imprimirAdvertencia("Operacion cancelada.");
            return;
        }

        try {
            Adopcion adop = controller.formalizarAdopcion(mascotaAdopId, adoptanteId, condiciones, seguimiento);
            ConsolaUtils.imprimirExito("Adopcion formalizada exitosamente con Contrato Nro: ADOP-" + adop.getId());
            System.out.println(ConsolaUtils.ANSI_CYAN + "Contrato TXT generado en: exportaciones/contratos/contrato_adopcion_ADOP-" + adop.getId() + ".txt" + ConsolaUtils.ANSI_RESET);
            System.out.println(ConsolaUtils.ANSI_CYAN + "Contrato PDF generado en: exportaciones/contratos/contrato_adopcion_ADOP-" + adop.getId() + ".pdf" + ConsolaUtils.ANSI_RESET);

        } catch (ValidacionException ve) {
            ConsolaUtils.imprimirAdvertencia("Alerta: " + ve.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al formalizar adopcion: " + e.getMessage());
        }
    }

    // Lista el historico de adopciones
    private void listarHistoricoAdopciones() {
        ConsolaUtils.imprimirSubtitulo("HISTORIAL DE ADOPCIONES REGISTRADAS");
        List<MascotaAdopcion> lista = controller.listarTodasMascotasAdopcion();
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay registros de adopcion en el sistema.");
            return;
        }

        ConsolaUtils.mostrarTablaPaginada(lista, 6, sublista -> {
            System.out.printf(ConsolaUtils.ANSI_BOLD + "%-4s | %-18s | %-10s | %-24s | %-12s | %-10s%n" + ConsolaUtils.ANSI_RESET,
                    "ID", "MASCOTA", "ESPECIE", "ADOPTANTE", "FECHA ADOP", "ESTADO");
            System.out.println(ConsolaUtils.COLOR_BORDE + "-".repeat(95) + ConsolaUtils.ANSI_RESET);

            for (MascotaAdopcion ma : sublista) {
                String masc = ma.getNombreMascota();
                if (masc.length() > 18) masc = masc.substring(0, 15) + "...";
                String adopt = ma.getNombreAdoptante() != null ? ma.getNombreAdoptante() : "(En albergue)";
                if (adopt.length() > 24) adopt = adopt.substring(0, 21) + "...";
                String fecha = ma.getFechaAdopcion() != null ? ma.getFechaAdopcion().toString() : "-";

                String badge = ma.getEstado() == EstadoAdopcionEnum.ADOPTADA
                        ? ConsolaUtils.badgeExito(ma.getEstado().getValor())
                        : ConsolaUtils.badgeAdvertencia(ma.getEstado().getValor());

                System.out.printf("%-4d | %-18s | %-10s | %-24s | %-12s | %s%n",
                        ma.getId(), masc, ma.getEspecieMascota(), adopt, fecha, badge);
            }
        });
    }

    // ==========================================
    // 3. SUBMODULO CLUB DE MASCOTAS FRECUENTES
    // ==========================================

    private void menuClubMascotas() {
        boolean enSubmenu = true;
        while (enSubmenu) {
            ConsolaUtils.imprimirSubtitulo("SUBMODULO: CLUB DE MASCOTAS FRECUENTES");
            System.out.println("1. Tabla de clasificacion / ranking general de puntos");
            System.out.println("2. Consultar estado y puntos de un cliente");
            System.out.println("3. Inscribir nuevo cliente en el Club");
            System.out.println("4. Catalogo de beneficios y premios canjeables");
            System.out.println("5. Realizar canje de puntos por beneficio o descuento");
            System.out.println("0. Volver al menu de actividades");

            int opc = ConsolaUtils.leerEnteroEnRango("Seleccione opcion [0-5]: ", 0, 5);
            switch (opc) {
                case 1 -> mostrarRankingClub();
                case 2 -> consultarClubDueno();
                case 3 -> inscribirDuenoEnClub();
                case 4 -> listarCatalogoBeneficios();
                case 5 -> canjearBeneficio();
                case 0 -> enSubmenu = false;
            }
            if (enSubmenu) {
                ConsolaUtils.pausar();
            }
        }
    }

    // Muestra el ranking leaderboard de clientes frecuentes
    private void mostrarRankingClub() {
        ConsolaUtils.imprimirSubtitulo("RANKING GENERAL: CLUB DE MASCOTAS FRECUENTES");
        List<ClubMascota> ranking = controller.obtenerRankingClub();
        if (ranking.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay miembros activos registrados en el club.");
            return;
        }

        System.out.printf(ConsolaUtils.ANSI_BOLD + "%-4s | %-5s | %-28s | %-10s | %-12s | %-12s%n" + ConsolaUtils.ANSI_RESET,
                "POS", "NIVEL", "CLIENTE / TITULAR", "DISPONIBLE", "ACUMULADOS", "INSCRIPCION");
        System.out.println(ConsolaUtils.COLOR_BORDE + "-".repeat(88) + ConsolaUtils.ANSI_RESET);

        int pos = 1;
        for (ClubMascota cm : ranking) {
            String nom = cm.getNombreDueno() != null ? cm.getNombreDueno() : "Dueno #" + cm.getDuenoId();
            if (nom.length() > 28) nom = nom.substring(0, 25) + "...";

            String nivelBadge = switch (cm.getNivel()) {
                case PLATINO -> ConsolaUtils.badgeInfo("PLATINO");
                case ORO -> ConsolaUtils.badgeAdvertencia("ORO");
                case PLATA -> ConsolaUtils.badgeExito("PLATA");
                default -> ConsolaUtils.badgeError("BRONCE");
            };

            System.out.printf("#%-3d | %-5s | %-28s | %8d pts | %8d pts | %s%n",
                    pos++, nivelBadge, nom, cm.getPuntosDisponibles(), cm.getPuntosAcumulados(), cm.getFechaInscripcion());
        }
    }

    // Consulta la membresia de un dueno
    private void consultarClubDueno() {
        ConsolaUtils.imprimirSubtitulo("CONSULTAR MEMBRESIA CLUB DE CLIENTE");
        int duenoId = ConsolaUtils.leerEntero("ID del cliente a consultar: ");
        Optional<ClubMascota> clubOpt = controller.buscarClubPorDueno(duenoId);
        if (clubOpt.isEmpty()) {
            ConsolaUtils.imprimirInfo("El cliente no cuenta con membresia activa en el Club de Mascotas.");
            return;
        }

        ClubMascota cm = clubOpt.get();
        System.out.println(ConsolaUtils.COLOR_TITULO + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
        System.out.println("  Cliente:             " + cm.getNombreDueno() + " (Doc: " + cm.getDocumentoDueno() + ")");
        System.out.println("  Nivel de Fidelidad:  " + ConsolaUtils.badgeExito(cm.getNivel().getValor().toUpperCase()));
        System.out.println("  Puntos Disponibles:  " + ConsolaUtils.ANSI_BOLD + cm.getPuntosDisponibles() + " puntos" + ConsolaUtils.ANSI_RESET);
        System.out.println("  Puntos Acumulados:   " + cm.getPuntosAcumulados() + " puntos");
        System.out.println("  Puntos Canjeados:    " + cm.getPuntosCanjeados() + " puntos");
        System.out.println("  Fecha Inscripcion:   " + cm.getFechaInscripcion());
        System.out.println(ConsolaUtils.COLOR_TITULO + "----------------------------------------------------------------------------" + ConsolaUtils.ANSI_RESET);
    }

    // Inscribe a un dueno
    private void inscribirDuenoEnClub() {
        ConsolaUtils.imprimirSubtitulo("INSCRIBIR CLIENTE EN EL CLUB DE MASCOTAS");
        int duenoId = ConsolaUtils.leerEntero("ID del cliente a afiliar: ");
        try {
            ClubMascota cm = controller.inscribirDuenoEnClub(duenoId);
            ConsolaUtils.imprimirExito("Cliente inscrito exitosamente con membresia ID: " + cm.getId() + " [Nivel Bronce]");
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al inscribir cliente: " + e.getMessage());
        }
    }

    // Lista el catalogo de premios y beneficios
    private void listarCatalogoBeneficios() {
        ConsolaUtils.imprimirSubtitulo("CATALOGO DE BENEFICIOS Y PREMIOS CANJEABLES");
        List<BeneficioClub> beneficios = controller.listarBeneficios();
        if (beneficios.isEmpty()) {
            ConsolaUtils.imprimirInfo("No hay beneficios activos en el catalogo.");
            return;
        }

        System.out.printf(ConsolaUtils.ANSI_BOLD + "%-4s | %-32s | %-16s | %-10s | %-12s%n" + ConsolaUtils.ANSI_RESET,
                "ID", "BENEFICIO", "TIPO", "REQUERIDO", "PUNTOS");
        System.out.println(ConsolaUtils.COLOR_BORDE + "-".repeat(85) + ConsolaUtils.ANSI_RESET);

        for (BeneficioClub b : beneficios) {
            String nom = b.getNombre().length() > 32 ? b.getNombre().substring(0, 29) + "..." : b.getNombre();
            System.out.printf("%-4d | %-32s | %-16s | %-10s | %d pts%n",
                    b.getId(), nom, b.getTipoBeneficio(), b.getNivelRequerido(), b.getPuntosNecesarios());
        }
    }

    // Canje de puntos interactivo
    private void canjearBeneficio() {
        ConsolaUtils.imprimirSubtitulo("CANJE DE PUNTOS POR BENEFICIO");
        int duenoId = ConsolaUtils.leerEntero("ID del cliente que redime puntos: ");
        Optional<ClubMascota> clubOpt = controller.buscarClubPorDueno(duenoId);
        if (clubOpt.isEmpty()) {
            ConsolaUtils.imprimirError("El cliente no pertenece al Club de Mascotas.");
            return;
        }
        ClubMascota cm = clubOpt.get();
        System.out.printf("Cliente: %s | Nivel: %s | Puntos Disponibles: %d pts%n",
                cm.getNombreDueno(), cm.getNivel(), cm.getPuntosDisponibles());

        listarCatalogoBeneficios();
        int beneficioId = ConsolaUtils.leerEntero("ID del beneficio a canjear: ");

        boolean confirmar = ConsolaUtils.leerBooleano("Confirma el canje de puntos?");
        if (!confirmar) {
            ConsolaUtils.imprimirAdvertencia("Operacion cancelada.");
            return;
        }

        try {
            CanjeBeneficio canje = controller.canjearBeneficio(duenoId, beneficioId);
            ConsolaUtils.imprimirExito("Canje realizado exitosamente!");
            System.out.printf("Beneficio: %s | Puntos Redimidos: -%d pts | Vigencia hasta: %s%n",
                    canje.getNombreBeneficio(), canje.getPuntosCanjeados(), canje.getFechaExpiracion());

        } catch (ValidacionException ve) {
            ConsolaUtils.imprimirAdvertencia("Validacion: " + ve.getMessage());
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al procesar el canje: " + e.getMessage());
        }
    }
}
