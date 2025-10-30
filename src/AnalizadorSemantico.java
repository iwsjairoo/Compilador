import java.util.List;

public class AnalizadorSemantico {

    private TablaSimbolos tablaSimbolos;

    public AnalizadorSemantico(TablaSimbolos tablaSimbolos) {
        this.tablaSimbolos = tablaSimbolos;
    }

    // Método principal para realizar el análisis semántico
    public void analizar(List<Node> arbolSintactico) throws Exception {
        for (Node nodo : arbolSintactico) {
            verificarNodo(nodo);
        }
    }

    // Verifica un nodo del árbol sintáctico
    private void verificarNodo(Node nodo) throws Exception {
        switch (nodo.getValor()) {
            case "DECLARACION":
                verificarDeclaracion(nodo);
                break;
            case "ASIGNACION":
                verificarAsignacion(nodo);
                break;
            case "OPERACION":
                verificarOperacion(nodo);
                break;
            default:
                throw new Exception("Tipo de nodo desconocido: " + nodo.getValor());
        }

        // Verificar los hijos del nodo
        for (Node hijo : nodo.getHijos()) {
            verificarNodo(hijo);
        }
    }

    // Verifica una declaración de variable
    private void verificarDeclaracion(Node nodo) throws Exception {
        String nombre = nodo.getValor();
        if (tablaSimbolos.existeSimbolo(nombre)) {
            throw new Exception("Error semántico: La variable '" + nombre + "' ya fue declarada.");
        }
        tablaSimbolos.agregarSimbolo(nombre, "tipo_desconocido"); // Ajustar tipo según el lenguaje
    }

    // Verifica una asignación
    private void verificarAsignacion(Node nodo) throws Exception {
        String nombre = nodo.getValor();
        if (!tablaSimbolos.existeSimbolo(nombre)) {
            throw new Exception("Error semántico: La variable '" + nombre + "' no ha sido declarada.");
        }
        // Aquí podrías agregar validación de tipos si es necesario
    }

    // Verifica una operación
    private void verificarOperacion(Node nodo) throws Exception {
        List<Node> operandos = nodo.getHijos();
        if (operandos.size() != 2) {
            throw new Exception("Error semántico: Operación inválida.");
        }
        String tipo1 = tablaSimbolos.obtenerTipo(operandos.get(0).getValor());
        String tipo2 = tablaSimbolos.obtenerTipo(operandos.get(1).getValor());
        if (!tipo1.equals(tipo2)) {
            throw new Exception("Error semántico: Tipos incompatibles en la operación.");
        }
    }
}
