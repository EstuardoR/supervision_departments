/*
 * Autor: Andrea Sofia Guerrero Castro
 * Carné: 9490-25-14813
 */
package view;
import dao.clsInformeFinal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import util.clsConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import dao.clsInformeFinal;
import java.util.ArrayList;
import java.util.List;
import dao.SupervisionDAO;
import dao.SupervisionException;
import dao.DocumentoDAO;
import java.io.IOException;
import servicios.FirmaService;
import servicios.GeneradorDocumentoService;


public class frmInformeFinal extends javax.swing.JInternalFrame {

    private static final String TIPO_ANEXO_IV = "Anexo IV - Informe de supervision";

    /**
     * Creates new form frmInformeFinal
     */
    public frmInformeFinal() {
        initComponents();
        
            configurarTabla();

    }
    
    private int idSupervision;

public frmInformeFinal(int idSupervision) {
    this();

    this.idSupervision = idSupervision;

    cargarDatosSupervision();
    cargarHallazgos();
    cargarInformeGuardado();
}


private void cargarDatosSupervision() {

    String sql =
        "SELECT u.nombre_completo, "
        + "s.oficina_auxiliatura, "
        + "s.fecha_alcance_inicial, "
        + "s.fecha_alcance_final "
        + "FROM dbo.Supervisiones s "
        + "INNER JOIN dbo.Usuarios u "
        + "ON s.cui_supervisor = u.cui "
        + "WHERE s.id_supervision = ?";

    try (Connection con = clsConnection.ConnectionDB()) {

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

                    SimpleDateFormat formato =
                        new SimpleDateFormat("dd/MM/yyyy");

                    java.sql.Date fechaInicial =
                        rs.getDate("fecha_alcance_inicial");

                    java.sql.Date fechaFinal =
                        rs.getDate("fecha_alcance_final");

                    txtInicial.setText(
                        fechaInicial != null
                        ? formato.format(fechaInicial)
                        : ""
                    );

                    txtFinal.setText(
                        fechaFinal != null
                        ? formato.format(fechaFinal)
                        : ""
                    );

                    // Campos de solo lectura
                    txtSupervisor.setEditable(false);
                    txtLugar.setEditable(false);
                    txtInicial.setEditable(false);
                    txtFinal.setEditable(false);

                } else {

                    JOptionPane.showMessageDialog(this,
                        "No se encontró la supervisión con ID: "
                        + idSupervision);
                }
            }
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
            "Error al cargar supervisión:\n"
            + e.getMessage());
    }
}

private void configurarTabla() {

    DefaultTableModel modelo = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Incluir", "Sigla", "Descripción"}
    ) {

        @Override
        public Class<?> getColumnClass(int columna) {
            if (columna == 0) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna == 0;
        }
    };

    tablaHallazgos.setModel(modelo);

    tablaHallazgos.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
    );

    tablaHallazgos.setRowHeight(25);
}



private void cargarHallazgos() {

    DefaultTableModel modelo =
            (DefaultTableModel) tablaHallazgos.getModel();

    modelo.setRowCount(0);
    idsHallazgos.clear();

    String sql =
        "SELECT d.id_detalle_resultado, "
        + "d.sigla, d.descripcion_resultado "
        + "FROM dbo.Detalle_Resultados d "
        + "INNER JOIN dbo.Resultados_Supervision r "
        + "ON d.id_resultado = r.id_resultado "
        + "WHERE r.id_supervision = ? "
        + "ORDER BY d.id_detalle_resultado";

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con SQL Server.");
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSupervision);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int idHallazgo =
                            rs.getInt("id_detalle_resultado");

                    idsHallazgos.add(idHallazgo);

                    modelo.addRow(new Object[]{
                        Boolean.TRUE,
                        rs.getString("sigla"),
                        rs.getString("descripcion_resultado")
                    });
                }
            }
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
                "Error al cargar hallazgos:\n"
                + e.getMessage());
    }
}



private boolean validarCampos() {

    // Validar objetivos específicos
    if (txtEspecificos.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Debe ingresar los objetivos específicos.");
        txtEspecificos.requestFocus();
        return false;
    }

    // Validar objetivos examinados
    if (txtExaminados.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Debe ingresar los objetivos examinados.");
        txtExaminados.requestFocus();
        return false;
    }

    // Confirmar que exista al menos un hallazgo incluido
    boolean hayHallazgoSeleccionado = false;

    for (int i = 0; i < tablaHallazgos.getRowCount(); i++) {

        int filaModelo =
                tablaHallazgos.convertRowIndexToModel(i);

        Object valor = tablaHallazgos.getModel()
                .getValueAt(filaModelo, 0);

        if (Boolean.TRUE.equals(valor)) {
            hayHallazgoSeleccionado = true;
            break;
        }
    }

    if (!hayHallazgoSeleccionado) {
        JOptionPane.showMessageDialog(this,
                "Debe seleccionar al menos un hallazgo "
                + "para incluir en el informe.");
        return false;
    }

    return true;
}

