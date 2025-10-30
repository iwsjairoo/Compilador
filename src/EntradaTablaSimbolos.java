// Contenido del archivo EntradaTablaSimbolos.java
public class EntradaTablaSimbolos {
    private int numero;
    private String lexema;
    private String token; // Tipo de token (ej. "ID", "PROG", "TYPE")
    private int referencia; // Podría ser el número de línea de la primera aparición/declaración

    public EntradaTablaSimbolos(int numero, String lexema, String token, int referencia) {
        this.numero = numero;
        this.lexema = lexema;
        this.token = token;
        this.referencia = referencia;
    }

    public int getNumero() {
        return numero;
    }

    public String getLexema() {
        return lexema;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return token;
    }

    public int getReferencia() {
        return referencia;
    }

    @Override
    public String toString() {
        // Formato para el archivo .tab, ajusta el espaciado según necesites
        return String.format("%-5d %-20s %-10s %-5d", numero, lexema, token, referencia);
    }
}