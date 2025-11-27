import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tvHormigas.tvAlimento;
import tvHormigas.tvIHormiga;

public class tvAntCiberDron implements tvIHormiga, tvIIA {

    private String nombre;

    public tvAntCiberDron(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean tvcomer(tvAlimento alimento) {
        // TODO: implementar lógica real
        return true;
    }

    /**
     * Versión original que recibe la ruta como String.
     */
    @Override
    public boolean tvbuscar(String archivoRuta) {
        final java.util.regex.Pattern P1 = java.util.regex.Pattern.compile("^abcdt+$");
        final java.util.regex.Pattern P2 = java.util.regex.Pattern.compile("^abcd*$");

        if (archivoRuta == null || archivoRuta.isBlank()) {
            System.err.println("Ruta de archivo vacía.");
            return false;
        }

        Path file = Path.of(archivoRuta);
        if (!java.nio.file.Files.exists(file)) {
            System.err.println("Archivo no encontrado: " + file.toAbsolutePath());
            return false;
        }

        List<String> lines;
        try {
            lines = java.nio.file.Files.readAllLines(file, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
            return false;
        }

        if (lines.isEmpty()) {
            System.err.println("El archivo está vacío.");
            return false;
        }

        // eliminar BOM si existe
        if (lines.get(0).startsWith("\uFEFF")) {
            lines.set(0, lines.get(0).substring(1));
        }

        // datos sin encabezado
        List<String> dataLines = lines.size() > 1 ? lines.subList(1, lines.size()) : java.util.Collections.emptyList();

        // queremos la fila 7 sin contar encabezado -> índice 6 en dataLines
        int wantedIndex = 6;
        if (dataLines.size() <= wantedIndex) {
            System.err.println("No hay suficientes filas de datos. Filas disponibles (sin encabezado): " + dataLines.size());
            return false;
        }

        String targetLine = dataLines.get(wantedIndex);

        // parsear la línea respetando comillas y comillas escapadas (usa ',' como separador por defecto)
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < targetLine.length(); i++) {
            char c = targetLine.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < targetLine.length() && targetLine.charAt(i + 1) == '"') {
                    // comilla escapada
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // separador fuera de comillas
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        // añadir último campo
        fields.add(cur.toString());

        // evaluar cada celda con los patrones
        for (String cell : fields) {
            if (cell == null) continue;
            String v = cell; // si quieres ignorar espacios: cell = cell.trim();
            if (P1.matcher(v).matches() || P2.matcher(v).matches()) {
                System.out.println("Fila 7 (sin encabezado) cumple en la celda: \"" + v + "\"");
                return true;
            }
        }

        System.out.println("Fila 7 (sin encabezado) no cumple ninguno de los patrones.");
        return false;
    }

    /**
     * Sobrecarga que acepta Path y delega a la versión String.
     */
    public boolean tvbuscar(Path file) {
        return tvbuscar(file == null ? null : file.toString());
    }

    /**
     * Sobrecarga que acepta Path y separador; delega a la versión String.
     * (Actualmente ignora el delimiter y usa la lógica existente).
     */
    public boolean tvbuscar(Path file, char delimiter) {
        // Si en el futuro quieres usar 'delimiter', adapta la implementación de tvbuscar(String)
        return tvbuscar(file == null ? null : file.toString());
    }
}