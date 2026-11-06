package view;

import dao.AccionDAO;
import dao.SupervisionDAO;
import dao.SupervisionException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Usuario;
import util.Sesion;

/**
 * Pantalla 14 – Matriz de seguimiento.
 * Supervisiones en INFORME; al generar la matriz se muestran los hallazgos con su
 * contramedida, responsable y fecha, y la supervisión pasa a SEGUIMIENTO.
 */
public class FrmMatrizSeguimiento extends JInternalFrame {

    private static final String[] COLUMNAS_SUPERVISIONES = {"ID", "Oficina", "Fecha informe", "Estado"};
    private static final String[] COLUMNAS_MATRIZ = {"Hallazgo", "Descripción", "Acción inmediata",
        "Causa raíz", "Contramedida", "Responsable", "Fecha compromiso"};

    private final DefaultTableModel modeloSupervisiones = crearModelo(COLUMNAS_SUPERVISIONES);
    private final DefaultTableModel modeloMatriz = crearModelo(COLUMNAS_MATRIZ);
    private final JTable tblSupervisiones = new JTable(modeloSupervisiones);
    private final JTable tblMatriz = new JTable(modeloMatriz);
    private final JButton btnExportar = new JButton("Exportar (CSV)");
    private final JLabel lblMatriz = new JLabel("Matriz de seguimiento");
    private int idMatriz;

    public FrmMatrizSeguimiento() {
        super("FrmMatrizSeguimiento", true, true, true, true);
        armarPantalla();
        cargarSupervisiones();
        setSize(1000, 600);
    }

    private static DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void armarPantalla() {
        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        norte.add(new JLabel("Supervisiones en estado INFORME"), BorderLayout.NORTH);
        tblSupervisiones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spSupervisiones = new JScrollPane(tblSupervisiones);
        spSupervisiones.setPreferredSize(new Dimension(100, 120));
        norte.add(spSupervisiones, BorderLayout.CENTER);

        JButton btnMenu = new JButton("Menú principal");
        JButton btnGenerar = new JButton("Generar matriz (SEGUIMIENTO)");
        btnMenu.addActionListener(e -> dispose());
        btnGenerar.addActionListener(e -> generarMatriz());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnMenu);
        botones.add(btnGenerar);
        norte.add(botones, BorderLayout.SOUTH);

        tblMatriz.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblMatriz.setRowHeight(24);
        int[] anchos = {80, 280, 240, 240, 240, 170, 120};
        for (int i = 0; i < anchos.length; i++) {
            tblMatriz.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        btnExportar.setEnabled(false);
        btnExportar.addActionListener(e -> exportar());

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        centro.add(lblMatriz, BorderLayout.NORTH);
        centro.add(new JScrollPane(tblMatriz), BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        sur.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        sur.add(btnExportar);

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    /** El supervisor solo ve sus propias supervisiones; los demás perfiles (ej. ADMINISTRADOR) ven todas. */
    private String cuiDelSupervisor() {
        Usuario u = Sesion.getInstancia().getUsuarioActual();
        if (u != null && ("SUPERVISOR_DCI".equals(u.getPerfil()) || "OPERADOR".equals(u.getPerfil()))) {
            return u.getCui();
        }
        return null;
    }

    private void cargarSupervisiones() {
        try {
            modeloSupervisiones.setRowCount(0);
            for (Object[] fila : AccionDAO.listarSupervisiones("INFORME", cuiDelSupervisor(), null, true)) {
                modeloSupervisiones.addRow(fila);
            }
        } catch (SupervisionException e) {
            mostrarError(e);
        }
    }

    private void generarMatriz() {
        int fila = tblSupervisiones.getSelectedRow();
        if (fila < 0) {
            aviso("Seleccione una supervisión en estado INFORME.");
            return;
        }
        int id = (Integer) modeloSupervisiones.getValueAt(fila, 0);
        try {
            List<Object[]> datos = AccionDAO.listarMatriz(id);
            if (datos.isEmpty()) {
                aviso("La supervisión no tiene hallazgos (deficiencias u oportunidades de mejora).");
                return;
            }
            SupervisionDAO.cambiarEstado(id, "SEGUIMIENTO", "Matriz de seguimiento generada");

            modeloMatriz.setRowCount(0);
            for (Object[] f : datos) {
                modeloMatriz.addRow(f);
            }
            idMatriz = id;
            lblMatriz.setText("Matriz de seguimiento – Supervisión " + id);
            btnExportar.setEnabled(true);
            cargarSupervisiones();
            JOptionPane.showMessageDialog(this, "Matriz generada. La supervisión pasó a SEGUIMIENTO.",
                    "Matriz de seguimiento", JOptionPane.INFORMATION_MESSAGE);
        } catch (SupervisionException e) {
            mostrarError(e);
        }
    }

    /** Exporta la matriz mostrada a un CSV que abre en Excel. */
    private void exportar() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("matriz_supervision_" + idMatriz + ".csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (BufferedWriter w = Files.newBufferedWriter(selector.getSelectedFile().toPath(),
                StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            escribirFila(w, COLUMNAS_MATRIZ);
            for (int f = 0; f < modeloMatriz.getRowCount(); f++) {
                Object[] fila = new Object[COLUMNAS_MATRIZ.length];
                for (int c = 0; c < fila.length; c++) {
                    fila[c] = modeloMatriz.getValueAt(f, c);
                }
                escribirFila(w, fila);
            }
            JOptionPane.showMessageDialog(this, "Archivo guardado.", "Exportar",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo: " + e.getMessage(),
                    "Exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void escribirFila(BufferedWriter w, Object[] valores) throws IOException {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            String texto = valores[i] == null ? "" : valores[i].toString();
            if (i > 0) {
                linea.append(',');
            }
            linea.append('"').append(texto.replace("\"", "\"\"")).append('"');
        }
        w.write(linea.toString());
        w.newLine();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Matriz de seguimiento", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(SupervisionException e) {
        String detalle = e.getCause() != null ? "\n" + e.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, e.getMessage() + detalle, "Matriz de seguimiento",
                JOptionPane.ERROR_MESSAGE);
    }
}