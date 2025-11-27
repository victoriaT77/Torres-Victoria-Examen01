import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import tvHormigas.tvAlimento;
import tvHormigas.tvHerviboro;
import tvHormigas.tvIHormiga;

public class tvAntCiberDron implements tvIHormiga, tvIIA {

    private String nombre;

    public tvAntCiberDron(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean tvcomer(tvAlimento alimento) {
    return alimento instanceof tvHerviboro;
    }

    @Override
    public List<List<String>> tvbuscar(String archivoRuta) {
        final Pattern P1 = Pattern.compile("^abcdt+$");
        final Pattern P2 = Pattern.compile("^abcd*$");

        List<List<String>> result = new ArrayList<>();

        if (archivoRuta == null || archivoRuta.isBlank()) {
            System.err.println("Ruta de archivo vacía.");
            return result;
        }

        Path file = Path.of(archivoRuta);
        if (!Files.exists(file)) {
            System.err.println("Archivo no encontrado: " + file.toAbsolutePath());
            return result;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
            return result;
        }

        if (lines.isEmpty()) {
            System.err.println("El archivo está vacío.");
            return result;
        }

        if (lines.get(0).startsWith("\uFEFF")) {
            lines.set(0, lines.get(0).substring(1));
        }

        // encabezado (línea 0) usando ';' como delimitador
        List<String> header = parseCsvLine(lines.get(0), ';');

        // construir encabezado reducido
        List<String> headerReduced = new ArrayList<>();
        headerReduced.add(header.get(0));   // primera columna
        headerReduced.add("ValorVerdad");   // nueva columna

        result.add(headerReduced);

        // recorrer todas las filas de datos (desde línea 1)
        List<String> dataLines = lines.subList(1, lines.size());
        for (String line : dataLines) {
            List<String> row = parseCsvLine(line, ';');

            // primera columna
            String firstCol = row.size() > 0 ? row.get(0) : "";

            // evaluar columna 7 (índice 6)
            String targetCell = row.size() > 6 ? row.get(6) : "";
            boolean truth = P1.matcher(targetCell).matches() || P2.matcher(targetCell).matches();

            // construir fila reducida
            List<String> truthRow = new ArrayList<>();
            truthRow.add(firstCol);
            truthRow.add(truth ? "true" : "false");

            result.add(truthRow);
        }

        return result;
    }

    /**
     * Helper: parsea una línea CSV simple respetando comillas y comillas escapadas ("").
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
        fields.add(cur.toString());
        return fields;
    }
}