private final List<Integer> idsHallazgos = new ArrayList<>();


private void guardarInforme() {

    // Validar campos
    if (!validarCampos()) {
        return;
    }

    // Validar supervisión
    if (idSupervision <= 0) {
        JOptionPane.showMessageDialog(this,
                "No se recibió una supervisión válida.");
        return;
    }

    String objetivos = txtEspecificos.getText().trim();
    String documentos = txtExaminados.getText().trim();

    List<Integer> seleccionados = new ArrayList<>();

    // Tomar únicamente los hallazgos marcados
    for (int i = 0; i < tablaHallazgos.getRowCount(); i++) {

        int filaModelo =
                tablaHallazgos.convertRowIndexToModel(i);

        Boolean incluir = (Boolean) tablaHallazgos
                .getModel().getValueAt(filaModelo, 0);

        if (Boolean.TRUE.equals(incluir)) {

            int idHallazgo = idsHallazgos.get(filaModelo);

            seleccionados.add(idHallazgo);
        }
    }

    // Guardar en SQL Server
    try {

        clsInformeFinal dao = new clsInformeFinal();

        dao.guardarInforme(
                idSupervision,
                objetivos,
                documentos,
                seleccionados
        );

        JOptionPane.showMessageDialog(this,
                "Informe guardado correctamente.\n"
                + "Estado del informe: CREADO.");

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
                "Error al guardar informe:\n"
                + e.getMessage());
    }
}


private void cargarInformeGuardado() {

    try {

        clsInformeFinal dao = new clsInformeFinal();

        clsInformeFinal.InformeGuardado informe =
                dao.cargarInforme(idSupervision);

        if (informe == null) {
            return;
        }

        // Recuperar objetivos específicos
        txtEspecificos.setText(
                informe.objetivosEspecificos != null
                ? informe.objetivosEspecificos
                : ""
        );

        // Recuperar documentos examinados
        txtExaminados.setText(
                informe.documentosExaminados != null
                ? informe.documentosExaminados
                : ""
        );

        // Recuperar hallazgos seleccionados
        javax.swing.table.TableModel modelo =
                tablaHallazgos.getModel();

        for (int i = 0; i < modelo.getRowCount(); i++) {

            int idHallazgo = idsHallazgos.get(i);

            boolean incluir =
                    informe.hallazgosSeleccionados
                            .contains(idHallazgo);

            modelo.setValueAt(incluir, i, 0);
        }

        // Determinar si el informe puede editarse
        boolean editable =
                "CREADO".equals(informe.estadoInforme);

        txtEspecificos.setEditable(editable);
        txtExaminados.setEditable(editable);

        btnGuardar.setEnabled(editable);

        // Bloquear o permitir las casillas de selección
        if (modelo instanceof
                javax.swing.table.DefaultTableModel) {

            tablaHallazgos.setEnabled(editable);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
                "Error al recuperar el informe:\n"
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
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtEspecificos = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtExaminados = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaHallazgos = new javax.swing.JTable();
        btnPrincipal = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnDocumento = new javax.swing.JButton();
        btnVisualizar = new javax.swing.JButton();
        txtLugar = new javax.swing.JTextField();
        txtInicial = new javax.swing.JTextField();
        txtFinal = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Informe Final");

        jLabel1.setText("Supervisor DCI 1");

        jLabel2.setText("Lugar supervisado");

        jLabel3.setText("Alcance inicial");

        jLabel4.setText("Alcance final");

        txtSupervisor.setEditable(false);

        jLabel5.setText("Objetivos especificos");

        txtEspecificos.setColumns(20);
        txtEspecificos.setRows(5);
        jScrollPane1.setViewportView(txtEspecificos);

        jLabel6.setText("Objetivos examinados");

        txtExaminados.setColumns(20);
        txtExaminados.setRows(5);
        jScrollPane2.setViewportView(txtExaminados);

        jLabel7.setText("Hallazgos a incluir en el informe");

        tablaHallazgos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Incluir", "Sigla", "Descripcion"
            }
        ));
        jScrollPane3.setViewportView(tablaHallazgos);

        btnPrincipal.setText("Menú principal");
        btnPrincipal.addActionListener(this::btnPrincipalActionPerformed);

        btnGuardar.setText("Guardar (CREADO)");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        btnDocumento.setBackground(new java.awt.Color(51, 153, 255));
        btnDocumento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDocumento.setForeground(new java.awt.Color(255, 255, 255));
        btnDocumento.setText("Generar documento (INFORME)");
        btnDocumento.addActionListener(this::btnDocumentoActionPerformed);

        btnVisualizar.setText("Visualizar documento");
        btnVisualizar.addActionListener(this::btnVisualizarActionPerformed);

        txtLugar.setEditable(false);

        txtInicial.setEditable(false);

        txtFinal.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addComponent(jScrollPane3)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(89, 89, 89)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtLugar, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(38, 38, 38)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(31, 31, 31)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(259, 259, 259)
                                .addComponent(btnPrincipal)
                                .addGap(18, 18, 18)
                                .addComponent(btnGuardar)
                                .addGap(18, 18, 18)
                                .addComponent(btnDocumento)
                                .addGap(18, 18, 18)
                                .addComponent(btnVisualizar))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(txtSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLugar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPrincipal)
                    .addComponent(btnGuardar)
                    .addComponent(btnDocumento)
                    .addComponent(btnVisualizar))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPrincipalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrincipalActionPerformed
        dispose();
    }//GEN-LAST:event_btnPrincipalActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       guardarInforme();

    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDocumentoActionPerformed
        
