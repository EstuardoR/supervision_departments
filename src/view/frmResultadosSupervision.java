/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import util.clsConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.ArrayList;
import dao.clsResultados;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import dao.clsResultados;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class frmResultadosSupervision extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(frmResultadosSupervision.class.getName());

    /**
     * Creates new form frmResultadosSupervision
     */
public frmResultadosSupervision() {
    initComponents();

datePresentacion.setDateFormatString("dd/MM/yyyy");
dateLimite.setDateFormatString("dd/MM/yyyy");

datePresentacion.setDate(null);
dateLimite.setDate(null);
    agregarPlaceholder(txtEnlace, "[enlace]");
    agregarPlaceholder(txtEnlace, "[enlace]");
    
    javax.swing.table.DefaultTableModel modelo =
        (javax.swing.table.DefaultTableModel) tablaHallazgos.getModel();

modelo.setRowCount(0);
}

private void agregarPlaceholder(
        javax.swing.JTextField campo, String texto) {

    campo.setText(texto);
    campo.setForeground(java.awt.Color.GRAY);
    campo.setEditable(true);

    campo.addFocusListener(new java.awt.event.FocusAdapter() {

        @Override
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (campo.getText().equals(texto)) {
                campo.setText("");
                campo.setForeground(java.awt.Color.BLACK);
            }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (campo.getText().trim().isEmpty()) {
                campo.setText(texto);
                campo.setForeground(java.awt.Color.GRAY);
            }
        }
    });
}

private int idSupervision;

public frmResultadosSupervision(int idSupervision) {
    this();

    this.idSupervision = idSupervision;

    setLocationRelativeTo(null);

    cargarDatosSupervision();
    cargarResultadosGuardados();
}


private void cargarDatosSupervision() {

    String sql =
        "SELECT u.nombre_completo, "
        + "s.oficina_auxiliatura, s.estado "
        + "FROM dbo.Supervisiones s "
        + "INNER JOIN dbo.Usuarios u "
        + "ON s.cui_supervisor = u.cui "
        + "WHERE s.id_supervision = ? "
        + "AND s.estado = 'EJECUTADO'";

    try (Connection con = clsConnection.getConexion()) {

        if (con == null) {
            JOptionPane.showMessageDialog(this,
                "No se pudo conectar con SQL Server.");
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSupervision);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    txtSupervisor.setText(
                        rs.getString("nombre_completo")
                    );

                    txtLugar.setText(
                        rs.getString("oficina_auxiliatura")
                    );

                    txtSupervisor.setEditable(false);
                    txtLugar.setEditable(false);

                } else {

                    JOptionPane.showMessageDialog(this,
                        "No se encontró una supervisión "
                        + "en estado EJECUTADO con ID: "
                        + idSupervision);
                }
            }
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
            "Error al cargar supervisión: "
            + e.getMessage());
    }
}


private boolean validarFechas() {

    java.util.Date presentacion = datePresentacion.getDate();
    java.util.Date limite = dateLimite.getDate();

    if (presentacion == null || limite == null) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar ambas fechas.");
        return false;
    }

    if (limite.before(presentacion)) {
        JOptionPane.showMessageDialog(this,
                "La fecha límite no puede ser anterior "
                + "a la fecha de presentación.");
        return false;
    }

    return true;
}

public void agregarHallazgo(
        String sigla,
        String descripcion,
        String normativa,
        File evidencia,
        String accion) {

    DefaultTableModel modelo =
            (DefaultTableModel) tablaHallazgos.getModel();

    modelo.addRow(new Object[]{
        sigla,
        descripcion,
        normativa,
        evidencia.getName(),
        accion
    });

    evidencias.add(evidencia);
}

public String obtenerEvidenciaHallazgo(int fila) {

    Object valor = tablaHallazgos.getModel().getValueAt(fila, 3);

    return valor == null ? "" : valor.toString();
}

