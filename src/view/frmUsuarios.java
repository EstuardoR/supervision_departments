/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package view;

import dao.UsuarioDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Usuario;

/**
 * Pantalla de administración de usuarios (solo ADMINISTRADOR). CRUD completo contra
 * dbo.Usuarios: listar, crear, actualizar y eliminar.
 */
public class frmUsuarios extends JInternalFrame {

    private static final String[] PERFILES = {
        "ADMINISTRADOR", "DIRECTOR_DCI", "JEFE_SUPERVISION",
        "SUPERVISOR_DCI", "OPERADOR", "JEFE_OFICINA"
    };
    private static final String[] COLUMNAS = {"CUI", "Nombre completo", "Perfil", "Correo institucional"};

    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tblUsuarios = new JTable(modeloTabla);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final javax.swing.JTextField txtCui = new javax.swing.JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final javax.swing.JTextField txtNombre = new javax.swing.JTextField();
    private final JComboBox<String> cmbPerfil = new JComboBox<>(PERFILES);
    private final javax.swing.JTextField txtDireccion = new javax.swing.JTextField();
    private final javax.swing.JTextField txtUnidad = new javax.swing.JTextField();
    private final javax.swing.JTextField txtPuesto = new javax.swing.JTextField();
    private final javax.swing.JTextField txtCodigo = new javax.swing.JTextField();
    private final javax.swing.JTextField txtCorreo = new javax.swing.JTextField();
    private final JLabel lblAyudaContrasena = new JLabel(" ");

    /** CUI del usuario seleccionado en la tabla; null cuando el formulario está en modo "Nuevo". */
    private String cuiSeleccionado;

    public frmUsuarios() {
        super("Administración de Usuarios", true, true, true, true);
        armarPantalla();
        cargarUsuarios();
        nuevoUsuario();
        setSize(880, 560);
    }

