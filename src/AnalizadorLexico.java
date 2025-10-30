// Contenido del archivo AnalizadorLexico.java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorLexico {
    public static List<Token> analizarLexicamente(String codigoFuente, List<String> erroresLexicos) {
        List<Token> tokens = new ArrayList<>();
        int lineaNumero = 1;

        codigoFuente = codigoFuente.replaceAll("/\\*.*?\\*/", ""); 
        String[] lineas = codigoFuente.split("\\r?\\n");

        for (String linea : lineas) {
            String lineaDepurada = linea.trim();
            if (lineaDepurada.isEmpty()) {
                lineaNumero++;
                continue;
            }

            int posicion = 0;
            while (posicion < lineaDepurada.length()) {
                int initialPos = posicion;
                if (Character.isWhitespace(lineaDepurada.charAt(posicion))) {
                    posicion++;
                    continue;
                }

                boolean palabraReservadaEncontrada = false;
                // Manejar palabras reservadas
                for (Map.Entry<String, String> entry : Gramatica.palabrasReservadas.entrySet()) {
                    if (lineaDepurada.startsWith(entry.getKey(), posicion)) {
                        tokens.add(new Token(entry.getKey(), entry.getValue(), lineaNumero));
                        // Opcional: Agregar palabras reservadas a la tabla de símbolos
                        // TablaSimbolos.agregar(entry.getKey(), entry.getValue(), lineaNumero); 
                        posicion += entry.getKey().length();
                        palabraReservadaEncontrada = true;
                        break;
                    }
                }
                if (palabraReservadaEncontrada) {
                    continue;
                }

                // Manejar identificadores y tipos de datos (que también pueden ser IDs en un sentido más amplio)
                if (posicion < lineaDepurada.length() && Character.isLetter(lineaDepurada.charAt(posicion))) {
                    int startPos = posicion;
                    StringBuilder potentialIdentifier = new StringBuilder();
                    int currentPos = posicion;
                    boolean hasSymbols = false;

                    while (currentPos < lineaDepurada.length()
                            && !Character.isWhitespace(lineaDepurada.charAt(currentPos))) {
                        char c = lineaDepurada.charAt(currentPos);
                        if (Character.isLetterOrDigit(c)) {
                            potentialIdentifier.append(c);
                            currentPos++;
                        } else if ((c == ',') || (c == ';') || (c == ')') || (c == ':') || (c == '*') || (c == '-')) {
                            break;
                        } else {
                            potentialIdentifier.append(c);
                            hasSymbols = true;
                            currentPos++;
                        }
                    }

                    String palabra = potentialIdentifier.toString();

                    if (hasSymbols) {
                        erroresLexicos.add("Error léxico en la línea " + lineaNumero + ", posición " + (startPos + 1)
                                + ": Identificador inválido '" + palabra + "' contiene símbolos no permitidos.");
                        posicion = currentPos;
                    } else {
                        boolean hasAccents = false;
                        for (char c : palabra.toCharArray()) {
                            if (Character.isLetter(c) && !(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                                hasAccents = true;
                                break;
                            }
                        }

                        if (hasAccents) {
                            erroresLexicos.add("Error léxico en la línea " + lineaNumero + ", posición "
                                    + (startPos + 1) + ": Identificador o tipo de dato inválido '" + palabra
                                    + "' contiene caracteres acentuados.");
                            posicion = currentPos;
                        } else if (Gramatica.tiposDeDatos.containsKey(palabra)) {
                            // Los tipos de datos son tokens, pero también pueden ir a la tabla de símbolos
                            String tipoToken = Gramatica.tiposDeDatos.get(palabra);
                            tokens.add(new Token(palabra, tipoToken, lineaNumero));
                            TablaSimbolos.agregar(palabra, tipoToken, lineaNumero); // Agregar tipo a tabla de símbolos
                            posicion = currentPos;
                        } else if (palabra.matches(Gramatica.idRegex)) {
                            tokens.add(new Token(palabra, "ID", lineaNumero));
                            TablaSimbolos.agregar(palabra, "ID", lineaNumero); // Agregar ID a tabla de símbolos
                            posicion = currentPos;
                        } else if (!palabra.isEmpty()) {
                            erroresLexicos.add("Error léxico en la línea " + lineaNumero + ", posición "
                                    + (startPos + 1) + ": Identificador inválido '" + palabra + "'.");
                            posicion = currentPos;
                        }
                    }
                    continue;
                }

                // Manejar otros tipos de tokens (literales, operadores, etc.)
                Pattern literalPattern = Pattern.compile(Gramatica.literalRegex);
                Matcher matchLiteral = literalPattern.matcher(lineaDepurada.substring(posicion));
                if (matchLiteral.lookingAt()) {
                    String literal = matchLiteral.group(0);
                    tokens.add(new Token(literal, "LITERAL_CAD", lineaNumero));
                    // Generalmente los literales no van a la tabla de símbolos de esta manera
                    posicion += literal.length();
                    continue;
                }

                if (posicion < lineaDepurada.length()) {
                    String currentCharStr = String.valueOf(lineaDepurada.charAt(posicion));
                    if (Gramatica.signosOperadores.containsKey(currentCharStr)) {
                        String tipoToken = Gramatica.signosOperadores.get(currentCharStr);
                        tokens.add(new Token(currentCharStr, tipoToken, lineaNumero));
                        // Opcional: algunos operadores podrían ir a la tabla de símbolos si se desea
                        // TablaSimbolos.agregar(currentCharStr, tipoToken, lineaNumero);
                        posicion++;
                        continue;
                    }

                    Pattern cintPattern = Pattern.compile(Gramatica.cintRegex);
                    Matcher matchCint = cintPattern.matcher(lineaDepurada.substring(posicion));
                    if (matchCint.lookingAt()) { 
                        String numero = matchCint.group(0);
                        tokens.add(new Token(numero, "CINT", lineaNumero));
                        // Generalmente las constantes numéricas no van a la tabla de símbolos de esta manera
                        posicion += numero.length();
                        continue;
                    }

                    Pattern boolPattern = Pattern.compile(Gramatica.boolRegex);
                    Matcher matchBool = boolPattern.matcher(lineaDepurada.substring(posicion));
                    if (matchBool.lookingAt()) { 
                        String booleano = matchBool.group(0);
                        tokens.add(new Token(booleano, "BOOL", lineaNumero));
                        // TablaSimbolos.agregar(booleano, "BOOL", lineaNumero); // Si quieres 'verdadero'/'falso' en la tabla
                        posicion += booleano.length();
                        continue;
                    }
                }

                if (posicion == initialPos && posicion < lineaDepurada.length()) {
                    erroresLexicos.add("Error léxico en la línea " + lineaNumero + ", posición " + (posicion + 1)
                            + ": Carácter no reconocido '" + lineaDepurada.charAt(posicion) + "'");
                    posicion++;
                }
            }
            lineaNumero++;
        }
        return tokens;
    }
}