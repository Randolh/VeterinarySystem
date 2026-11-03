package randolh.veterinarysystem.core.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

// Utilitario de consola sin emojis con paleta ANSI, NO_COLOR y lectura segura
public final class ConsolaUtils {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final boolean COLOR_HABILITADO = System.getenv("NO_COLOR") == null;

    // Constantes de estilo ANSI
    public static final String ANSI_RESET = COLOR_HABILITADO ? "\u001B[0m" : "";
    public static final String ANSI_BOLD = COLOR_HABILITADO ? "\u001B[1m" : "";
    public static final String ANSI_CYAN = COLOR_HABILITADO ? "\u001B[96m" : "";
    public static final String ANSI_GREEN = COLOR_HABILITADO ? "\u001B[92m" : "";
    public static final String ANSI_YELLOW = COLOR_HABILITADO ? "\u001B[93m" : "";
    public static final String ANSI_RED = COLOR_HABILITADO ? "\u001B[91m" : "";
    public static final String ANSI_WHITE = COLOR_HABILITADO ? "\u001B[97m" : "";
    public static final String ANSI_GRAY = COLOR_HABILITADO ? "\u001B[90m" : "";

    // Colores de fondo
    public static final String BG_RED = COLOR_HABILITADO ? "\u001B[41m" : "";
    public static final String BG_GREEN = COLOR_HABILITADO ? "\u001B[42m" : "";
    public static final String BG_YELLOW = COLOR_HABILITADO ? "\u001B[43m" : "";
    public static final String BG_CYAN = COLOR_HABILITADO ? "\u001B[46m" : "";
    public static final String BG_GRAY = COLOR_HABILITADO ? "\u001B[100m" : "";

    // Paleta semantica
    public static final String COLOR_TITULO = ANSI_BOLD + ANSI_CYAN;
    public static final String COLOR_EXITO = ANSI_BOLD + ANSI_GREEN;
    public static final String COLOR_ERROR = ANSI_BOLD + ANSI_RED;
    public static final String COLOR_ADVERTENCIA = ANSI_BOLD + ANSI_YELLOW;
    public static final String COLOR_INFO = ANSI_WHITE;
    public static final String COLOR_BORDE = ANSI_GRAY;

    // Repintado de menu actual
    private static Runnable menuActual;

    private ConsolaUtils() {
        // Constructor privado para clase utilitaria
    }

    public static void setMenuActual(Runnable menu) {
        menuActual = menu;
    }

    public static void repintarMenuActual() {
        if (menuActual != null) {
            menuActual.run();
        }
    }

    // Insignias visuales
    public static String badgeExito(String texto) {
        return BG_GREEN + ANSI_WHITE + " " + texto + " " + ANSI_RESET;
    }

    public static String badgeError(String texto) {
        return BG_RED + ANSI_WHITE + " " + texto + " " + ANSI_RESET;
    }

    public static String badgeAdvertencia(String texto) {
        return BG_YELLOW + ANSI_WHITE + " " + texto + " " + ANSI_RESET;
    }

    public static String badgeInfo(String texto) {
        return BG_CYAN + ANSI_WHITE + " " + texto + " " + ANSI_RESET;
    }

    // Metodos de impresion estandarizados
    public static void imprimirTitulo(String titulo) {
        int ancho = 76;
        String borde = "+" + "-".repeat(ancho - 2) + "+";
        String texto = titulo.length() > ancho - 6 ? titulo.substring(0, ancho - 6) : titulo;
        int espaciosTotales = (ancho - 2) - texto.length();
        int izq = espaciosTotales / 2;
        int der = espaciosTotales - izq;

        System.out.println();
        System.out.println(COLOR_BORDE + borde + ANSI_RESET);
        System.out.println(COLOR_BORDE + "|" + " ".repeat(izq) + COLOR_TITULO + texto + ANSI_RESET + COLOR_BORDE + " ".repeat(der) + "|" + ANSI_RESET);
        System.out.println(COLOR_BORDE + borde + ANSI_RESET);
    }

    public static void imprimirSubtitulo(String subtitulo) {
        System.out.println();
        System.out.println(COLOR_TITULO + "--- " + subtitulo + " ---" + ANSI_RESET);
    }

