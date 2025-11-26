import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiladorGUI extends JFrame {
    private final JTextField rutaField;
    private final JTextArea editorArea;
    private final JTextArea salidaArea;
    private final JTextArea npiArea;
    private final JTextArea asmArea;
    private final JButton analizarButton;
    private final JButton seleccionarButton;
    private final JButton guardarButton;
    private final JButton recargarButton;
    private final JTree arbolTree;
    private final DefaultTreeModel arbolModel;
    private final DefaultTableModel tokensModel;
    private final DefaultTableModel simbolosModel;
    private final DefaultTableModel triplosModel;
    private Path archivoActual;

    public CompiladorGUI() {
        super("Proyecto Analizador - Interfaz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        rutaField = new JTextField("progftef.txt");
        editorArea = new JTextArea();
        editorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        salidaArea = new JTextArea();
        salidaArea.setEditable(false);
        salidaArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        npiArea = new JTextArea();
        npiArea.setEditable(false);
        npiArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        asmArea = new JTextArea();
        asmArea.setEditable(false);
        asmArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        tokensModel = new DefaultTableModel(new Object[] {"Lexema", "Tipo", "Línea"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tokensTable = new JTable(tokensModel);
        tokensTable.setFillsViewportHeight(true);

        simbolosModel = new DefaultTableModel(new Object[] {"No", "Lexema", "Token", "Referencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable simbolosTable = new JTable(simbolosModel);
        simbolosTable.setFillsViewportHeight(true);

        triplosModel = new DefaultTableModel(new Object[] {"Operador", "Arg1", "Arg2", "Resultado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable triplosTable = new JTable(triplosModel);
        triplosTable.setFillsViewportHeight(true);

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

        guardarButton = new JButton("Guardar archivo");
        guardarButton.addActionListener(this::guardarArchivoAction);

        recargarButton = new JButton("Recargar");
        recargarButton.addActionListener(this::recargarArchivoAction);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(new JLabel("Archivo fuente:"), BorderLayout.WEST);
        topPanel.add(rutaField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(seleccionarButton);
        buttonPanel.add(recargarButton);
        buttonPanel.add(guardarButton);
        buttonPanel.add(analizarButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        JScrollPane editorScrollPane = new JScrollPane(editorArea);
        editorScrollPane.setBorder(BorderFactory.createTitledBorder("Editor de código fuente"));

        JScrollPane arbolScrollPane = new JScrollPane(arbolTree);
        arbolScrollPane.setBorder(BorderFactory.createTitledBorder("Árbol sintáctico"));

        JTabbedPane resultadosTabs = new JTabbedPane();
        resultadosTabs.addTab("Mensajes", crearScrollMonospaced(salidaArea, "Detalle de ejecución"));
        resultadosTabs.addTab("Tokens", new JScrollPane(tokensTable));
        resultadosTabs.addTab("Tabla de símbolos", new JScrollPane(simbolosTable));
        resultadosTabs.addTab("NPI", crearScrollMonospaced(npiArea, "Notación Polaca Inversa"));
        resultadosTabs.addTab("Triplos", new JScrollPane(triplosTable));
        resultadosTabs.addTab("Código ensamblador", crearScrollMonospaced(asmArea, "ASM generado"));

        JSplitPane inferiorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, arbolScrollPane, resultadosTabs);
        inferiorSplitPane.setResizeWeight(0.35);

        JSplitPane principalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScrollPane, inferiorSplitPane);
        principalSplitPane.setResizeWeight(0.4);

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(principalSplitPane, BorderLayout.CENTER);

        intentarCargarArchivoPorDefecto();
    }

    private JScrollPane crearScrollMonospaced(JTextArea area, String titulo) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(titulo));
        return scroll;
    }

    private void intentarCargarArchivoPorDefecto() {
        Path posible = Paths.get(rutaField.getText().trim());
        if (Files.exists(posible)) {
            cargarArchivo(posible);
        }
    }

    private void seleccionarArchivoAction(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo fuente");
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            cargarArchivo(chooser.getSelectedFile().toPath());
        }
    }

    private void recargarArchivoAction(ActionEvent event) {
        if (archivoActual == null) {
            mostrarError("No hay un archivo asociado para recargar.");
            return;
        }
        if (!Files.exists(archivoActual)) {
            mostrarError("El archivo ya no existe en disco: " + archivoActual);
            return;
        }
        cargarArchivo(archivoActual);
    }

    private void guardarArchivoAction(ActionEvent event) {
        Path destino = archivoActual;
        if (destino == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar archivo fuente");
            int resultado = chooser.showSaveDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                destino = chooser.getSelectedFile().toPath();
            } else {
                return;
            }
        }

        try {
            Files.writeString(destino, editorArea.getText());
            archivoActual = destino.toAbsolutePath();
            rutaField.setText(archivoActual.toString());
            salidaArea.append("Archivo guardado en: " + archivoActual + System.lineSeparator());
        } catch (IOException ex) {
            mostrarError("No se pudo guardar el archivo: " + ex.getMessage());
        }
    }

    private void cargarArchivo(Path ruta) {
        try {
            String contenido = Files.readString(ruta);
            archivoActual = ruta.toAbsolutePath();
            rutaField.setText(archivoActual.toString());
            editorArea.setText(contenido);
            editorArea.setCaretPosition(0);
            salidaArea.append("Archivo cargado: " + archivoActual + System.lineSeparator());
        } catch (IOException ex) {
            mostrarError("No se pudo leer el archivo: " + ex.getMessage());
        }
    }

    private void analizarAction(ActionEvent event) {
        analizarButton.setEnabled(false);
        seleccionarButton.setEnabled(false);
        guardarButton.setEnabled(false);
        recargarButton.setEnabled(false);
        salidaArea.setText("Procesando...\n");

        String rutaArchivo = rutaField.getText().trim();
        String codigoFuente = editorArea.getText();

        new Thread(() -> {
            ResultadoAnalisis resultado = ejecutarAnalisis(rutaArchivo, codigoFuente);
            SwingUtilities.invokeLater(() -> {
                salidaArea.setText(resultado.salida);
                salidaArea.setCaretPosition(0);
                actualizarArbol(resultado.arbol);
                actualizarTokens(resultado.tokens);
                actualizarTablaSimbolos(resultado.simbolos);
                actualizarNpi(resultado.npi);
                actualizarTriplos(resultado.triplos);
                actualizarCodigoAsm(resultado.codigoAsm);
                analizarButton.setEnabled(true);
                seleccionarButton.setEnabled(true);
                guardarButton.setEnabled(true);
                recargarButton.setEnabled(true);
            });
        }).start();
    }

    private static class ResultadoAnalisis {
        private final String salida;
        private final Node arbol;
        private final List<Token> tokens;
        private final List<EntradaTablaSimbolos> simbolos;
        private final List<String> npi;
        private final List<AnalizadorSintactico.Triplo> triplos;
        private final String codigoAsm;

        private ResultadoAnalisis(String salida,
                                  Node arbol,
                                  List<Token> tokens,
                                  List<EntradaTablaSimbolos> simbolos,
                                  List<String> npi,
                                  List<AnalizadorSintactico.Triplo> triplos,
                                  String codigoAsm) {
            this.salida = salida;
            this.arbol = arbol;
            this.tokens = tokens;
            this.simbolos = simbolos;
            this.npi = npi;
            this.triplos = triplos;
            this.codigoAsm = codigoAsm;
        }
    }

    private ResultadoAnalisis ejecutarAnalisis(String rutaArchivoFuenteStr, String codigoFuenteEditor) {
        StringBuilder salida = new StringBuilder();
        Node arbolParaUI = null;
        List<Token> tokensUI = new ArrayList<>();
        List<EntradaTablaSimbolos> simbolosUI = new ArrayList<>();
        List<String> polacaUI = new ArrayList<>();
        List<AnalizadorSintactico.Triplo> triplosUI = new ArrayList<>();
        String codigoAsm = null;

        try {
            Path rutaArchivoFuente = null;
            if (rutaArchivoFuenteStr != null && !rutaArchivoFuenteStr.isBlank()) {
                try {
                    rutaArchivoFuente = Paths.get(rutaArchivoFuenteStr).toAbsolutePath();
                } catch (InvalidPathException ex) {
                    salida.append("[Error] La ruta indicada no es válida: ").append(rutaArchivoFuenteStr).append('\n');
                }
            }

            String codigoFuente = (codigoFuenteEditor != null) ? codigoFuenteEditor : "";

            if (rutaArchivoFuente != null) {
                salida.append("Archivo seleccionado: ").append(rutaArchivoFuente).append('\n');
            }
            salida.append("Longitud del código analizado: ").append(codigoFuente.length()).append(" caracteres\n\n");

            Path directorioBase = (rutaArchivoFuente != null && rutaArchivoFuente.getParent() != null)
                    ? rutaArchivoFuente.getParent()
                    : Paths.get("").toAbsolutePath();
            String nombreBase = (rutaArchivoFuente != null) ? obtenerNombreBase(rutaArchivoFuente) : "salida";

            Path rutaArchivoTokens = directorioBase.resolve(nombreBase + ".tok");
            Path rutaArchivoTablaSimbolos = directorioBase.resolve(nombreBase + ".tab");
            Path rutaArchivoArbolSintactico = directorioBase.resolve(nombreBase + ".arbol");
            Path rutaArchivoAsm = directorioBase.resolve(nombreBase + ".asm");
            Path rutaArchivoAsmDesdeArbol = directorioBase.resolve(nombreBase + "_arbol.asm");

            TablaSimbolos.limpiar();
            List<String> erroresLexicos = new ArrayList<>();
            List<Token> tokens = AnalizadorLexico.analizarLexicamente(codigoFuente, erroresLexicos);
            tokensUI = new ArrayList<>(tokens);
            simbolosUI = new ArrayList<>(TablaSimbolos.getEntradas());

            GeneradorArchivos.generarArchivoTokens(rutaArchivoTokens, tokens);
            GeneradorArchivos.generarArchivoTablaSimbolos(rutaArchivoTablaSimbolos, TablaSimbolos.getEntradas());

            if (!erroresLexicos.isEmpty()) {
                salida.append("--- Errores Léxicos Encontrados ---\n");
                for (String err : erroresLexicos) {
                    salida.append(err).append('\n');
                }
                salida.append("-------------------------------------\n\n");
                salida.append("Se generó el archivo de tokens en: ").append(rutaArchivoTokens.toAbsolutePath()).append('\n');
                return new ResultadoAnalisis(salida.toString(), null, tokensUI, simbolosUI, polacaUI, triplosUI, codigoAsm);
            }

            salida.append("Análisis léxico exitoso. Se generaron ")
                    .append(rutaArchivoTokens.getFileName()).append(" y ")
                    .append(rutaArchivoTablaSimbolos.getFileName()).append('.').append('\n');

            AnalizadorSintactico analizadorSintactico = new AnalizadorSintactico(tokens);
            Node arbolSintactico = analizadorSintactico.analizar();
            List<String> erroresSintacticos = analizadorSintactico.getErroresSintacticos();

            if (!erroresSintacticos.isEmpty()) {
                salida.append("--- Errores Sintácticos Encontrados ---\n");
                for (String err : erroresSintacticos) {
                    salida.append(err).append('\n');
                }
                salida.append("-------------------------------------\n\n");
            }

            boolean sintaxisCorrecta = arbolSintactico != null && erroresSintacticos.isEmpty();
            if (sintaxisCorrecta) {
                arbolParaUI = arbolSintactico;
                GeneradorArchivos.generarArbolSintactico(rutaArchivoArbolSintactico, arbolSintactico);
                salida.append("Árbol de sintaxis generado en: ").append(rutaArchivoArbolSintactico.toAbsolutePath()).append('\n');

                polacaUI = new ArrayList<>(analizadorSintactico.getPolacaInversa());
                if (!polacaUI.isEmpty()) {
                    salida.append("\nNotación Polaca Inversa (NPI):\n");
                    salida.append(String.join(" ", polacaUI)).append('\n');
                }

                triplosUI = new ArrayList<>(analizadorSintactico.getTriplos());
                if (!triplosUI.isEmpty()) {
                    salida.append("\nTriplos:\n");
                    for (AnalizadorSintactico.Triplo triplo : triplosUI) {
                        salida.append(triplo).append('\n');
                    }
                }

                try {
                    Map<String, Integer> valores = new HashMap<>();
                    int resultado = analizadorSintactico.ejecutarNPI(valores);
                    salida.append("\nResultado de la expresión: ").append(resultado).append('\n');
                } catch (Exception e) {
                    salida.append("\nError al evaluar la expresión: ").append(e.getMessage()).append('\n');
                }

                codigoAsm = GeneradorArchivos.construirCodigoEnsamblador(polacaUI);
                GeneradorArchivos.generarCodigoEnsamblador(rutaArchivoAsm, polacaUI);
                salida.append("Código ensamblador generado en: ").append(rutaArchivoAsm.toAbsolutePath()).append('\n');

                if (arbolSintactico != null) {
                    GeneradorArchivos.generarCodigoDesdeArbol(rutaArchivoAsmDesdeArbol, arbolSintactico);
                    salida.append("Código ensamblador generado desde el árbol sintáctico en: ")
                            .append(rutaArchivoAsmDesdeArbol.toAbsolutePath()).append('\n');
                }
            } else {
                salida.append("Expresión inválida. Se encontraron errores léxicos o sintácticos.\n");
            }

            salida.append("\nArchivos de salida generados en: ").append(directorioBase.toAbsolutePath()).append('\n');
            return new ResultadoAnalisis(salida.toString(), arbolParaUI, tokensUI, simbolosUI, polacaUI, triplosUI, codigoAsm);
        } catch (Exception ex) {
            salida.append("[Error inesperado] ").append(ex.getMessage()).append('\n');
            return new ResultadoAnalisis(salida.toString(), arbolParaUI, tokensUI, simbolosUI, polacaUI, triplosUI, codigoAsm);
        }
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

    private void actualizarTokens(List<Token> tokens) {
        tokensModel.setRowCount(0);
        if (tokens == null) {
            return;
        }
        for (Token token : tokens) {
            tokensModel.addRow(new Object[] {token.getLexema(), token.getTipo(), token.getLinea()});
        }
    }

    private void actualizarTablaSimbolos(List<EntradaTablaSimbolos> entradas) {
        simbolosModel.setRowCount(0);
        if (entradas == null) {
            return;
        }
        for (EntradaTablaSimbolos entrada : entradas) {
            simbolosModel.addRow(new Object[] {
                    entrada.getNumero(),
                    entrada.getLexema(),
                    entrada.getToken(),
                    entrada.getReferencia()
            });
        }
    }

    private void actualizarNpi(List<String> polaca) {
        if (polaca == null || polaca.isEmpty()) {
            npiArea.setText("Sin datos disponibles.");
        } else {
            npiArea.setText(String.join(" ", polaca));
            npiArea.setCaretPosition(0);
        }
    }

    private void actualizarTriplos(List<AnalizadorSintactico.Triplo> triplos) {
        triplosModel.setRowCount(0);
        if (triplos == null) {
            return;
        }
        for (AnalizadorSintactico.Triplo triplo : triplos) {
            triplosModel.addRow(new Object[] {
                    triplo.getOperador(),
                    triplo.getArgumento1(),
                    triplo.getArgumento2(),
                    triplo.getResultado()
            });
        }
    }

    private void actualizarCodigoAsm(String codigoAsmGenerado) {
        if (codigoAsmGenerado == null || codigoAsmGenerado.isBlank()) {
            asmArea.setText("Sin datos disponibles.");
        } else {
            asmArea.setText(codigoAsmGenerado);
            asmArea.setCaretPosition(0);
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String obtenerNombreBase(Path rutaArchivoFuente) {
        String nombreArchivo = rutaArchivoFuente.getFileName().toString();
        int indice = nombreArchivo.lastIndexOf('.');
        if (indice > 0) {
            return nombreArchivo.substring(0, indice);
        }
        return nombreArchivo;
    }
}
