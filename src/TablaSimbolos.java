// Contenido del archivo TablaSimbolos.java
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; // Usaremos HashMap para evitar duplicados por lexema fácilmente
import java.util.Map;

public class TablaSimbolos {
    // Usaremos una lista para mantener el orden de inserción para la numeración "No"
    private static final List<EntradaTablaSimbolos> entradas = new ArrayList<>();
    // Usaremos un Map para verificar rápidamente si un lexema ya existe y obtener su entrada
    private static final Map<String, EntradaTablaSimbolos> mapaLexemas = new HashMap<>();
    private static int contadorEntradas = 0;

    /**
     * Agrega un nuevo símbolo a la tabla si no existe previamente.
     * @param lexema El lexema del símbolo.
     * @param tipoToken El tipo de token (ej. "ID", "PROG", "TYPE").
     * @param numeroLinea La línea donde se encontró/declaró el símbolo (para la referencia).
     */
    public static void agregar(String lexema, String tipoToken, int numeroLinea) {
        if (!mapaLexemas.containsKey(lexema)) { // Solo agregar si el lexema no existe
            contadorEntradas++;
            EntradaTablaSimbolos nuevaEntrada = new EntradaTablaSimbolos(contadorEntradas, lexema, tipoToken, numeroLinea);
            entradas.add(nuevaEntrada);
            mapaLexemas.put(lexema, nuevaEntrada);
        }
        // Si decides actualizar la referencia (ej. línea) cada vez que aparece,
        // podrías hacerlo aquí incluso si el lexema ya existe. Por ahora, solo primera aparición.
    }

    /**
     * Obtiene todas las entradas de la tabla de símbolos.
     * @return Una lista de objetos EntradaTablaSimbolos.
     */
    public static List<EntradaTablaSimbolos> getEntradas() {
        return entradas;
    }

    /**
     * Limpia la tabla de símbolos para una nueva ejecución.
     */
    public static void limpiar() {
        entradas.clear();
        mapaLexemas.clear();
        contadorEntradas = 0;
    }

    /**
     * Verifica si un símbolo existe en la tabla.
     * @param lexema El lexema del símbolo.
     * @return true si el símbolo existe, false en caso contrario.
     */
    public static boolean existeSimbolo(String lexema) {
        return mapaLexemas.containsKey(lexema);
    }

    /**
     * Agrega un símbolo con un tipo específico a la tabla.
     * @param lexema El lexema del símbolo.
     * @param tipo El tipo del símbolo (ej. "int", "float").
     */
    public static void agregarSimbolo(String lexema, String tipo) {
        if (!mapaLexemas.containsKey(lexema)) {
            contadorEntradas++;
            EntradaTablaSimbolos nuevaEntrada = new EntradaTablaSimbolos(contadorEntradas, lexema, tipo, -1);
            entradas.add(nuevaEntrada);
            mapaLexemas.put(lexema, nuevaEntrada);
        }
    }

    /**
     * Obtiene el tipo de un símbolo.
     * @param lexema El lexema del símbolo.
     * @return El tipo del símbolo, o null si no existe.
     */
    public static String obtenerTipo(String lexema) {
        EntradaTablaSimbolos entrada = mapaLexemas.get(lexema);
        return (entrada != null) ? entrada.getTipo() : null;
    }
}