/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class frmHallazgo extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(frmHallazgo.class.getName());

    /**
     * Creates new form frmHallazgo
     */
    public frmHallazgo() {
        initComponents();
        
        boxSigla.setSelectedIndex(0);
        txtAccion.setEditable(false);
        actualizarAccion();
    }
    
    private void actualizarAccion() {

    Object seleccionado = boxSigla.getSelectedItem();

    if (seleccionado == null) {
        txtAccion.setText("");
        return;
    }

    String sigla = seleccionado.toString();

    if (sigla.startsWith("D -")) {
        txtAccion.setText("AC - Correctiva");

    } else if (sigla.startsWith("OM -")) {
        txtAccion.setText("AP - Preventiva");

    } else if (sigla.startsWith("F -")) {
        txtAccion.setText("");
    }
}
    
    private File archivoEvidencia;
    
    private frmResultadosSupervision formularioResultados;
    
    public frmHallazgo(frmResultadosSupervision formularioResultados) {
    this();

    this.formularioResultados = formularioResultados;

    setLocationRelativeTo(formularioResultados);
}


    private int filaEditar = -1;
    
    public frmHallazgo(
        frmResultadosSupervision formularioResultados,
        int filaEditar,
        String sigla,
        String descripcion,
        String normativa,
        String accion) {

    this(formularioResultados);

    this.filaEditar = filaEditar;

    if (sigla.equals("D")) {
        boxSigla.setSelectedIndex(0);
    } else if (sigla.equals("OM")) {
        boxSigla.setSelectedIndex(1);
    } else if (sigla.equals("F")) {
        boxSigla.setSelectedIndex(2);
    }

    txtResultado.setText(descripcion);
    txtPolitica.setText(normativa);

    actualizarAccion();
    
    archivoEvidencia =
        formularioResultados.obtenerArchivoEvidencia(filaEditar);

if (archivoEvidencia != null) {
    txtEvidencia.setText(archivoEvidencia.getName());
}
}
   
    private boolean validarCampos() {

    if (txtResultado.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Debe ingresar la descripción del resultado.");
        txtResultado.requestFocus();
        return false;
    }

    if (txtPolitica.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Debe ingresar la política, normativa o criterio.");
        txtPolitica.requestFocus();
        return false;
    }

    if (archivoEvidencia == null
            || !archivoEvidencia.isFile()
            || !archivoEvidencia.getName().toLowerCase().endsWith(".pdf")) {

        JOptionPane.showMessageDialog(this,
                "Debe adjuntar un archivo PDF de evidencia.");

        return false;
    }

    return true;
}
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        boxSigla = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtAccion = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtResultado = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        txtPolitica = new javax.swing.JTextField();
        txtEvidencia = new javax.swing.JTextField();
        btnEvidencia = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Sigla");

        boxSigla.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "D - Deficiencia", "OM - Oportunidad de mejora", "F - Fortaleza" }));
        boxSigla.addActionListener(this::boxSiglaActionPerformed);

        jLabel2.setText("Acción necesaria (automatica)");

        jLabel3.setText("Descripción del resultado");

        txtResultado.setColumns(20);
        txtResultado.setRows(5);
        jScrollPane1.setViewportView(txtResultado);

        jLabel4.setText("Política / normativa / criterio");

        btnEvidencia.setText("Adjuntar evidencia");
        btnEvidencia.addActionListener(this::btnEvidenciaActionPerformed);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        btnGuardar.setBackground(new java.awt.Color(51, 153, 255));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnEvidencia)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancelar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnGuardar)))
                .addGap(42, 42, 42))
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(txtPolitica)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEvidencia, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(boxSigla, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAccion, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(36, 36, 36))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(boxSigla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(txtPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEvidencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEvidencia))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnGuardar))
                .addGap(26, 26, 26))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void boxSiglaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxSiglaActionPerformed
        actualizarAccion();
    }//GEN-LAST:event_boxSiglaActionPerformed

    private void btnEvidenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEvidenciaActionPerformed
 
        JFileChooser explorador = new JFileChooser();

explorador.setDialogTitle("Seleccionar evidencia PDF");

explorador.setFileFilter(
    new FileNameExtensionFilter("Archivos PDF", "pdf")
);

explorador.setAcceptAllFileFilterUsed(false);

int resultado = explorador.showOpenDialog(this);

if (resultado == JFileChooser.APPROVE_OPTION) {

    File archivo = explorador.getSelectedFile();

    if (!archivo.isFile()
            || !archivo.getName().toLowerCase().endsWith(".pdf")) {

        JOptionPane.showMessageDialog(this,
                "Seleccione un archivo PDF válido.");
        return;
    }

    archivoEvidencia = archivo;

    txtEvidencia.setText(archivo.getName());
}
    }//GEN-LAST:event_btnEvidenciaActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       if (!validarCampos()) {
    return;
}
        
        if (formularioResultados == null) {
    JOptionPane.showMessageDialog(this,
            "Debe abrir este formulario desde Resultados de Supervisión.");
    return;
}

String siglaSeleccionada = boxSigla.getSelectedItem().toString();

String sigla = siglaSeleccionada.split(" - ")[0];

String descripcion = txtResultado.getText().trim();
String normativa = txtPolitica.getText().trim();

String evidencia = archivoEvidencia != null
        ? archivoEvidencia.getName()
        : "";

String accion = "";

if (sigla.equals("D")) {
    accion = "AC";
} else if (sigla.equals("OM")) {
    accion = "AP";
}

if (descripcion.isEmpty()) {
    JOptionPane.showMessageDialog(this,
            "Debe ingresar la descripción del resultado.");
    return;
}

if (normativa.isEmpty()) {
    JOptionPane.showMessageDialog(this,
            "Debe ingresar la política, normativa o criterio.");
    return;
}

if (filaEditar == -1) {

    formularioResultados.agregarHallazgo(
            sigla,
            descripcion,
            normativa,
            archivoEvidencia,
            accion
    );

    JOptionPane.showMessageDialog(this,
            "Hallazgo agregado correctamente.");

} else {

    formularioResultados.actualizarHallazgo(
            filaEditar,
            sigla,
            descripcion,
            normativa,
            archivoEvidencia,
            accion
    );

    JOptionPane.showMessageDialog(this,
            "Hallazgo actualizado correctamente.");
}

dispose();

    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new frmHallazgo().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> boxSigla;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEvidencia;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtAccion;
    private javax.swing.JTextField txtEvidencia;
    private javax.swing.JTextField txtPolitica;
    private javax.swing.JTextArea txtResultado;
    // End of variables declaration//GEN-END:variables
}
