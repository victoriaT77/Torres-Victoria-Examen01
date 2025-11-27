import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import tvHormigas.tvAlimento;
import tvHormigas.tvHerviboro;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {
        Path archivo = args.length > 0 ? Path.of(args[0]) : Path.of("TorresVictoria.csv");
        char separador = ';';  
        tvController controller = new tvController();
        
        controller.inicializar(archivo, separador);

        tvAntCiberDron parser = new tvAntCiberDron("tvNombre");
        Scanner sc = new Scanner(System.in);
        tvAntCiberDron hormiga = new tvAntCiberDron("HormigaBiológica");
        tvAlimento comida = new tvHerviboro(); 
        while (true) {
            System.out.println("====================================");
            System.out.println("  MENÚ PRINCIPAL");
            System.out.println("====================================");
            System.out.println("1) Destruir arsenal ");
            System.out.println("2) Simulación biológica ");
            System.out.println("3) Salir");
            System.out.print("Elige una opción (1-3): ");

            String opcion = sc.nextLine().trim();

            if (opcion.equals("1")) {
                System.out.println("\n-- Destruir arsenal --");

                List<List<String>> tabla = null;
                try {
                    tabla = parser.tvbuscar(archivo.toString());
                } catch (Exception e) {
                    System.out.println("\nError al ejecutar tvbuscar del dron: " + e.getMessage());
                }

                if (tabla == null || tabla.isEmpty()) {
                    System.out.println("\nNo se pudo generar la tabla de resultados (revisa el archivo o el parser).");
                } else {
                    printBooleanTable(tabla);
                }

                System.out.println("\nPresiona Enter para volver al menú...");
                sc.nextLine();

            } else if (opcion.equals("2")) {
                
                System.out.println("\n-- Simulación biológica (modo seguro) --");
                boolean estaViva = hormiga.tvcomer(comida);

                
               System.out.println("\nResultado de la simulación:");
                if (estaViva) {
                 System.out.println("✅ La hormiga está viva 🐜💚");
                } else {
                System.out.println("❌ La hormiga ha muerto 💀");
                }
                System.out.println("\nPresiona Enter para volver al menú...");
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




    private static void printBooleanTable(List<List<String>> tabla) {
        if (tabla.size() < 2) {
            System.out.println("\nTabla incompleta.");
            return;
        }

        System.out.println("\n=== Nueva tabla (primera columna + valor de verdad) ===");

        // imprimir encabezado reducido
        List<String> header = tabla.get(0);
        for (String col : header) {
            System.out.printf("%-20s", col);
        }
        System.out.println();

        // imprimir todas las filas de resultados
        for (int i = 1; i < tabla.size(); i++) {
            List<String> row = tabla.get(i);
            for (String val : row) {
                System.out.printf("%-20s", val);
            }
            System.out.println();
        }
    }
}