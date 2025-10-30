import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

public class GeneradorArchivos {

    public static void generarArchivoTokens(Path filePath, List<Token> tokens) {
        try (FileWriter writer = new FileWriter(filePath.toFile())) { 
            for (Token token : tokens) {
                writer.write("<" + token.getTipo() + ", " + token.getLexema() + ", Linea: " + token.getLinea() + ">\n");
            }
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo de tokens en " + filePath + ": " + e.getMessage());
        }
    }

    public static void generarArchivoTablaSimbolos(Path filePath, List<EntradaTablaSimbolos> entradasTabla) {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            for (EntradaTablaSimbolos entrada : entradasTabla) {
                writer.write(entrada.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo de tabla de símbolos en " + filePath + ": " + e.getMessage());
        }
    }

    public static void generarArbolSintactico(Path filePath, Node raiz) {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write("Árbol de Sintaxis para Expresiones Aritméticas:\n\n");
            escribirNodo(writer, raiz, "", true);
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo del árbol sintáctico en " + filePath + ": " + e.getMessage());
        }
    }

    private static void escribirNodo(FileWriter writer, Node nodo, String prefijo, boolean esUltimo) throws IOException {
        if (nodo == null) return;

        writer.write(prefijo + (esUltimo ? "└── " : "├── ") + nodo.getValor() + "\n");
        
        List<Node> hijos = nodo.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimoHijo = (i == hijos.size() - 1);
            escribirNodo(writer, hijos.get(i), prefijo + (esUltimo ? "    " : "│   "), esUltimoHijo);
        }
    }

    public static void generarCodigoEnsamblador(Path filePath, List<String> polacaInversa) {
        StringBuilder asm = new StringBuilder();
        asm.append(".MODEL SMALL\n");
        asm.append(".STACK 100H\n\n");
        asm.append(".DATA\n");
        asm.append("    RESULT DW ?\n");
        asm.append("    BUFFER DB 6 DUP('$')\n\n");
        asm.append(".CODE\n");
        asm.append("START:\n");
        asm.append("    MOV AX, @DATA\n");
        asm.append("    MOV DS, AX\n\n");
        asm.append("    CALL CALCULAR\n\n");
        asm.append("    MOV AX, RESULT\n");
        asm.append("    CALL MOSTRAR\n\n");
        asm.append("    MOV AH, 4CH\n");
        asm.append("    INT 21H\n\n");
        asm.append("CALCULAR PROC\n");

        for (String token : polacaInversa) {
            if (token.matches("\\d+")) { // Es un número
                asm.append("    MOV AX, ").append(token).append("\n");
                asm.append("    PUSH AX\n");
            } else { // Es un operador
                asm.append("    POP BX\n");
                asm.append("    POP AX\n");
                switch (token) {
                    case "+":
                        asm.append("    ADD AX, BX\n");
                        break;
                    case "-":
                        asm.append("    SUB AX, BX\n");
                        break;
                    case "*":
                        asm.append("    MUL BX\n");
                        break;
                    case "/":
                        asm.append("    XOR DX, DX\n");
                        asm.append("    DIV BX\n");
                        break;
                }
                asm.append("    PUSH AX\n");
            }
        }
        asm.append("    POP AX\n");
        asm.append("    MOV RESULT, AX\n");
        asm.append("    RET\n");
        asm.append("CALCULAR ENDP\n\n");

        asm.append("MOSTRAR PROC\n");
        asm.append("    MOV CX, 0\n");
        asm.append("    MOV BX, 10\n");
        asm.append("CONVERTIR_LOOP:\n");
        asm.append("    XOR DX, DX\n");
        asm.append("    DIV BX\n");
        asm.append("    PUSH DX\n");
        asm.append("    INC CX\n");
        asm.append("    CMP AX, 0\n");
        asm.append("    JNE CONVERTIR_LOOP\n");
        asm.append("MOSTRAR_LOOP:\n");
        asm.append("    POP DX\n");
        asm.append("    ADD DL, '0'\n");
        asm.append("    MOV AH, 02H\n");
        asm.append("    INT 21H\n");
        asm.append("    LOOP MOSTRAR_LOOP\n");
        asm.append("    RET\n");
        asm.append("MOSTRAR ENDP\n\n");
        asm.append("END START\n");

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(asm.toString());
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo de ensamblador en " + filePath + ": " + e.getMessage());
        }
    }

    public static void generarCodigoDesdeArbol(Path filePath, Node raiz) {
        try {
            System.out.println("Iniciando la generación de ensamblador desde el árbol sintáctico...");
            if (raiz == null) {
                System.err.println("El nodo raíz del árbol sintáctico es nulo. No se puede generar ensamblador.");
                return;
            }
            System.out.println("Nodo raíz del árbol: " + raiz.getValor());

            // Generar instrucciones en notación polaca inversa desde el árbol sintáctico
            List<String> polacaInversa = new ArrayList<>();
            generarPolacaInversa(raiz, polacaInversa);
            System.out.println("Instrucciones en notación polaca inversa generadas: " + polacaInversa);

            // Usar el método existente para generar el ensamblador
            generarCodigoEnsamblador(filePath, polacaInversa);
            System.out.println("Archivo ensamblador generado en: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error durante la generación de ensamblador desde el árbol sintáctico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generarPolacaInversa(Node nodo, List<String> polacaInversa) {
        if (nodo == null) return;

        // Recorrer hijos (postorden)
        for (Node hijo : nodo.getHijos()) {
            generarPolacaInversa(hijo, polacaInversa);
        }

        // Agregar el valor del nodo a la lista
        polacaInversa.add(nodo.getValor());
    }
}