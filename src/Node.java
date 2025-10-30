import java.util.ArrayList;
import java.util.List;

public class Node {
    private final String valor; 
    private final List<Node> hijos;

    public Node(String valor) {
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(Node hijo) {
        this.hijos.add(hijo);
    }

    public String getValor() {
        return valor;
    }

    public List<Node> getHijos() {
        return hijos;
    }
}