    public static void imprimirExito(String mensaje) {
        System.out.println(COLOR_EXITO + "[OK] " + mensaje + ANSI_RESET);
    }

    public static void imprimirError(String mensaje) {
        System.out.println(COLOR_ERROR + "[ERROR] " + mensaje + ANSI_RESET);
    }

    public static void imprimirAdvertencia(String mensaje) {
        System.out.println(COLOR_ADVERTENCIA + "[AVISO] " + mensaje + ANSI_RESET);
    }

    public static void imprimirInfo(String mensaje) {
        System.out.println(COLOR_INFO + "[INFO] " + mensaje + ANSI_RESET);
    }

    public static void limpiarPantalla() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("\n".repeat(25));
        }
    }

    public static void pausar() {
        System.out.print(ANSI_GRAY + "\nPresione Enter para continuar..." + ANSI_RESET);
        SCANNER.nextLine();
    }

    // Metodos de lectura segura
    public static String leerTexto(String prompt) {
        System.out.print(ANSI_WHITE + prompt + ANSI_RESET);
        return SCANNER.nextLine().trim();
    }

    public static String leerTextoOpcional(String prompt, String valorDefecto) {
        System.out.printf("%s%s [%s]: %s", ANSI_WHITE, prompt, valorDefecto, ANSI_RESET);
        String entrada = SCANNER.nextLine().trim();
        return entrada.isEmpty() ? valorDefecto : entrada;
    }

    public static String leerTextoConLimite(String prompt, int maxLongitud) {
        while (true) {
            String valor = leerTexto(prompt);
            if (valor.length() <= maxLongitud) {
                return valor;
            }
            imprimirError(String.format("El texto no puede exceder %d caracteres (ingresados: %d).", maxLongitud, valor.length()));
        }
    }

    public static String leerTextoObligatorio(String prompt, int maxLongitud) {
        while (true) {
            String valor = leerTextoConLimite(prompt, maxLongitud);
            if (!valor.isEmpty()) {
                return valor;
            }
            imprimirError("Este campo es obligatorio. Por favor ingrese un valor.");
        }
    }

    public static int leerEntero(String prompt) {
        while (true) {
            System.out.print(ANSI_WHITE + prompt + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                imprimirError("Entrada invalida. Debe ingresar un numero entero.");
                repintarMenuActual();
            }
        }
    }

    public static int leerEnteroEnRango(String prompt, int min, int max) {
        while (true) {
            int valor = leerEntero(prompt);
            if (valor >= min && valor <= max) {
                return valor;
            }
            imprimirError(String.format("El valor debe estar comprendido entre %d y %d.", min, max));
            repintarMenuActual();
        }
    }

    public static double leerDecimal(String prompt) {
        while (true) {
            System.out.print(ANSI_WHITE + prompt + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                imprimirError("Entrada invalida. Debe ingresar un numero decimal valido.");
            }
        }
    }

    public static double leerDecimalEnRango(String prompt, double min, double max) {
        while (true) {
            double valor = leerDecimal(prompt);
            if (valor >= min && valor <= max) {
                return valor;
            }
            imprimirError(String.format("El valor debe estar comprendido entre %.2f y %.2f.", min, max));
        }
    }

    public static double leerDecimalOpcional(String prompt, double valorDefecto) {
        System.out.print(ANSI_WHITE + prompt + ANSI_RESET);
        String entrada = SCANNER.nextLine().trim().replace(',', '.');
        if (entrada.isEmpty()) {
            return valorDefecto;
        }
        try {
            return Double.parseDouble(entrada);
        } catch (NumberFormatException e) {
            imprimirAdvertencia("Valor invalido. Se utilizara el valor por defecto: " + valorDefecto);
            return valorDefecto;
        }
    }

    public static LocalDate leerFecha(String prompt) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(ANSI_WHITE + prompt + " (YYYY-MM-DD): " + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim();
            try {
                return LocalDate.parse(entrada, formato);
            } catch (DateTimeParseException e) {
                imprimirError("Formato de fecha invalido. Use el formato YYYY-MM-DD (ejemplo: 2026-05-15).");
            }
        }
    }

    public static LocalDate leerFechaOpcional(String prompt) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(ANSI_WHITE + prompt + " (YYYY-MM-DD o Enter para omitir): " + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim();
            if (entrada.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(entrada, formato);
            } catch (DateTimeParseException e) {
                imprimirError("Formato de fecha invalido. Use el formato YYYY-MM-DD.");
            }
        }
    }

    public static LocalTime leerHora(String prompt) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
        while (true) {
            System.out.print(ANSI_WHITE + prompt + " (HH:mm, 24 horas): " + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim();
            try {
                return LocalTime.parse(entrada, formato);
            } catch (DateTimeParseException e) {
                imprimirError("Formato de hora invalido. Use el formato HH:mm (ejemplo: 14:30).");
            }
        }
    }

    public static boolean leerBooleano(String prompt) {
        while (true) {
            System.out.print(ANSI_WHITE + prompt + " (S/N): " + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim().toUpperCase();
            if (entrada.equals("S") || entrada.equals("SI") || entrada.equals("Y") || entrada.equals("YES")) {
                return true;
            }
            if (entrada.equals("N") || entrada.equals("NO")) {
                return false;
            }
            imprimirError("Respuesta invalida. Ingrese 'S' para Si o 'N' para No.");
        }
    }

    public static <T> void mostrarTablaPaginada(List<T> datos, int tamanoPagina, Consumer<List<T>> renderizador) {
        if (datos == null || datos.isEmpty()) {
            imprimirAdvertencia("No hay registros para mostrar.");
            return;
        }

        Runnable menuAnterior = menuActual;
        int totalRegistros = datos.size();
        int totalPaginas = (int) Math.ceil((double) totalRegistros / tamanoPagina);
        int[] paginaActual = new int[]{1};

        try {
            Runnable repintarTabla = () -> {
                int inicio = (paginaActual[0] - 1) * tamanoPagina;
                int fin = Math.min(inicio + tamanoPagina, totalRegistros);
                List<T> subLista = datos.subList(inicio, fin);

                imprimirSubtitulo(String.format("Pagina %d de %d - Total: %d registros", paginaActual[0], totalPaginas, totalRegistros));
                renderizador.accept(subLista);

                System.out.println(COLOR_BORDE + "-".repeat(76) + ANSI_RESET);
                System.out.println(ANSI_GRAY + "Navegacion: [S] Siguiente | [A] Anterior | [P] Ir a pagina | [0] Volver" + ANSI_RESET);
            };

            setMenuActual(repintarTabla);

            while (true) {
                repintarTabla.run();
                String opcion = leerTexto("Seleccione una accion: ").toUpperCase();

                if (opcion.equals("0")) {
                    break;
                } else if (opcion.equals("S")) {
                    if (paginaActual[0] < totalPaginas) {
                        paginaActual[0]++;
                    } else {
                        imprimirAdvertencia("Ya se encuentra en la ultima pagina.");
                    }
                } else if (opcion.equals("A")) {
                    if (paginaActual[0] > 1) {
                        paginaActual[0]--;
                    } else {
                        imprimirAdvertencia("Ya se encuentra en la primera pagina.");
                    }
                } else if (opcion.equals("P")) {
                    int paginaDestino = pedirNumeroPagina(totalPaginas, repintarTabla);
                    if (paginaDestino > 0) {
                        paginaActual[0] = paginaDestino;
                    }
                } else {
                    imprimirError("Opcion no reconocida.");
                }
            }
        } finally {
            setMenuActual(menuAnterior);
        }
    }

    private static int pedirNumeroPagina(int totalPaginas, Runnable repintarTabla) {
        while (true) {
            System.out.print(ANSI_WHITE + String.format("Ingrese numero de pagina (1 a %d, o 0 para cancelar): ", totalPaginas) + ANSI_RESET);
            String entrada = SCANNER.nextLine().trim();
            if (entrada.isEmpty() || entrada.equals("0")) {
                imprimirInfo("Navegacion cancelada.");
                return -1;
            }
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= 1 && valor <= totalPaginas) {
                    return valor;
                }
                imprimirError(String.format("El valor debe estar comprendido entre 1 y %d.", totalPaginas));
            } catch (NumberFormatException e) {
                imprimirError("Entrada invalida. Debe ingresar un numero entero.");
            }
            if (repintarTabla != null) {
                repintarTabla.run();
            }
        }
    }
}
