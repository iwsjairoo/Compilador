import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiladorGUI extends JFrame {
    private final JTextField rutaField;
    private final JTextArea salidaArea;
    private final JButton analizarButton;
    private final JButton seleccionarButton;
    private final JTree arbolTree;
    private final DefaultTreeModel arbolModel;

    public CompiladorGUI() {
        super("Proyecto Analizador - Interfaz");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

    rutaField = new JTextField("progftef.txt");
        salidaArea = new JTextArea();
        salidaArea.setEditable(false);
        salidaArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

    DefaultMutableTreeNode raizInicial = new DefaultMutableTreeNode("Árbol no disponible");
    arbolModel = new DefaultTreeModel(raizInicial);
    arbolTree = new JTree(arbolModel);
    arbolTree.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    arbolTree.setRootVisible(true);
    arbolTree.setShowsRootHandles(true);

        analizarButton = new JButton("Analizar");
        analizarButton.addActionListener(this::analizarAction);

        seleccionarButton = new JButton("Seleccionar archivo...");
        seleccionarButton.addActionListener(this::seleccionarArchivoAction);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(new JLabel("Archivo fuente:"), BorderLayout.WEST);
        topPanel.add(rutaField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(seleccionarButton);
        buttonPanel.add(analizarButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(salidaArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Salida"));

        JScrollPane arbolScrollPane = new JScrollPane(arbolTree);
        arbolScrollPane.setBorder(BorderFactory.createTitledBorder("Árbol sintáctico"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, arbolScrollPane, scrollPane);
        splitPane.setResizeWeight(0.35);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    private void seleccionarArchivoAction(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo fuente");
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            rutaField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void analizarAction(ActionEvent event) {
        analizarButton.setEnabled(false);
        seleccionarButton.setEnabled(false);
        salidaArea.setText("Procesando...\n");

        String rutaArchivo = rutaField.getText().trim();
        if (rutaArchivo.isEmpty()) {
            salidaArea.append("\n[Error] Debes indicar la ruta del archivo fuente.\n");
            analizarButton.setEnabled(true);
            seleccionarButton.setEnabled(true);
            return;
        }

        new Thread(() -> {
            ResultadoAnalisis resultado = ejecutarAnalisis(rutaArchivo);
            SwingUtilities.invokeLater(() -> {
                salidaArea.setText(resultado.salida);
                actualizarArbol(resultado.arbol);
                analizarButton.setEnabled(true);
                seleccionarButton.setEnabled(true);
            });
        }).start();
    }

    private static class ResultadoAnalisis {
        private final String salida;
        private final Node arbol;

        private ResultadoAnalisis(String salida, Node arbol) {
            this.salida = salida;
            this.arbol = arbol;
        }
    }

    private ResultadoAnalisis ejecutarAnalisis(String rutaArchivoFuenteStr) {
        StringBuilder salida = new StringBuilder();
        Node arbolParaUI = null;

        try {
            Path rutaArchivoFuente = Paths.get(rutaArchivoFuenteStr);
            if (!Files.exists(rutaArchivoFuente)) {
                salida.append("[Error] El archivo no existe: ").append(rutaArchivoFuenteStr).append('\n');
                return new ResultadoAnalisis(salida.toString(), null);
            }

            TablaSimbolos.limpiar();
            Path directorioBase = rutaArchivoFuente.getParent();
            if (directorioBase == null) {
                directorioBase = Paths.get("").toAbsolutePath();
            }

            Path rutaArchivoTokens = directorioBase.resolve("progfte.tok");
            Path rutaArchivoTablaSimbolos = directorioBase.resolve("progfte.tab");
            Path rutaArchivoArbolSintactico = directorioBase.resolve("progfte.arbol");
            Path rutaArchivoASM = directorioBase.resolve("progfte.asm");

            String codigoFuente;
            try {
                codigoFuente = Files.readString(rutaArchivoFuente);
            } catch (IOException e) {
                salida.append("[Error crítico] No se pudo leer el archivo fuente: ").append(e.getMessage()).append('\n');
                return new ResultadoAnalisis(salida.toString(), null);
            }

            salida.append("--- Contenido del archivo ").append(rutaArchivoFuente.getFileName()).append(" ---\n");
            salida.append(codigoFuente).append('\n');
            salida.append("-------------------------------------\n\n");

            List<String> erroresLexicos = new ArrayList<>();
            List<Token> tokens = AnalizadorLexico.analizarLexicamente(codigoFuente, erroresLexicos);

            if (!erroresLexicos.isEmpty()) {
                salida.append("--- Errores Léxicos Encontrados ---\n");
                for (String err : erroresLexicos) {
                    salida.append(err).append('\n');
                }
                salida.append("-------------------------------------\n\n");
                GeneradorArchivos.generarArchivoTokens(rutaArchivoTokens, tokens);
                salida.append("Se generó el archivo de tokens a pesar de los errores léxicos.\n");
                return new ResultadoAnalisis(salida.toString(), null);
            }

            salida.append("Análisis léxico exitoso. Se generó progfte.tok y progfte.tab.\n");
            GeneradorArchivos.generarArchivoTokens(rutaArchivoTokens, tokens);
            GeneradorArchivos.generarArchivoTablaSimbolos(rutaArchivoTablaSimbolos, TablaSimbolos.getEntradas());

            AnalizadorSintactico analizadorSintactico = new AnalizadorSintactico(tokens);
            Node arbolSintactico = analizadorSintactico.analizar();
            List<String> erroresSintacticos = analizadorSintactico.getErroresSintacticos();
            boolean sintaxisCorrecta = arbolSintactico != null && erroresSintacticos.isEmpty();

            if (!erroresSintacticos.isEmpty()) {
                salida.append("--- Errores Sintácticos Encontrados ---\n");
                for (String err : erroresSintacticos) {
                    salida.append(err).append('\n');
                }
                salida.append("-------------------------------------\n\n");
            }

            if (sintaxisCorrecta) {
                salida.append("Expresión válida. El análisis sintáctico fue exitoso.\n");
                GeneradorArchivos.generarArbolSintactico(rutaArchivoArbolSintactico, arbolSintactico);
                salida.append("Árbol de sintaxis generado en: ").append(rutaArchivoArbolSintactico.toAbsolutePath()).append('\n');
                arbolParaUI = arbolSintactico;

                salida.append("\nNotación Polaca Inversa (NPI):\n");
                salida.append(analizadorSintactico.getPolacaInversa()).append('\n');

                salida.append("\nTriplos:\n");
                analizadorSintactico.getTriplos().forEach(triplo -> salida.append(triplo).append('\n'));

                try {
                    Map<String, Integer> valores = new HashMap<>();
                    int resultado = analizadorSintactico.ejecutarNPI(valores);
                    salida.append("\nResultado de la expresión: ").append(resultado).append('\n');
                } catch (Exception e) {
                    salida.append("\nError al evaluar la expresión: ").append(e.getMessage()).append('\n');
                }

                GeneradorArchivos.generarCodigoEnsamblador(rutaArchivoASM, analizadorSintactico.getPolacaInversa());
                salida.append("Código ensamblador generado en: ").append(rutaArchivoASM.toAbsolutePath()).append('\n');
            } else {
                salida.append("Expresión inválida. Se encontraron errores léxicos o sintácticos.\n");
            }

            salida.append("\nArchivos de salida generados en: ").append(directorioBase.toAbsolutePath()).append('\n');
        } catch (Exception ex) {
            salida.append("[Error inesperado] ").append(ex.getMessage()).append('\n');
        }

        return new ResultadoAnalisis(salida.toString(), arbolParaUI);
    }

    private void actualizarArbol(Node raiz) {
        DefaultMutableTreeNode raizArbol = (raiz != null)
                ? construirNodo(raiz)
                : new DefaultMutableTreeNode("Árbol no disponible");
        arbolModel.setRoot(raizArbol);
        arbolModel.reload();
        for (int i = 0; i < arbolTree.getRowCount(); i++) {
            arbolTree.expandRow(i);
        }
    }

    private DefaultMutableTreeNode construirNodo(Node nodo) {
        if (nodo == null) {
            return new DefaultMutableTreeNode("(nulo)");
        }
        DefaultMutableTreeNode nodoActual = new DefaultMutableTreeNode(nodo.getValor());
        for (Node hijo : nodo.getHijos()) {
            nodoActual.add(construirNodo(hijo));
        }
        return nodoActual;
    }
}
