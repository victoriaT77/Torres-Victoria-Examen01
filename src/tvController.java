import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class tvController {
    static final String CEDULA = "0850426867";


    public void tvMostrarLoading(AtomicBoolean loadingFlag, AtomicLong bytesRead, long totalBytes) {
    char[] symbols = {'-', '+'};       
    int idx = 0;
    while (loadingFlag.get()) {
        char symbol = symbols[idx % symbols.length];
        long read = bytesRead.get();
        int percent = totalBytes > 0 ? (int) Math.min(100, (read * 100) / totalBytes) : 0;
       
        System.out.print("\r" + symbol + " Cargando... " + percent + "%");
        System.out.flush();
        idx++;
        try {
            Thread.sleep(220); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.print("\nAnimación interrumpida.");
            return;
        }
    }
 
    System.out.print("\r" + symbols[0] + " Cargando... 100%\n");
    System.out.flush();
}

    public void mostrarPresentacion() {
        System.out.println("============[*]INFORMACION====================");
        System.out.println("Nombre: Torres Victoria");
        System.out.println("Cédula: " + CEDULA);
        System.out.println("==========[*]COORDENADAS UCRANEANAS======================");
    }

    /**
     * Lee y parsea el CSV devolviendo las filas.
     * Conserva espacios tal cual y soporta campos multilínea entre comillas.
     * Actualiza bytesLeidos para que el spinner calcule porcentaje.
     */
    public List<List<String>> leerCsv(Path archivo, char separador, AtomicLong bytesLeidos) throws IOException {
        List<List<String>> filas = new ArrayList<>();
        long total = Files.size(archivo);
        if (total == 0) return filas;

        int newlineBytes = System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;

        try (BufferedReader br = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            StringBuilder registro = new StringBuilder();
            boolean primera = true;

            while ((linea = br.readLine()) != null) {

                if (primera && linea.startsWith("\uFEFF")) linea = linea.substring(1);
                primera = false;

                if (registro.length() > 0) registro.append("\n");
                registro.append(linea);

                int bytesEstaLinea = linea.getBytes(StandardCharsets.UTF_8).length + newlineBytes;
                bytesLeidos.addAndGet(bytesEstaLinea);

                int comillas = 0;
                for (int i = 0; i < registro.length(); i++) {
                    char ch = registro.charAt(i);
                    if (ch == '"') {
                        if (i + 1 < registro.length() && registro.charAt(i + 1) == '"') {
                            i++;
                        } else {
                            comillas++;
                        }
                    }
                }

                if (comillas % 2 == 0) {
                    filas.add(parseLineaCsv(registro.toString(), separador));
                    registro.setLength(0);
                }
             
        }

        bytesLeidos.set(Math.min(bytesLeidos.get(), Files.size(archivo)));
        return filas;
    }
    }

    private List<String> parseLineaCsv(String linea, char separador) {
        List<String> campos = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (enComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    cur.append('"'); // comilla escapada
                    i++;
                } else {
                    enComillas = !enComillas;
                }
            } else if (c == separador && !enComillas) {
                campos.add(cur.toString()); // no hago trim, conservo espacios
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        campos.add(cur.toString());
        return campos;
    }

    public void imprimirTabla(List<List<String>> filas) {
        if (filas == null || filas.isEmpty()) {
            System.out.println("No hay datos para mostrar.");
            return;
        }

        int cols = 0;
        for (List<String> r : filas) if (r.size() > cols) cols = r.size();

        int[] anchos = new int[cols];
  
        for (List<String> r : filas) {
            for (int c = 0; c < cols; c++) {
                String cel = c < r.size() ? r.get(c) : "";
                if (cel == null) cel = "";
                int len = cel.length() + 2; // +2 por las comillas
                if (len > anchos[c]) anchos[c] = len;
            }
        }

        StringBuilder sep = new StringBuilder();
        for (int w : anchos) {
            sep.append("+");
            for (int i = 0; i < w + 2; i++) sep.append("-");
        }
        sep.append("+");

        System.out.println();
        System.out.println(sep.toString());
        for (List<String> r : filas) {
            StringBuilder lineaOut = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                String cel = c < r.size() ? r.get(c) : "";
                if (cel == null) cel = "";
                String mostrado = "\"" + cel + "\""; // comillas para visualizar espacios
                lineaOut.append("| ");
                lineaOut.append(padRight(mostrado, anchos[c]));
                lineaOut.append(" ");
            }
            lineaOut.append("|");
            System.out.println(lineaOut.toString());
            System.out.println(sep.toString());
        }
    }


    private static String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    /**
     * Método principal que controla todo:
     * - verifica archivo
     * - arranca spinner en hilo
     * - lee CSV
     * - detiene spinner y muestra tabla
     */
    public void inicializar(Path archivo, char separador) {
        System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));
        if (!Files.exists(archivo)) {
            System.err.println("Archivo no encontrado: " + archivo.toAbsolutePath());
            return;
        }

        AtomicBoolean bandera = new AtomicBoolean(true);
        AtomicLong bytesLeidos = new AtomicLong(0);
        long totalBytes;
        try {
            totalBytes = Files.size(archivo);
        } catch (IOException e) {
            System.err.println("No se pudo obtener tamaño del archivo: " + e.getMessage());
            return;
        }

        Thread hiloSpinner = new Thread(() -> tvMostrarLoading(bandera, bytesLeidos, totalBytes));
        hiloSpinner.start();

        List<List<String>> filas = null;
        try {
            mostrarPresentacion();
            filas = leerCsv(archivo, separador, bytesLeidos);
        } catch (IOException e) {
            System.err.println("\nError leyendo el archivo: " + e.getMessage());
        } finally {
            bandera.set(false);
            try { hiloSpinner.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (filas != null && !filas.isEmpty()) {
            imprimirTabla(filas);
        } else if (filas != null && filas.isEmpty()) {
            System.out.println("\nEl archivo está vacío o no contiene registros válidos.");
        }
    }
    public List<List<String>> tvbuscar(java.nio.file.Path file, char delimiter) {
    final java.util.regex.Pattern P1 = java.util.regex.Pattern.compile("^abcdt+$");
    final java.util.regex.Pattern P2 = java.util.regex.Pattern.compile("^abcd*$");

    List<List<String>> result = new ArrayList<>();

    if (file == null || !java.nio.file.Files.exists(file)) {
        System.err.println("Archivo no encontrado: " + (file == null ? "null" : file));
        return result;
    }

    List<String> lines;
    try {
        lines = java.nio.file.Files.readAllLines(file, java.nio.charset.StandardCharsets.UTF_8);
    } catch (IOException e) {
        System.err.println("Error leyendo el archivo: " + e.getMessage());
        return result;
    }

    if (lines.isEmpty()) {
        System.err.println("El archivo está vacío.");
        return result;
    }

    // eliminar BOM si existe
    if (lines.get(0).startsWith("\uFEFF")) {
        lines.set(0, lines.get(0).substring(1));
    }

    // necesitamos al menos encabezado + 7 filas de datos
    if (lines.size() < 8) { // 1 encabezado + 7 filas = 8 líneas
        System.err.println("No hay suficientes filas (se requieren encabezado + 7 filas). Filas totales: " + lines.size());
        return result;
    }

    // parsear encabezado (línea 0) y fila 7 (línea 7)
    List<String> header = parseCsvLine(lines.get(0), delimiter);
    List<String> row7 = parseCsvLine(lines.get(7), delimiter);

    // evaluar fila7 y construir fila de verdad
    List<String> truthRow = new ArrayList<>();
    for (String cell : row7) {
        String v = cell == null ? "" : cell;
        boolean truth = P1.matcher(v).matches() || P2.matcher(v).matches();
        truthRow.add(truth ? "true" : "false");
    }

    // Alinear columnas (rellenar con "")
    int cols = Math.max(header.size(), Math.max(row7.size(), truthRow.size()));
    while (header.size() < cols) header.add("");
    while (row7.size() < cols) row7.add("");
    while (truthRow.size() < cols) truthRow.add("false");

    result.add(header);
    result.add(row7);
    result.add(truthRow);
    return result;
}

/**
 * Helper: parsea una línea CSV simple respetando comillas y comillas escapadas ("").
 * Devuelve la lista de campos. Usa el delimiter pasado (',' o ';', etc.).
 */
private List<String> parseCsvLine(String line, char delimiter) {
    List<String> fields = new ArrayList<>();
    if (line == null) {
        return fields;
    }
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (c == '"') {
            if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                // comilla escapada -> añadir una comilla y saltar la siguiente
                cur.append('"');
                i++;
            } else {
                inQuotes = !inQuotes;
            }
        } else if (c == delimiter && !inQuotes) {
            fields.add(cur.toString());
            cur.setLength(0);
        } else {
            cur.append(c);
        }
    }
    // añadir último campo
    fields.add(cur.toString());
    return fields;
}
}