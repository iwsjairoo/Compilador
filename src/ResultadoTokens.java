import java.util.List;

/**
 * Clase responsable de generar diferentes reportes relacionados con el análisis léxico.
 */
public class ResultadoTokens {

    public static String generarTablaTokensString() {
        StringBuilder tabla = new StringBuilder();
        tabla.append("==================================================================================================================\n");
        tabla.append("| TOKEN                     | DESCRIPCIÓN INFORMAL                                          | LEXEMAS DE EJEMPLO |\n");
        tabla.append("==================================================================================================================\n");
        tabla.append("| INICIO                    | Palabra reservada para inicio                                 | inicio             |\n");
        tabla.append("| PROG                      | Palabra reservada para programa                               | pf2024             |\n");
        tabla.append("| DECL                      | Palabra reservada para declaración                            | decl               |\n");
        tabla.append("| IMPDIG                    | Palabra reservada para imprimir entero                        | impdig             |\n");
        tabla.append("| IMPCAD                    | Palabra reservada para imprimir cadena                        | impcad             |\n");
        tabla.append("| LEERDIG                   | Palabra reservada para leer entero                            | leerdig            |\n");
        tabla.append("| END                       | Palabra reservada para fin                                    | end                |\n");
        tabla.append("| TYPE                      | Palabra reservada para tipo básico de datos                   | int, cad, booleano |\n");
        tabla.append("| PC                        | Punto y coma                                                  | ;                  |\n");
        tabla.append("| ASIG                      | Dos puntos igual                                              | :=                 |\n");
        tabla.append("| MAS                       | Signo de suma                                                 | +                  |\n");
        tabla.append("| MENOS                     | Signo de resta                                                | -                  |\n");
        tabla.append("| DIV                       | Signo de división                                             | /                  |\n");
        tabla.append("| MUL                       | Signo de multiplicación                                       | *                  |\n");
        tabla.append("| PAREN                     | Paréntesis de apertura                                        | (                  |\n");
        tabla.append("| TESIS                     | Paréntesis de cierre                                          | )                  |\n");
        tabla.append("| IGUAL                     | Signo de igual                                                | =                  |\n");
        tabla.append("| COMA                      | Coma                                                          | ,                  |\n");
        tabla.append("| ID                        | Identificador (letras seguidas de números)                    | variable, cont1    |\n");
        tabla.append("| CINT                      | Constante entera                                              | 123, 0             |\n");
        tabla.append("| CAD                       | Constante de cadena (entre \"\")                                | \"hola\", \"mundo\"    |\n");
        tabla.append("| BOOL                      | Constante booleana                                            | verdadero, falso   |\n");
        tabla.append("==================================================================================================================\n");
        return tabla.toString();
    }

    public static String generarReporteTokens(List<Token> tokens, String contenidoOriginal) {
        StringBuilder reporte = new StringBuilder();
        reporte.append("============================================================================\n");
        reporte.append("Lista de lexemas encontrados:\n");
        reporte.append("============================================================================\n");

        int posicionActual = 0;
        for (Token token : tokens) {
            int indice = contenidoOriginal.indexOf(token.getLexema(), posicionActual);
            if (indice != -1) {
                int linea = 1;
                int posicionEnLinea = 0;
                for (int i = 0; i < indice; i++) {
                    if (contenidoOriginal.charAt(i) == '\n') {
                        linea++;
                        posicionEnLinea = 0;
                    } else {
                        posicionEnLinea++;
                    }
                }

                reporte.append("Lexema: ").append(token.getLexema())
                       .append(", Renglón: ").append(linea)
                       .append(", Posición: ").append(posicionEnLinea + 1)
                       .append(", Token: ").append(token.getTipo()).append("\n");

                posicionActual = indice + token.getLexema().length();
            } else {
                reporte.append("Lexema: ").append(token.getLexema())
                       .append(", Renglón: Desconocido, Token: ").append(token.getTipo()).append("\n");
            }
        }

        reporte.append("==========================================================================\n");
        return reporte.toString();
    }

    public static String generarReporteErrores(List<String> erroresLexicos) {
        StringBuilder reporte = new StringBuilder();
        if (!erroresLexicos.isEmpty()) {
            reporte.append("\n=====================================================================\n");
            reporte.append("Errores Léxicos:\n");
            for (String error : erroresLexicos) {
                reporte.append(error).append("\n");
            }
            reporte.append("=====================================================================\n");
        }
        return reporte.toString();
    }
}
