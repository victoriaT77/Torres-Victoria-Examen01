import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Path archivo = args.length > 0 ? Path.of(args[0]) : Path.of("TorresVictoria.csv");
        char separador = ','; // cambia a ';' si tu CSV usa punto y coma

        tvController controller = new tvController();
        tvAntCiberDron parser = new tvAntCiberDron("tvNombre");

        Scanner sc = new Scanner(System.in);

        while (true) {
            // Menú simple
            System.out.println("====================================");
            System.out.println("  MENÚ PRINCIPAL");
            System.out.println("====================================");
            System.out.println("1) Destruir arsenal ");
            System.out.println("2) Simulación biológica ");
            System.out.println("3) Salir");
            System.out.print("Elige una opción (1-3): ");

            String opcion = sc.nextLine().trim();

            if (opcion.equals("1")) {
                // Llamar a la versión String del dron (firma: tvbuscar(String))
                boolean encontrado;
                try {
                    encontrado = parser.tvbuscar(archivo.toString());
                } catch (Exception e) {
                    System.out.println("\nError al ejecutar tvbuscar del dron: " + e.getMessage());
                    encontrado = false;
                }

                if (!encontrado) {
                    System.out.println("\nLa búsqueda no encontró coincidencias o hubo un error.");
                } else {
                    // Si el dron encontró coincidencias, pedir la tabla al controller
                    List<List<String>> tabla = null;
                    try {
                        // Asumimos que tvController tiene: public List<List<String>> tvbuscar(Path, char)
                        tabla = controller.tvbuscar(archivo, separador);
                    } catch (Exception e) {
                        System.out.println("\nNo se pudo obtener la tabla desde tvController: " + e.getMessage());
                    }

                    if (tabla == null || tabla.size() != 3) {
                        System.out.println("\nNo se pudo generar la tabla de resultados (revisa el archivo o el parser).");
                    } else {
                        printThreeRowTable(tabla);
                    }
                }

                System.out.println("\nPresiona Enter para volver al menú...");
                sc.nextLine();

            } else if (opcion.equals("2")) {
                System.out.println("\n-- Simulación biológica (modo seguro) --");
                
                sc.nextLine();

            } else if (opcion.equals("3")) {
                System.out.println("Saliendo. ¡Hasta luego!");
                sc.close();
                break;

            } else {
                System.out.println("Opción no válida. Intenta de nuevo.\n");
            }
        }
    }

    // Imprime las tres filas: encabezado, fila7 y fila de verdad
    private static void printThreeRowTable(List<List<String>> tabla) {
        List<String> header = tabla.get(0);
        List<String> row7 = tabla.get(1);
        List<String> truthRow = tabla.get(2);

        int cols = header.size();
        int[] widths = new int[cols];
        for (int c = 0; c < cols; c++) {
            int w = Math.max(header.get(c).length(),
                    Math.max(row7.get(c).length(), truthRow.get(c).length()));
            widths[c] = w;
        }

        // construir separador
        StringBuilder sep = new StringBuilder();
        for (int w : widths) {
            sep.append("+");
            for (int i = 0; i < w + 2; i++) sep.append("-");
        }
        sep.append("+");

        System.out.println();
        System.out.println(sep.toString());

        // encabezado (entre comillas para visualizar espacios)
        StringBuilder hline = new StringBuilder();
        for (int c = 0; c < cols; c++) {
            String cell = header.get(c);
            String shown = "\"" + (cell == null ? "" : cell) + "\"";
            hline.append("| ").append(padRight(shown, widths[c])).append(" ");
        }
        hline.append("|");
        System.out.println(hline.toString());
        System.out.println(sep.toString());

        // fila 7
        StringBuilder rline = new StringBuilder();
        for (int c = 0; c < cols; c++) {
            String cell = row7.get(c);
            String shown = "\"" + (cell == null ? "" : cell) + "\"";
            rline.append("| ").append(padRight(shown, widths[c])).append(" ");
        }
        rline.append("|");
        System.out.println(rline.toString());
        System.out.println(sep.toString());

        // fila de verdad
        StringBuilder tline = new StringBuilder();
        for (int c = 0; c < cols; c++) {
            String cell = truthRow.get(c);
            tline.append("| ").append(padRight(cell == null ? "" : cell, widths[c])).append(" ");
        }
        tline.append("|");
        System.out.println(tline.toString());
        System.out.println(sep.toString());
        System.out.println();
    }

    // Helper para alinear texto a la derecha con espacios
    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }
}