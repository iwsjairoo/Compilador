// Contenido del archivo Token.java
public class Token {
    private String lexema;
    private String tipo;
    private int linea; // Número de línea donde se encontró el token

    /**
     * Constructor para la clase Token.
     * @param lexema El texto del token.
     * @param tipo El tipo de token (ej. ID, CINT, MAS).
     * @param linea El número de línea donde se encuentra el token en el código fuente.
     */
    public Token(String lexema, String tipo, int linea) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
    }

    public String getLexema() {
        return lexema;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLinea() {
        return linea;
    }

    @Override
    public String toString() {
        return "<" + tipo + ", " + lexema + ", Linea: " + linea + ">";
    }
}