public void actualizarHallazgo(
        int fila,
        String sigla,
        String descripcion,
        String normativa,
        File evidencia,
        String accion) {

    DefaultTableModel modelo =
            (DefaultTableModel) tablaHallazgos.getModel();

    modelo.setValueAt(sigla, fila, 0);
    modelo.setValueAt(descripcion, fila, 1);
    modelo.setValueAt(normativa, fila, 2);
    modelo.setValueAt(evidencia.getName(), fila, 3);
    modelo.setValueAt(accion, fila, 4);

    evidencias.set(fila, evidencia);
}

public File obtenerArchivoEvidencia(int fila) {
    return evidencias.get(fila);
}

private final ArrayList<File> evidencias = new ArrayList<>();


private void guardarResultados() {

    // Validar fechas
    if (!validarFechas()) {
        return;
    }

    // Validar ID de supervisión
    if (idSupervision <= 0) {
        JOptionPane.showMessageDialog(this,
                "No se recibió una supervisión válida.");
        return;
    }

    // Obtener fechas de los calendarios
    LocalDate presentacion = datePresentacion.getDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    LocalDate limite = dateLimite.getDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    // Obtener enlace de videoconferencia
    String enlace = txtEnlace.getText().trim();

    if (enlace.isEmpty() || enlace.equals("[enlace]")) {
        JOptionPane.showMessageDialog(this,
                "Debe ingresar el enlace de videoconferencia.");
        return;
    }

    // Obtener observaciones generales
    String observaciones = txtObservaciones.getText().trim();

    // Preparar hallazgos
    List<clsResultados.Hallazgo> hallazgos = new ArrayList<>();

    javax.swing.table.TableModel modelo =
            tablaHallazgos.getModel();

    if (modelo.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this,
                "Debe agregar al menos un hallazgo.");
        return;
    }

    for (int i = 0; i < modelo.getRowCount(); i++) {

        File evidencia = evidencias.get(i);

        clsResultados.Hallazgo hallazgo =
                new clsResultados.Hallazgo(
                        modelo.getValueAt(i, 0).toString(),
                        modelo.getValueAt(i, 1).toString(),
                        modelo.getValueAt(i, 2).toString(),
                        evidencia,
                        modelo.getValueAt(i, 4).toString()
                );

        hallazgos.add(hallazgo);
    }

    // Guardar en SQL Server
    try {

        clsResultados dao = new clsResultados();

        dao.guardarResultados(
                idSupervision,
                presentacion,
                limite,
                enlace,
                observaciones,
                hallazgos
        );

        JOptionPane.showMessageDialog(this,
                "Resultados guardados correctamente.\n"
                + "Estado del resultado: CREADO.");

    } catch (Exception e) {

        JOptionPane.showMessageDialog(this,
                "Error al guardar resultados:\n"
                + e.getMessage());
    }
}


