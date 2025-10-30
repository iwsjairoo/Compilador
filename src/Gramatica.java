import java.util.HashMap;
import java.util.Map;

/**
 * Clase que contiene las definiciones de los elementos léxicos del lenguaje.
 * Define las palabras reservadas, tipos de datos, signos de operadores y las expresiones regulares para identificadores,
 * constantes enteras, constantes de cadena y booleanos.
 */
public class Gramatica {
    /**
     * Mapa que almacena las palabras reservadas del lenguaje y su correspondiente token.
     * La clave es la palabra reservada (String) y el valor es el tipo de token (String).
     */
    public static final Map<String, String> palabrasReservadas = new HashMap<>();

    /**
     * Mapa que almacena los tipos de datos del lenguaje y su correspondiente token.
     * La clave es el tipo de dato (String) y el valor es el tipo de token (String).
     */
    public static final Map<String, String> tiposDeDatos = new HashMap<>();

    /**
     * Mapa que almacena los signos de operadores del lenguaje y su correspondiente token.
     * La clave es el signo del operador (String) y el valor es el tipo de token (String).
     */
    public static final Map<String, String> signosOperadores = new HashMap<>();

    /**
     * Expresión regular para identificar identificadores válidos.
     * Un identificador debe comenzar con una letra (mayúscula o minúscula) seguida de cero o más letras o dígitos.
     */
    public static final String idRegex = "[a-zA-Z]([a-zA-Z0-9]*)";

    /**
     * Expresión regular para identificar constantes enteras.
     * Una constante entera está compuesta por uno o más dígitos.
     */
    public static final String cintRegex = "\\d+";

    /** 
     * Expresión regular para identificar literales de cadena.
     * Un literal de cadena está encerrado entre comillas dobles ("), y puede contener cualquier carácter entre ellas (incluso vacío).
     * El uso de `(.*?)` asegura una coincidencia no codiciosa.
     */
    public static final String literalRegex = "\"(.*?)\"";

    /**
     * Expresión regular para identificar valores booleanos.
     * Los valores booleanos válidos son "verdadero" o "falso".
     */
    public static final String boolRegex = "(verdadero|falso)";

    /**
     * Bloque estático que se ejecuta una sola vez cuando la clase `Gramatica` es cargada en memoria.
     * Se utiliza para inicializar los mapas de palabras reservadas, tipos de datos y signos de operadores.
     */
    static {
        // Inicialización del mapa de palabras reservadas.
        palabrasReservadas.put("inicio", "INICIO");
        palabrasReservadas.put("pf2024", "PROG");
        palabrasReservadas.put("decl", "DECL");
        palabrasReservadas.put("impdig", "IMPDIG");
        palabrasReservadas.put("impcad", "IMPCAD");
        palabrasReservadas.put("leerdig", "LEERDIG");
        palabrasReservadas.put("end", "END");

        // Inicialización del mapa de tipos de datos.
        tiposDeDatos.put("int", "TYPE");
        tiposDeDatos.put("cad", "TYPE");
        tiposDeDatos.put("booleano", "TYPE");

        // Inicialización del mapa de signos de operadores.
        signosOperadores.put(";", "PC");
        signosOperadores.put(":", "ASIG");
        signosOperadores.put("+", "MAS");
        signosOperadores.put("-", "MENOS");
        signosOperadores.put("/", "DIV");
        signosOperadores.put("*", "MUL");
        signosOperadores.put("(", "PAREN");
        signosOperadores.put(")", "TESIS");
        signosOperadores.put("=", "IGUAL");
        signosOperadores.put(",", "COMA");
    }
}