import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class AnalizadorSintactico {
    private final List<Token> tokens;
    private int indice = 0;
    private final List<String> erroresSintacticos;
    private boolean error = false;
    private final List<String> polacaInversa = new ArrayList<>();
    private final List<Triplo> triplos = new ArrayList<>();

    public static class Triplo {
        private final String operador;
        private final String argumento1;
        private final String argumento2;
        private final String resultado;

        public Triplo(String operador, String argumento1, String argumento2, String resultado) {
            this.operador = operador;
            this.argumento1 = argumento1;
            this.argumento2 = argumento2;
            this.resultado = resultado;
        }

        @Override
        public String toString() {
            return "(" + operador + ", " + argumento1 + ", " + argumento2 + ", " + resultado + ")";
        }
    }

    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
        this.erroresSintacticos = new ArrayList<>();
    }

    public Node analizar() {
        indice = 0;
        error = false;
        erroresSintacticos.clear();
        polacaInversa.clear();
        triplos.clear();

        if (tokens.isEmpty()) {
            reportarErrorBase("No hay tokens para analizar. La expresión está vacía.");
            return null;
        }

        Node raiz = E();

        if (!this.error && indice < tokens.size()) {
            Token tokenSobrante = tokens.get(indice);
            reportarErrorConToken("Caracteres extra o token inesperado después de una expresión válida", tokenSobrante);
        }

        if (!this.error && raiz != null) {
            generarCodigoIntermedio();
        }

        return !this.error ? raiz : null;
    }

    public List<String> getErroresSintacticos() {
        return erroresSintacticos;
    }

    public List<String> getPolacaInversa() {
        return new ArrayList<>(polacaInversa);
    }

    public List<Triplo> getTriplos() {
        return new ArrayList<>(triplos);
    }

    public int ejecutarNPI(Map<String, Integer> valores) {
        if (polacaInversa.isEmpty()) {
            throw new IllegalStateException("La notación polaca inversa aún no ha sido generada.");
        }
        Deque<Integer> pila = new ArrayDeque<>();
        for (String token : polacaInversa) {
            if (token == null || token.isBlank()) {
                continue;
            }
            if (esOperador(token)) {
                if (pila.size() < 2) {
                    throw new IllegalStateException("Expresión postfix inválida: no hay suficientes operandos para el operador '" + token + "'.");
                }
                int b = pila.pop();
                int a = pila.pop();
                int resultado;
                switch (token) {
                    case "+":
                        resultado = a + b;
                        break;
                    case "-":
                        resultado = a - b;
                        break;
                    case "*":
                        resultado = a * b;
                        break;
                    case "/":
                        if (b == 0) {
                            throw new ArithmeticException("División entre cero en la evaluación de la expresión.");
                        }
                        resultado = a / b;
                        break;
                    default:
                        throw new IllegalStateException("Operador no soportado: " + token);
                }
                pila.push(resultado);
            } else if (token.matches("[0-9]+")) {
                pila.push(Integer.parseInt(token));
            } else {
                if (valores == null || !valores.containsKey(token)) {
                    throw new IllegalArgumentException("No se proporcionó un valor para la variable '" + token + "'.");
                }
                pila.push(valores.get(token));
            }
        }
        if (pila.size() != 1) {
            throw new IllegalStateException("La evaluación de la NPI no produjo un único resultado.");
        }
        return pila.pop();
    }

    private void generarCodigoIntermedio() {
        List<String> postfix = convertirTokensAPostfijo();
        polacaInversa.clear();
        polacaInversa.addAll(postfix);
        triplos.clear();
        generarTriplosDesdePostfijo(postfix);
    }

    private List<String> convertirTokensAPostfijo() {
        List<String> salida = new ArrayList<>();
        Deque<String> operadores = new ArrayDeque<>();

        for (Token token : tokens) {
            String lexema = token.getLexema();
            String tipo = token.getTipo();

            if (tipo != null && (tipo.equals("CINT") || tipo.equals("ID"))) {
                salida.add(lexema);
            } else if ("(".equals(lexema)) {
                operadores.push(lexema);
            } else if (")".equals(lexema)) {
                while (!operadores.isEmpty() && !operadores.peek().equals("(")) {
                    salida.add(operadores.pop());
                }
                operadores.pop(); // Sacar el '('
            } else if (esOperador(lexema)) {
                while (!operadores.isEmpty() && esOperador(operadores.peek())
                        && precedencia(operadores.peek()) >= precedencia(lexema)) {
                    salida.add(operadores.pop());
                }
                operadores.push(lexema);
            }
        }

        while (!operadores.isEmpty()) {
            salida.add(operadores.pop());
        }

        return salida;
    }

    private void generarTriplosDesdePostfijo(List<String> postfix) {
        Deque<String> pila = new ArrayDeque<>();
        int contadorTemporales = 1;

        for (String token : postfix) {
            if (esOperador(token)) {
                String operandoDerecho = pila.pop();
                String operandoIzquierdo = pila.pop();
                String temporal = "t" + contadorTemporales++;
                triplos.add(new Triplo(token, operandoIzquierdo, operandoDerecho, temporal));
                pila.push(temporal);
            } else {
                pila.push(token);
            }
        }
    }

    private boolean esOperador(String lexema) {
        return "+".equals(lexema) || "-".equals(lexema) || "*".equals(lexema) || "/".equals(lexema);
    }

    private int precedencia(String operador) {
        switch (operador) {
            case "*":
            case "/":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return 0;
        }
    }

    private Token actual() {
        return (indice < tokens.size()) ? tokens.get(indice) : null;
    }

    private Token anterior() {
        return (indice > 0 && (indice - 1) < tokens.size()) ? tokens.get(indice - 1) : null;
    }

    private void reportarErrorConToken(String mensajeBase, Token tokenContexto) {
        int lineaError = (tokenContexto != null) ? tokenContexto.getLinea() : 1;
        String infoContextoAdicional = (tokenContexto != null) ? ". Se encontró '" + tokenContexto.getLexema() + "'" : ". Expresión finalizada inesperadamente.";
        String mensajeCompleto = "Error sintáctico en línea " + lineaError + ": " + mensajeBase + infoContextoAdicional;
        if (!erroresSintacticos.contains(mensajeCompleto)) {
            erroresSintacticos.add(mensajeCompleto);
        }
        this.error = true;
    }
    
    private void reportarErrorBase(String mensajeBase) {
        int lineaError = (anterior() != null) ? anterior().getLinea() : (actual() != null ? actual().getLinea() : 1);
        String infoContexto = (anterior() != null) ? " después de '" + anterior().getLexema() + "'" : "";
        if (actual() == null && indice >= tokens.size()){ 
             infoContexto += ". Se esperaba continuación pero finalizó la expresión.";
        }
        String mensajeCompleto = "Error sintáctico en línea " + lineaError + ": " + mensajeBase + infoContexto;
        if (!erroresSintacticos.contains(mensajeCompleto)) {
            erroresSintacticos.add(mensajeCompleto);
        }
        this.error = true;
    }

    // E -> T E'
    private Node E() {
        if (this.error) return null;
        Node nodoE = new Node("E");
        Node hijoT = T();
        if (this.error) return null;
        nodoE.agregarHijo(hijoT);
        Node hijoEp = Ep();
        if (this.error) return null;
        if (hijoEp != null) {
            nodoE.agregarHijo(hijoEp);
        }
        return nodoE;
    }

    // E' -> + T E' | - T E' | ε
    private Node Ep() {
        if (this.error) return null;
        Token tokenActual = actual();
        if (tokenActual != null && (tokenActual.getLexema().equals("+") || tokenActual.getLexema().equals("-"))) {
            Node nodoEp = new Node("E'");
            nodoEp.agregarHijo(new Node(tokenActual.getLexema()));
            indice++;
            Token siguienteToken = actual();
            if (siguienteToken == null || (!siguienteToken.getTipo().equals("ID") && !siguienteToken.getTipo().equals("CINT") && !siguienteToken.getLexema().equals("("))) {
                reportarErrorBase("Operando esperado después de '" + tokenActual.getLexema() + "'");
                return null;
            }
            Node hijoT = T();
            if (this.error) return null;
            nodoEp.agregarHijo(hijoT);
            Node hijoEp2 = Ep();
            if (this.error) return null;
            if (hijoEp2.getValor().equals("ε")) {
                nodoEp.agregarHijo(hijoEp2);
            } else {
                nodoEp.agregarHijo(hijoEp2);
            }
            return nodoEp;
        } else {
            return new Node("ε");
        }
    }

    // T -> F T'
    private Node T() {
        if (this.error) return null;
        Node nodoT = new Node("T");
        Node hijoF = F();
        if (this.error) return null;
        nodoT.agregarHijo(hijoF);
        Node hijoTp = Tp();
        if (this.error) return null;
        if (hijoTp != null) {
            nodoT.agregarHijo(hijoTp);
        }
        return nodoT;
    }

    // T' -> * F T' | / F T' | ε
    private Node Tp() {
        if (this.error) return null;
        Token tokenActual = actual();
        if (tokenActual != null && (tokenActual.getLexema().equals("*") || tokenActual.getLexema().equals("/"))) {
            Node nodoTp = new Node("T'");
            nodoTp.agregarHijo(new Node(tokenActual.getLexema()));
            indice++;
            Token siguienteToken = actual();
            if (siguienteToken == null || (!siguienteToken.getTipo().equals("ID") && !siguienteToken.getTipo().equals("CINT") && !siguienteToken.getLexema().equals("("))) {
                reportarErrorBase("Operando esperado después de '" + tokenActual.getLexema() + "'");
                return null;
            }
            Node hijoF = F();
            if (this.error) return null;
            nodoTp.agregarHijo(hijoF);
            Node hijoTp2 = Tp();
            if (this.error) return null;
            if (hijoTp2.getValor().equals("ε")) {
                nodoTp.agregarHijo(hijoTp2);
            } else {
                nodoTp.agregarHijo(hijoTp2);
            }
            return nodoTp;
        } else {
            return new Node("ε");
        }
    }

    // F -> (E) | id | num
    private Node F() {
        if (this.error) return null;
        Token tokenActual = actual();
        Node nodoF = new Node("F");
        if (tokenActual == null) { 
            reportarErrorBase("Se esperaba un operando (ID, número o expresión entre paréntesis)");
            return null;
        }

        if (tokenActual.getLexema().equals("(")) { 
            nodoF.agregarHijo(new Node("("));
            indice++;
            Node hijoE = E();
            if (this.error) return null;
            nodoF.agregarHijo(hijoE);
            
            Token tokenCierre = actual();
            if (tokenCierre != null && tokenCierre.getLexema().equals(")")) {
                nodoF.agregarHijo(new Node(")"));
                indice++;
            } else {
                reportarErrorConToken("Se esperaba un paréntesis de cierre ')'", tokenCierre);
                return null;
            }
        } else if (tokenActual.getTipo().equals("ID") || tokenActual.getTipo().equals("CINT")) { 
            nodoF.agregarHijo(new Node(tokenActual.getLexema()));
            indice++;
        } else {
            reportarErrorConToken("Se esperaba un operando (ID, número o '(')", tokenActual);
            return null;
        }
        return nodoF;
    }
}