private void cargarResultadosGuardados() {

    try {
        clsResultados dao = new clsResultados();

        clsResultados.ResultadoGuardado resultado =
                dao.cargarResultados(idSupervision);

        if (resultado == null) {
            return;
        }

        // Recuperar fecha de presentación
        if (resultado.fechaPresentacion != null) {
            datePresentacion.setDate(
                Date.from(
                    resultado.fechaPresentacion
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                )
            );
        }

        // Recuperar fecha límite
        if (resultado.fechaLimite != null) {
            dateLimite.setDate(
                Date.from(
                    resultado.fechaLimite
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                )
            );
        }

        // Recuperar enlace
        if (resultado.enlace != null
                && !resultado.enlace.isBlank()) {
            txtEnlace.setText(resultado.enlace);
            txtEnlace.setForeground(java.awt.Color.BLACK);
        }

        // Recuperar observaciones
        txtObservaciones.setText(
            resultado.observaciones != null
                ? resultado.observaciones : ""
        );

        // Limpiar tabla y archivos anteriores
        DefaultTableModel modelo =
                (DefaultTableModel) tablaHallazgos.getModel();

        modelo.setRowCount(0);
        evidencias.clear();

        // Cargar hallazgos
        for (clsResultados.Hallazgo h : resultado.hallazgos) {

            String nombrePDF = h.evidencia != null
                    ? h.evidencia.getName()
                    : "";

            modelo.addRow(new Object[]{
                h.sigla,
                h.descripcion,
                h.normativa,
                nombrePDF,
                h.accion != null ? h.accion : ""
            });

            evidencias.add(h.evidencia);
        }

        // Permitir edición solamente en CREADO
        boolean editable = "CREADO".equals(resultado.estado);

        btnAgregar.setEnabled(editable);
        btnEditar.setEnabled(editable);
        btnQuitar.setEnabled(editable);
        btnGuardar.setEnabled(editable);

        datePresentacion.setEnabled(editable);
        dateLimite.setEnabled(editable);
        txtEnlace.setEditable(editable);
        txtObservaciones.setEditable(editable);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
            "Error al recuperar resultados:\n"
            + e.getMessage());
    }
}





    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtSupervisor = new javax.swing.JTextField();
        txtLugar = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtEnlace = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaHallazgos = new javax.swing.JTable();
        btnAgregar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        btnPrincipal = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnDocumento = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        datePresentacion = new com.toedter.calendar.JDateChooser();
        dateLimite = new com.toedter.calendar.JDateChooser();
        btnGenerarEnlace = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Supervisor");

        jLabel2.setText("Lugar supervisado");

        jLabel3.setText("Fecha de presentación");

        jLabel4.setText("Fecha límite");

        jLabel5.setText("Enlace de videocoferencia");

        jLabel6.setText("Hallazgos");

        tablaHallazgos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Sigla", "Descripción", "Normativa / criterio", "Evidencia ", "Acción"
            }
        ));
        jScrollPane1.setViewportView(tablaHallazgos);

        btnAgregar.setText("Agregar");
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);

        btnEditar.setText("Editar");
        btnEditar.addActionListener(this::btnEditarActionPerformed);

        btnQuitar.setText("Quitar");
        btnQuitar.addActionListener(this::btnQuitarActionPerformed);

        jLabel7.setText("Observaciones generales");

        txtObservaciones.setColumns(20);
        txtObservaciones.setRows(5);
        jScrollPane2.setViewportView(txtObservaciones);

        btnPrincipal.setText("Menú principal");

        btnGuardar.setText("Guardar (CREADO)");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnDocumento.setBackground(new java.awt.Color(51, 153, 255));
        btnDocumento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDocumento.setForeground(new java.awt.Color(255, 255, 255));
        btnDocumento.setText("Generar documento (RESULTADOS)");
        btnDocumento.addActionListener(this::btnDocumentoActionPerformed);

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Resultados de Supervisión");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addContainerGap())
        );

        btnGenerarEnlace.setText("Generar enlace");
        btnGenerarEnlace.addActionListener(this::btnGenerarEnlaceActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnPrincipal)
                        .addGap(31, 31, 31)
                        .addComponent(btnGuardar)
                        .addGap(18, 18, 18)
                        .addComponent(btnDocumento))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 754, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnAgregar)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btnEditar)
                            .addGap(18, 18, 18)
                            .addComponent(btnQuitar))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 754, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnGenerarEnlace)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtLugar, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(datePresentacion, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dateLimite, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)))
                            .addComponent(txtEnlace, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 621, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(36, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtLugar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(dateLimite, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(datePresentacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEnlace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarEnlace))
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAgregar)
                    .addComponent(btnEditar)
                    .addComponent(btnQuitar))
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrincipal)
                    .addComponent(btnGuardar)
                    .addComponent(btnDocumento))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