if (idSupervision <= 0) {

    JOptionPane.showMessageDialog(this,
            "No se recibió una supervisión válida.");
    return;
}

try {

    clsInformeFinal dao = new clsInformeFinal();

    boolean permitido =
            dao.puedeGenerarDocumento(idSupervision);

    if (!permitido) {

        JOptionPane.showMessageDialog(this,
                "No se puede generar el informe.\n"
                + "Debe guardar el informe en estado CREADO "
                + "y seleccionar al menos un hallazgo.");

        return;
    }

    int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Desea generar el Informe Final "
            + "de la supervisión No. " + idSupervision + "?",
            "Confirmar generación",
            JOptionPane.YES_NO_OPTION
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    if (!FirmaService.firmar(this)) {
        return;
    }

    SupervisionDAO.cambiarEstado(idSupervision, "ACCIONES", "INFORME",
            "Informe final firmado. Código de firma " + FirmaService.getUltimoCodigo());
    dao.marcarComoEnviado(idSupervision);

    try {
        String cuerpo = "<table>"
                + "<tr><th>No. de supervisión</th><td>" + idSupervision + "</td></tr>"
                + "<tr><th>Supervisor</th><td>" + txtSupervisor.getText() + "</td></tr>"
                + "<tr><th>Lugar supervisado</th><td>" + txtLugar.getText() + "</td></tr>"
                + "<tr><th>Alcance</th><td>" + txtInicial.getText() + " a " + txtFinal.getText() + "</td></tr>"
                + "<tr><th>Objetivos específicos</th><td>" + txtEspecificos.getText() + "</td></tr>"
                + "<tr><th>Documentos examinados</th><td>" + txtExaminados.getText() + "</td></tr>"
                + "</table>"
                + "<p class=\"firma\">Firmado el " + FirmaService.getUltimaFechaHora()
                + " — Código de firma: " + FirmaService.getUltimoCodigo() + "</p>";
        String ruta = GeneradorDocumentoService.generar(idSupervision, "Anexo_IV_Informe",
                "Anexo IV - Informe de supervisión", cuerpo);
        new DocumentoDAO().registrar(idSupervision, TIPO_ANEXO_IV, ruta);
    } catch (IOException | SQLException ex) {
        // No se deshace el cambio de estado si falla la generación del documento.
    }

    JOptionPane.showMessageDialog(this,
            "Informe generado y firmado. La supervisión pasó a INFORME.\n"
            + "Código de firma: " + FirmaService.getUltimoCodigo());

} catch (SupervisionException e) {

    String detalle = e.getCause() != null ? "\n" + e.getCause().getMessage() : "";
    JOptionPane.showMessageDialog(this,
            "No se pudo generar el informe:\n" + e.getMessage() + detalle);

} catch (SQLException e) {

    JOptionPane.showMessageDialog(this,
            "Error al verificar el informe:\n"
            + e.getMessage());
}

    }//GEN-LAST:event_btnDocumentoActionPerformed

    private void btnVisualizarActionPerformed(java.awt.event.ActionEvent evt) {
        if (idSupervision <= 0) {
            JOptionPane.showMessageDialog(this, "No se recibió una supervisión válida.");
            return;
        }
        try {
            String ruta = new DocumentoDAO().buscarRuta(idSupervision, TIPO_ANEXO_IV);
            if (ruta == null || !GeneradorDocumentoService.abrir(ruta)) {
                JOptionPane.showMessageDialog(this,
                        "Todavía no se ha generado el documento de esta supervisión.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo consultar el documento:\n" + e.getMessage());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDocumento;
    private javax.swing.JButton btnVisualizar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnPrincipal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tablaHallazgos;
    private javax.swing.JTextArea txtEspecificos;
    private javax.swing.JTextArea txtExaminados;
    private javax.swing.JTextField txtFinal;
    private javax.swing.JTextField txtInicial;
    private javax.swing.JTextField txtLugar;
    private javax.swing.JTextField txtSupervisor;
    // End of variables declaration//GEN-END:variables
}
