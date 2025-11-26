import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        List<String> argumentos = (args == null) ? Collections.emptyList() : Arrays.asList(args);
        if (!argumentos.contains("--cli")) {
            SwingUtilities.invokeLater(() -> {
                CompiladorGUI gui = new CompiladorGUI();
                gui.setVisible(true);
            });
            return;
        }

        String rutaArgumento = argumentos.stream()
                .filter(arg -> !arg.startsWith("--"))
                .findFirst()
                .orElse("C:\\Users\\carlo\\Documents\\GitHub\\Compilador\\src\\progftef.txt");

        ejecutarModoCLI(rutaArgumento);
    }

    private static void ejecutarModoCLI(String rutaArchivoFuenteStr) {
        Path rutaArchivoFuente;
        try {
            rutaArchivoFuente = Paths.get(rutaArchivoFuenteStr).toAbsolutePath();
        } catch (InvalidPathException ex) {
            System.err.println("La ruta proporcionada no es válida: " + rutaArchivoFuenteStr);
            return;
        }

        Path directorioBase = obtenerDirectorioBase(rutaArchivoFuente);
        String nombreBase = obtenerNombreBase(rutaArchivoFuente);

        Path rutaArchivoTokens = directorioBase.resolve(nombreBase + ".tok");
        Path rutaArchivoTablaSimbolos = directorioBase.resolve(nombreBase + ".tab");
        Path rutaArchivoArbolSintactico = directorioBase.resolve(nombreBase + ".arbol");
        Path rutaArchivoAsm = directorioBase.resolve(nombreBase + ".asm");
        Path rutaArchivoAsmDesdeArbol = directorioBase.resolve(nombreBase + "_arbol.asm");

        String codigoFuente;
        try {
            codigoFuente = Files.readString(rutaArchivoFuente);
        } catch (IOException e) {
            System.err.println("Error crítico al leer el archivo fuente '" + rutaArchivoFuente + "': " + e.getMessage());
            return;
        }

        System.out.println("--- Contenido del archivo " + rutaArchivoFuente.getFileName() + " ---");
        System.out.println(codigoFuente);
        System.out.println("-------------------------------------\n");

        TablaSimbolos.limpiar();
        List<String> erroresLexicos = new ArrayList<>();
        List<Token> tokens = AnalizadorLexico.analizarLexicamente(codigoFuente, erroresLexicos);
        GeneradorArchivos.generarArchivoTokens(rutaArchivoTokens, tokens);

        if (!erroresLexicos.isEmpty()) {
            System.out.println("--- Errores Léxicos Encontrados ---");
            for (String err : erroresLexicos) {
                System.out.println(err);
            }
            System.out.println("-------------------------------------\n");
            System.out.println("Se generó el archivo de tokens pese a los errores léxicos en: " + rutaArchivoTokens.toAbsolutePath());
            return;
        }

        System.out.println("Análisis léxico exitoso. Se generaron " + rutaArchivoTokens.getFileName() + " y " + rutaArchivoTablaSimbolos.getFileName() + ".");
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
            GeneradorArchivos.generarArbolSintactico(rutaArchivoArbolSintactico, arbolSintactico);
            System.out.println("Árbol de sintaxis generado en: " + rutaArchivoArbolSintactico.toAbsolutePath());

            List<String> polaca = analizadorSintactico.getPolacaInversa();
            System.out.println("\nNotación Polaca Inversa (NPI):");
            System.out.println(polaca);

            System.out.println("\nTriplos:");
            analizadorSintactico.getTriplos().forEach(System.out::println);

            try {
                Map<String, Integer> valores = new HashMap<>();
                int resultado = analizadorSintactico.ejecutarNPI(valores);
                System.out.println("\nResultado de la expresión: " + resultado);
            } catch (Exception e) {
                System.err.println("\nError al evaluar la expresión: " + e.getMessage());
            }

            GeneradorArchivos.generarCodigoEnsamblador(rutaArchivoAsm, analizadorSintactico.getPolacaInversa());
            System.out.println("Código ensamblador generado en: " + rutaArchivoAsm.toAbsolutePath());

            if (arbolSintactico != null) {
                GeneradorArchivos.generarCodigoDesdeArbol(rutaArchivoAsmDesdeArbol, arbolSintactico);
                System.out.println("Código ensamblador generado desde el árbol sintáctico en: " + rutaArchivoAsmDesdeArbol.toAbsolutePath());
            }
        } else {
            System.out.println("Expresión inválida. Se encontraron errores léxicos o sintácticos.");
        }

        System.out.println("\nArchivos de salida generados en: " + directorioBase.toAbsolutePath());
    }

    private static Path obtenerDirectorioBase(Path rutaArchivoFuente) {
        Path directorioBase = rutaArchivoFuente.getParent();
        if (directorioBase == null) {
            directorioBase = Paths.get("").toAbsolutePath();
        }
        return directorioBase;
    }

    private static String obtenerNombreBase(Path rutaArchivoFuente) {
        String nombreArchivo = rutaArchivoFuente.getFileName().toString();
        int indice = nombreArchivo.lastIndexOf('.');
        if (indice > 0) {
            return nombreArchivo.substring(0, indice);
        }
        return nombreArchivo;
    }
}