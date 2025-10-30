import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args != null && Arrays.asList(args).contains("--gui")) {
            SwingUtilities.invokeLater(() -> {
                CompiladorGUI gui = new CompiladorGUI();
                gui.setVisible(true);
            });
            return;
        }
        String rutaArchivoFuenteStr = "/Users/iwsjairoo/Documents/Tec/Autómatas II/Compilador/src/progftef.txt"; 
        Path rutaArchivoFuente = Paths.get(rutaArchivoFuenteStr);

        Path directorioBase = rutaArchivoFuente.getParent();
        if (directorioBase == null) { 
            directorioBase = Paths.get("").toAbsolutePath(); 
        }

        Path rutaArchivoTokens = directorioBase.resolve("progfte.tok");
        Path rutaArchivoTablaSimbolos = directorioBase.resolve("progfte.tab");
        Path rutaArchivoArbolSintactico = directorioBase.resolve("progfte.arbol");

        String codigoFuente;
        try {
            codigoFuente = new String(Files.readAllBytes(rutaArchivoFuente));
        } catch (IOException e) {
            System.err.println("Error crítico al leer el archivo fuente '" + rutaArchivoFuente + "': " + e.getMessage());
            return;
        }
        
        System.out.println("--- Contenido del archivo " + rutaArchivoFuente.getFileName() + " ---");
        System.out.println(codigoFuente);
        System.out.println("-------------------------------------\n");

        List<String> erroresLexicos = new ArrayList<>();
        List<Token> tokens = AnalizadorLexico.analizarLexicamente(codigoFuente, erroresLexicos);

        if (!erroresLexicos.isEmpty()) {
            System.out.println("--- Errores Léxicos Encontrados ---");
            for (String err : erroresLexicos) {
                System.out.println(err);
            }
            System.out.println("-------------------------------------\n");
            GeneradorArchivos.generarArchivoTokens(rutaArchivoTokens, tokens);
            return; 
        }

        System.out.println("Análisis léxico exitoso. Se generó progfte.tok y progfte.tab.");
        GeneradorArchivos.generarArchivoTokens(rutaArchivoTokens, tokens);
        GeneradorArchivos.generarArchivoTablaSimbolos(rutaArchivoTablaSimbolos, TablaSimbolos.getEntradas());

        AnalizadorSintactico analizadorSintactico = new AnalizadorSintactico(tokens);
        Node arbolSintactico = analizadorSintactico.analizar();
        List<String> erroresSintacticos = analizadorSintactico.getErroresSintacticos();
        boolean sintaxisCorrecta = arbolSintactico != null && erroresSintacticos.isEmpty();

        if (!erroresSintacticos.isEmpty()) {
            System.out.println("--- Errores Sintácticos Encontrados ---");
            for (String err : erroresSintacticos) {
                System.out.println(err);
            }
            System.out.println("-------------------------------------\n");
        }
        
        if (sintaxisCorrecta) { 
            System.out.println("Expresión válida. El análisis sintáctico fue exitoso.");
            // Generar el archivo del árbol sintáctico
            GeneradorArchivos.generarArbolSintactico(rutaArchivoArbolSintactico, arbolSintactico);
            System.out.println("Árbol de sintaxis generado en: " + rutaArchivoArbolSintactico.toAbsolutePath());

            // Mostrar NPI y Triplos
            System.out.println("\nNotación Polaca Inversa (NPI):");
            System.out.println(analizadorSintactico.getPolacaInversa());

            System.out.println("\nTriplos:");
            analizadorSintactico.getTriplos().forEach(System.out::println);

            // Ejecutar y mostrar resultado
            try {
                Map<String, Integer> valores = new HashMap<>();
                // Aquí puedes añadir valores para las variables si las hubiera.
                // Ejemplo: valores.put("x", 10);
                int resultado = analizadorSintactico.ejecutarNPI(valores);
                System.out.println("\nResultado de la expresión: " + resultado);
            } catch (Exception e) {
                System.err.println("\nError al evaluar la expresión: " + e.getMessage());
            }

            // Depuración: Verificar si el árbol sintáctico es nulo
            if (arbolSintactico == null) {
                System.err.println("El árbol sintáctico es nulo. No se puede generar el ensamblador desde el árbol.");
            } else {
                System.out.println("El árbol sintáctico se generó correctamente. Procediendo a generar el ensamblador desde el árbol.");
                // Generar archivo de ensamblador directamente desde el árbol sintáctico
                Path rutaArchivoAsmDesdeArbol = directorioBase.resolve("progfte_arbol.asm");
                GeneradorArchivos.generarCodigoDesdeArbol(rutaArchivoAsmDesdeArbol, arbolSintactico);
                System.out.println("Código ensamblador generado desde el árbol sintáctico en: " + rutaArchivoAsmDesdeArbol.toAbsolutePath());
            }

        } else {
            System.out.println("Expresión inválida. Se encontraron errores léxicos o sintácticos.");
        }
        
        System.out.println("\nArchivos de salida generados en: " + directorioBase.toAbsolutePath());
    }
}