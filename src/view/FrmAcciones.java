package view;

import dao.AccionDAO;
import dao.SupervisionDAO;
import dao.SupervisionException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import model.CausaRaiz;
import model.HallazgoAccion;
import model.Usuario;
import servicios.FirmaService;
import servicios.NotificacionService;
import util.ArchivoService;
import util.Sesion;

/**
 * Pantalla 12 – Acciones del Jefe de Oficina.
 * Supervisiones en RESULTADOS; pestañas Preventivas (OM) y Correctivas (D);
 * Guardar (CREADO) y Generar documento (ACCIONES).
 */
public class FrmAcciones extends JInternalFrame {

    private static final String[] COLUMNAS_SUPERVISIONES = {"ID", "Oficina", "Fecha", "Estado"};

    private final DefaultTableModel modeloSupervisiones = new DefaultTableModel(COLUMNAS_SUPERVISIONES, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tblSupervisiones = new JTable(modeloSupervisiones);
    private final ModeloHallazgos modeloPreventivas = new ModeloHallazgos(false);
    private final ModeloHallazgos modeloCorrectivas = new ModeloHallazgos(true);
    private final JTable tblPreventivas = new JTable(modeloPreventivas);
    private final JTable tblCorrectivas = new JTable(modeloCorrectivas);
    private final JTabbedPane pestanas = new JTabbedPane();
    private Integer idSupervisionActual;
    private boolean cargando;

    public FrmAcciones() {
        super("FrmAcciones – Jefe de Oficina", true, true, true, true);
        armarPantalla();
        cargarSupervisiones();
        setSize(1000, 620);
    }

    // ------------------------------------------------------------------
    // Construcción de la pantalla
    // ------------------------------------------------------------------
    private void armarPantalla() {
        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        norte.add(new JLabel("Supervisiones en estado RESULTADOS de mi oficina"), BorderLayout.NORTH);
        tblSupervisiones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSupervisiones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !cargando) {
                alSeleccionarSupervision();
            }
        });
        JScrollPane spSupervisiones = new JScrollPane(tblSupervisiones);
        spSupervisiones.setPreferredSize(new Dimension(100, 110));
        norte.add(spSupervisiones, BorderLayout.CENTER);

        configurarTablaHallazgos(tblPreventivas);
        configurarTablaHallazgos(tblCorrectivas);
        pestanas.addTab("Preventivas (OM)", new JScrollPane(tblPreventivas));
        pestanas.addTab("Correctivas (D)", new JScrollPane(tblCorrectivas));
        modeloCorrectivas.setAlElegirSi(this::abrirCausaRaiz);

        JButton btnAdjuntar = new JButton("Adjuntar respaldo...");
        JButton btnCausa = new JButton("Causa-raíz...");
        JButton btnMenu = new JButton("Menú principal");
        JButton btnGuardar = new JButton("Guardar (CREADO)");
        JButton btnGenerar = new JButton("Generar documento (ACCIONES)");
        btnAdjuntar.addActionListener(e -> adjuntarRespaldo());
        btnCausa.addActionListener(e -> botonCausaRaiz());
        btnMenu.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        btnGenerar.addActionListener(e -> generarDocumento());

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izquierda.add(btnAdjuntar);
        izquierda.add(btnCausa);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        derecha.add(btnMenu);
        derecha.add(btnGuardar);
        derecha.add(btnGenerar);
        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        sur.add(izquierda, BorderLayout.WEST);
        sur.add(derecha, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        centro.add(pestanas, BorderLayout.CENTER);

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    private void configurarTablaHallazgos(JTable tabla) {
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JComboBox<String> combo = new JComboBox<>(new String[]{"", "SI", "NO"});
        tabla.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(combo));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(260);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------
    private void cargarSupervisiones() {
        cargando = true;
        try {
            modeloSupervisiones.setRowCount(0);
            for (Object[] fila : AccionDAO.listarSupervisiones("RESULTADOS", null, oficinaDelUsuario(), false)) {
                modeloSupervisiones.addRow(fila);
            }
        } catch (SupervisionException e) {
            mostrarError(e);
        } finally {
            cargando = false;
        }
        idSupervisionActual = null;
        modeloPreventivas.cargar(new ArrayList<>());
        modeloCorrectivas.cargar(new ArrayList<>());
    }

    /**
     * El Jefe de Oficina solo ve las supervisiones de su oficina. La tabla Usuarios
     * guarda su unidad en unidad_rrhh ("Oficina Auxiliatura Municipal"), y la
     * supervisión guarda oficina_auxiliatura ("Oficina Auxiliatura Municipal Flores"),
     * por eso se compara con LIKE. Los demás perfiles (ej. ADMINISTRADOR) ven todas.
     */
    private String oficinaDelUsuario() {
        Usuario u = Sesion.getInstancia().getUsuarioActual();
        if (u != null && "JEFE_OFICINA".equals(u.getPerfil()) && u.getUnidadRrhh() != null) {
            return u.getUnidadRrhh() + "%";
        }
        return null;
    }

    private void alSeleccionarSupervision() {
        int fila = tblSupervisiones.getSelectedRow();
        if (fila < 0) {
            idSupervisionActual = null;
            modeloPreventivas.cargar(new ArrayList<>());
            modeloCorrectivas.cargar(new ArrayList<>());
            return;
        }
        idSupervisionActual = (Integer) modeloSupervisiones.getValueAt(fila, 0);
        cargarHallazgos();
    }

    private void cargarHallazgos() {
        try {
            modeloPreventivas.cargar(AccionDAO.listarHallazgos(idSupervisionActual, "OM"));
            modeloCorrectivas.cargar(AccionDAO.listarHallazgos(idSupervisionActual, "D"));
        } catch (SupervisionException e) {
            mostrarError(e);
        }
    }

    // ------------------------------------------------------------------
    // Botones
    // ------------------------------------------------------------------
    private void adjuntarRespaldo() {
        if (idSupervisionActual == null) {
            aviso("Primero seleccione una supervisión.");
            return;
        }
        boolean correctivas = pestanas.getSelectedIndex() == 1;
        JTable tabla = correctivas ? tblCorrectivas : tblPreventivas;
        ModeloHallazgos modelo = correctivas ? modeloCorrectivas : modeloPreventivas;
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            aviso("Seleccione el hallazgo al que va el respaldo.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String ruta = ArchivoService.copiarAExpediente(idSupervisionActual, selector.getSelectedFile());
            modelo.get(fila).setRutaRespaldo(ruta);
            modelo.fireTableRowsUpdated(fila, fila);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo copiar el archivo: " + e.getMessage(),
                    "Acciones", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void botonCausaRaiz() {
        if (pestanas.getSelectedIndex() != 1) {
            aviso("La causa-raíz aplica a las deficiencias (pestaña Correctivas).");
            return;
        }
        int fila = tblCorrectivas.getSelectedRow();
        if (fila < 0) {
            aviso("Seleccione la deficiencia.");
            return;
        }
        abrirCausaRaiz(fila);
    }

    private void abrirCausaRaiz(int fila) {
        HallazgoAccion h = modeloCorrectivas.get(fila);
        if (!Boolean.TRUE.equals(h.getPorEfectuar())) {
            aviso("Primero elija SI en \"¿Acciones por efectuar?\".");
            return;
        }
        CausaRaiz r = DlgCausaRaiz.mostrar(SwingUtilities.getWindowAncestor(this),
                h.getDescripcion(), h.getCausaRaiz());
        if (r != null) {
            h.setCausaRaiz(r);
            modeloCorrectivas.fireTableRowsUpdated(fila, fila);
        }
    }

    private void detenerEdicion() {
        if (tblPreventivas.isEditing()) {
            tblPreventivas.getCellEditor().stopCellEditing();
        }
        if (tblCorrectivas.isEditing()) {
            tblCorrectivas.getCellEditor().stopCellEditing();
        }
    }

    /**
     * Guardar (CREADO). Estado de cada acción:
     * OM -> CREADO | D con NO -> SOLVENTADA | D con SI y causa raíz -> ATENDIDA.
     */
    private void guardar() {
        if (idSupervisionActual == null) {
            aviso("Primero seleccione una supervisión.");
            return;
        }
        detenerEdicion();
        List<HallazgoAccion> todos = new ArrayList<>(modeloPreventivas.todos());
        todos.addAll(modeloCorrectivas.todos());
        if (todos.isEmpty()) {
            aviso("Esta supervisión no tiene oportunidades de mejora ni deficiencias.");
            return;
        }
        int numeroOM = 0;
        int numeroD = 0;
        for (HallazgoAccion h : todos) {
            String nombre = "D".equals(h.getSigla()) ? "D-" + (++numeroD) : "OM-" + (++numeroOM);
            if (h.getAccionInmediata() == null || h.getAccionInmediata().trim().isEmpty()) {
                aviso("Escriba la acción inmediata de " + nombre + ".");
                return;
            }
            if (h.getPorEfectuar() == null) {
                aviso("Elija SI o NO en \"¿Acciones por efectuar?\" de " + nombre + ".");
                return;
            }
            if ("D".equals(h.getSigla())) {
                if (Boolean.FALSE.equals(h.getPorEfectuar())) {
                    h.setEstadoAccion("SOLVENTADA");
                } else if (h.getCausaRaiz() == null && !h.isTieneCausaRaiz()) {
                    aviso("Falta completar la causa-raíz de " + nombre + ".");
                    return;
                } else {
                    h.setEstadoAccion("ATENDIDA");
                }
            } else {
                h.setEstadoAccion("CREADO");
            }
        }
        try {
            AccionDAO.guardarAcciones(todos);
            cargarHallazgos();
            JOptionPane.showMessageDialog(this, "Las respuestas se guardaron correctamente.",
                    "Acciones", JOptionPane.INFORMATION_MESSAGE);
        } catch (SupervisionException e) {
            mostrarError(e);
        }
    }

    /** Generar documento: firma, cambia la supervisión a ACCIONES (queda en el historial) y envía correo. */
    private void generarDocumento() {
        if (idSupervisionActual == null) {
            aviso("Primero seleccione una supervisión.");
            return;
        }
        detenerEdicion();
        try {
            int pendientes = AccionDAO.contarHallazgosSinAccion(idSupervisionActual);
            if (pendientes > 0) {
                aviso("Hay " + pendientes + " hallazgo(s) sin acción guardada. "
                        + "Complételos y presione Guardar antes de generar el documento.");
                return;
            }
            if (!FirmaService.firmar(this)) {
                return;
            }
            SupervisionDAO.cambiarEstado(idSupervisionActual, "ACCIONES",
                    "Acciones firmadas. Código de firma " + FirmaService.getUltimoCodigo());

            String correo = AccionDAO.obtenerCorreoSupervisor(idSupervisionActual);
            boolean enviado = NotificacionService.enviar(correo,
                    "Acciones registradas - Supervisión " + idSupervisionActual,
                    "El Jefe de Oficina registró y firmó las acciones de la supervisión "
                    + idSupervisionActual + ".\nCódigo de firma: " + FirmaService.getUltimoCodigo()
                    + "\nFecha y hora: " + FirmaService.getUltimaFechaHora()
                    + "\n\nSistema de Supervisión de Dependencias DCI");

            JOptionPane.showMessageDialog(this,
                    "Documento generado. La supervisión pasó a ACCIONES.\n"
                    + (enviado ? "Se envió el correo al supervisor."
                            : "No se pudo enviar el correo (revise la configuración de correo)."),
                    "Acciones", JOptionPane.INFORMATION_MESSAGE);
            cargarSupervisiones();
        } catch (SupervisionException e) {
            mostrarError(e);
        }
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Acciones", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(SupervisionException e) {
        String detalle = e.getCause() != null ? "\n" + e.getCause().getMessage() : "";
        JOptionPane.showMessageDialog(this, e.getMessage() + detalle, "Acciones", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Modelo de tabla para los hallazgos
    // ------------------------------------------------------------------
    private static class ModeloHallazgos extends AbstractTableModel {

        private final boolean correctivas;
        private final String[] columnas;
        private final List<HallazgoAccion> filas = new ArrayList<>();
        private IntConsumer alElegirSi;

        ModeloHallazgos(boolean correctivas) {
            this.correctivas = correctivas;
            this.columnas = correctivas
                    ? new String[]{"Deficiencia", "Acción correctiva inmediata", "Adjunto",
                        "¿Acciones por efectuar?", "Causa-raíz"}
                    : new String[]{"Oportunidad de mejora", "Acción inmediata", "Adjunto",
                        "¿Acciones por efectuar?", "Dirección responsable", "Departamento responsable"};
        }

        void setAlElegirSi(IntConsumer accion) {
            this.alElegirSi = accion;
        }

        void cargar(List<HallazgoAccion> datos) {
            filas.clear();
            filas.addAll(datos);
            fireTableDataChanged();
        }

        HallazgoAccion get(int fila) {
            return filas.get(fila);
        }

        List<HallazgoAccion> todos() {
            return filas;
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 1 || columna == 3 || (!correctivas && (columna == 4 || columna == 5));
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            HallazgoAccion h = filas.get(fila);
            switch (columna) {
                case 0:
                    return h.getSigla() + "-" + (fila + 1) + " " + h.getDescripcion();
                case 1:
                    return texto(h.getAccionInmediata());
                case 2:
                    return h.getRutaRespaldo() == null ? "" : new File(h.getRutaRespaldo()).getName();
                case 3:
                    return h.getPorEfectuar() == null ? "" : (h.getPorEfectuar() ? "SI" : "NO");
                case 4:
                    if (correctivas) {
                        return (h.getCausaRaiz() != null || h.isTieneCausaRaiz()) ? "Completa" : "Pendiente";
                    }
                    return texto(h.getDireccionResponsable());
                default:
                    return texto(h.getDepartamentoResponsable());
            }
        }

        @Override
        public void setValueAt(Object valor, int fila, int columna) {
            HallazgoAccion h = filas.get(fila);
            String v = valor == null ? "" : valor.toString().trim();
            switch (columna) {
                case 1:
                    h.setAccionInmediata(v);
                    break;
                case 3:
                    h.setPorEfectuar(v.isEmpty() ? null : v.equals("SI"));
                    if (correctivas && v.equals("SI") && h.getCausaRaiz() == null
                            && !h.isTieneCausaRaiz() && alElegirSi != null) {
                        SwingUtilities.invokeLater(() -> alElegirSi.accept(fila));
                    }
                    break;
                case 4:
                    h.setDireccionResponsable(v);
                    break;
                case 5:
                    h.setDepartamentoResponsable(v);
                    break;
                default:
                    break;
            }
            fireTableRowsUpdated(fila, fila);
        }

        private static String texto(String s) {
            return s == null ? "" : s;
        }
    }
}