    private void armarPantalla() {
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alSeleccionarUsuario();
            }
        });
        JScrollPane spTabla = new JScrollPane(tblUsuarios);
        spTabla.setPreferredSize(new Dimension(100, 180));

        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        norte.add(new JLabel("Usuarios registrados"), BorderLayout.NORTH);
        norte.add(spTabla, BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        campo(formulario, g, "CUI", txtCui, 0, 0);
        campo(formulario, g, "Contraseña", txtContrasena, 1, 0);
        lblAyudaContrasena.setFont(lblAyudaContrasena.getFont().deriveFont(11f));
        g.gridx = 1;
        g.gridy = 1;
        g.gridwidth = 1;
        formulario.add(lblAyudaContrasena, g);

        campo(formulario, g, "Nombre completo", txtNombre, 0, 2);
        colocarEtiquetaYCombo(formulario, g, "Perfil", cmbPerfil, 1, 2);

        campo(formulario, g, "Dirección (RRHH)", txtDireccion, 0, 3);
        campo(formulario, g, "Unidad (RRHH)", txtUnidad, 1, 3);
        campo(formulario, g, "Puesto (RRHH)", txtPuesto, 0, 4);
        campo(formulario, g, "Código de empleado", txtCodigo, 1, 4);
        campo(formulario, g, "Correo institucional", txtCorreo, 0, 5);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnMenu = new JButton("Menú principal");
        btnNuevo.addActionListener(e -> nuevoUsuario());
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnMenu.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnMenu);
        botones.add(btnNuevo);
        botones.add(btnEliminar);
        botones.add(btnGuardar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(formulario, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        sur.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        add(norte, BorderLayout.NORTH);
        add(sur, BorderLayout.CENTER);
    }

    private void campo(JPanel panel, GridBagConstraints g, String etiqueta,
            javax.swing.JTextField campo, int columna, int fila) {
        g.gridx = columna * 2;
        g.gridy = fila;
        g.gridwidth = 1;
        g.weightx = 0;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = columna * 2 + 1;
        g.weightx = 1;
        panel.add(campo, g);
    }

    private void colocarEtiquetaYCombo(JPanel panel, GridBagConstraints g, String etiqueta,
            JComboBox<String> combo, int columna, int fila) {
        g.gridx = columna * 2;
        g.gridy = fila;
        g.gridwidth = 1;
        g.weightx = 0;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = columna * 2 + 1;
        g.weightx = 1;
        panel.add(combo, g);
    }

    private void cargarUsuarios() {
        try {
            modeloTabla.setRowCount(0);
            for (Usuario u : usuarioDAO.listarTodos()) {
                modeloTabla.addRow(new Object[]{
                    u.getCui(), u.getNombreCompleto(), u.getPerfil(), u.getCorreoInstitucional()
                });
            }
        } catch (SQLException e) {
            mostrarError("No se pudo cargar la lista de usuarios", e);
        }
    }

    private void alSeleccionarUsuario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String cui = (String) modeloTabla.getValueAt(fila, 0);
        try {
            Usuario u = usuarioDAO.buscarPorCui(cui);
            if (u == null) {
                aviso("No se encontró el usuario seleccionado.");
                return;
            }
            cuiSeleccionado = u.getCui();
            txtCui.setText(u.getCui());
            txtCui.setEditable(false);
            txtContrasena.setText("");
            lblAyudaContrasena.setText("Deje la contraseña en blanco para no cambiarla.");
            txtNombre.setText(u.getNombreCompleto());
            cmbPerfil.setSelectedItem(u.getPerfil());
            txtDireccion.setText(u.getDireccionRrhh());
            txtUnidad.setText(u.getUnidadRrhh());
            txtPuesto.setText(u.getPuestoRrhh());
            txtCodigo.setText(u.getCodigoEmpleado());
            txtCorreo.setText(u.getCorreoInstitucional());
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el usuario", e);
        }
    }

    private void nuevoUsuario() {
        tblUsuarios.clearSelection();
        cuiSeleccionado = null;
        txtCui.setText("");
        txtCui.setEditable(true);
        txtContrasena.setText("");
        lblAyudaContrasena.setText("Obligatoria para un usuario nuevo.");
        txtNombre.setText("");
        cmbPerfil.setSelectedIndex(0);
        txtDireccion.setText("");
        txtUnidad.setText("");
        txtPuesto.setText("");
        txtCodigo.setText("");
        txtCorreo.setText("");
        txtCui.requestFocus();
    }

    private void guardarUsuario() {
        boolean esNuevo = (cuiSeleccionado == null);
        String cui = txtCui.getText().trim();
        String nombre = txtNombre.getText().trim();
        String contrasena = new String(txtContrasena.getPassword()).trim();

        if (cui.isEmpty() || nombre.isEmpty()) {
            aviso("CUI y nombre completo son obligatorios.");
            return;
        }
        if (esNuevo && contrasena.isEmpty()) {
            aviso("Debe ingresar una contraseña para el usuario nuevo.");
            return;
        }

        Usuario u = new Usuario();
        u.setCui(cui);
        u.setContrasena(contrasena);
        u.setNombreCompleto(nombre);
        u.setPerfil((String) cmbPerfil.getSelectedItem());
        u.setDireccionRrhh(vacioComoNull(txtDireccion.getText()));
        u.setUnidadRrhh(vacioComoNull(txtUnidad.getText()));
        u.setPuestoRrhh(vacioComoNull(txtPuesto.getText()));
        u.setCodigoEmpleado(vacioComoNull(txtCodigo.getText()));
        u.setCorreoInstitucional(vacioComoNull(txtCorreo.getText()));

        try {
            if (esNuevo) {
                usuarioDAO.crear(u);
                JOptionPane.showMessageDialog(this, "Usuario creado correctamente.");
            } else {
                usuarioDAO.actualizar(u, !contrasena.isEmpty());
                JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.");
            }
            cargarUsuarios();
            nuevoUsuario();
        } catch (SQLException e) {
            mostrarError(esNuevo
                    ? "No se pudo crear el usuario (verifique que el CUI no esté repetido)"
                    : "No se pudo actualizar el usuario", e);
        }
    }

    private void eliminarUsuario() {
        if (cuiSeleccionado == null) {
            aviso("Seleccione un usuario de la tabla para eliminarlo.");
            return;
        }
        model.Usuario sesionActual = util.Sesion.getInstancia().getUsuarioActual();
        if (sesionActual != null && cuiSeleccionado.equals(sesionActual.getCui())) {
            aviso("No puede eliminar el usuario con el que tiene la sesión iniciada.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al usuario " + cuiSeleccionado + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            usuarioDAO.eliminar(cuiSeleccionado);
            JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.");
            cargarUsuarios();
            nuevoUsuario();
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el usuario (puede tener supervisiones o historial asociado)", e);
        }
    }

    private String vacioComoNull(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Usuarios", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje, SQLException e) {
        JOptionPane.showMessageDialog(this, mensaje + ".", "Usuarios", JOptionPane.ERROR_MESSAGE);
    }
}