guardarResultados();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnGenerarEnlaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarEnlaceActionPerformed
        try {
    java.awt.Desktop.getDesktop().browse(
        new java.net.URI("https://meet.google.com/")
    );
} catch (Exception e) {
    JOptionPane.showMessageDialog(this,
        "No se pudo abrir Google Meet: " + e.getMessage());
}
    }//GEN-LAST:event_btnGenerarEnlaceActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        frmHallazgo ventana = new frmHallazgo(this);

ventana.setDefaultCloseOperation(
        javax.swing.WindowConstants.DISPOSE_ON_CLOSE
);

ventana.setVisible(true);
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
       int fila = tablaHallazgos.getSelectedRow();

if (fila == -1) {
    JOptionPane.showMessageDialog(this,
            "Seleccione un hallazgo para editar.");
    return;
}

int filaModelo = tablaHallazgos.convertRowIndexToModel(fila);

String sigla = tablaHallazgos.getModel()
        .getValueAt(filaModelo, 0).toString();

String descripcion = tablaHallazgos.getModel()
        .getValueAt(filaModelo, 1).toString();

String normativa = tablaHallazgos.getModel()
        .getValueAt(filaModelo, 2).toString();

frmHallazgo ventana = new frmHallazgo(
        this,
        filaModelo,
        sigla,
        descripcion,
        normativa,
        ""
);

ventana.setDefaultCloseOperation(
        javax.swing.WindowConstants.DISPOSE_ON_CLOSE
);

ventana.setVisible(true);
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        int fila = tablaHallazgos.getSelectedRow();

if (fila == -1) {
    JOptionPane.showMessageDialog(this,
            "Seleccione un hallazgo para quitar.");
    return;
}

int respuesta = JOptionPane.showConfirmDialog(
        this,
        "¿Está seguro de quitar este hallazgo?",
        "Confirmar eliminación",
        JOptionPane.YES_NO_OPTION
);

if (respuesta == JOptionPane.YES_OPTION) {

    javax.swing.table.DefaultTableModel modelo =
            (javax.swing.table.DefaultTableModel)
            tablaHallazgos.getModel();

    int filaModelo = tablaHallazgos.convertRowIndexToModel(fila);
    evidencias.remove(filaModelo);
    modelo.removeRow(filaModelo);

    JOptionPane.showMessageDialog(this,
            "Hallazgo eliminado correctamente.");
}
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDocumentoActionPerformed
        
if (idSupervision <= 0) {
    JOptionPane.showMessageDialog(this,
            "No se recibió una supervisión válida.");
    return;
}

try {

    clsResultados dao = new clsResultados();

    boolean permitido =
            dao.puedeGenerarDocumento(idSupervision);

    if (!permitido) {

        JOptionPane.showMessageDialog(this,
                "No se puede generar el documento.\n"
                + "Debe guardar los resultados en estado CREADO "
                + "y registrar al menos un hallazgo.");

        return;
    }

    int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Desea generar el documento de resultados "
            + "de la supervisión No. " + idSupervision + "?",
            "Confirmar generación",
            JOptionPane.YES_NO_OPTION
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    JOptionPane.showMessageDialog(this,
            "Los resultados están listos para generar "
            + "el documento PDF.\n"
            + "Pendiente de integrar el servicio "
            + "de documentos y firma del Integrante 3.");

} catch (SQLException e) {

    JOptionPane.showMessageDialog(this,
            "Error al verificar los resultados:\n"
            + e.getMessage());
}

    }//GEN-LAST:event_btnDocumentoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new frmResultadosSupervision(3).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnDocumento;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnGenerarEnlace;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnPrincipal;
    private javax.swing.JButton btnQuitar;
    private com.toedter.calendar.JDateChooser dateLimite;
    private com.toedter.calendar.JDateChooser datePresentacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tablaHallazgos;
    private javax.swing.JTextField txtEnlace;
    private javax.swing.JTextField txtLugar;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtSupervisor;
    // End of variables declaration//GEN-END:variables
}
