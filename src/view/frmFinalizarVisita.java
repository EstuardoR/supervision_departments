/*
 * Autor: Andrea Sofia Guerrero Castro
 * Carné: 9490-25-14813
 */
package view;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import util.clsConnection;


public class frmFinalizarVisita extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(frmFinalizarVisita.class.getName());

    /**
     * Creates new form frmFinalizarVisita
     */
    
            private int idSupervision;

    public frmFinalizarVisita() {
        initComponents();
        
        btnGuardar.setEnabled(false);
        
    agregarPlaceholder(txtFinanciera, "Seleccionar PDF");
    agregarPlaceholder(txtServicio, "Seleccionar PDF");
    agregarPlaceholder(txtOficina, "Seleccionar PDF");
    agregarPlaceholder(txtAdministrativa, "Seleccionar PDF");
    
    }
    
    private void agregarPlaceholder(
        javax.swing.JTextField campo, String texto) {

    campo.setText(texto);
    campo.setForeground(java.awt.Color.GRAY);
    campo.setEditable(false);
    
}
    
public frmFinalizarVisita(int idSupervision) {
    this();

    this.idSupervision = idSupervision;
    setLocationRelativeTo(null);

    System.out.println("ID recibido: " + this.idSupervision);
}

private File archivoFinanciera;
private File archivoServicio;
private File archivoOficina;
private File archivoAdministrativa;

private File seleccionarPDF(javax.swing.JTextField campo) {

    JFileChooser explorador = new JFileChooser();

    explorador.setDialogTitle("Seleccionar archivo PDF");

    explorador.setFileFilter(
        new FileNameExtensionFilter("Archivos PDF", "pdf")
    );

    explorador.setAcceptAllFileFilterUsed(false);

    int resultado = explorador.showOpenDialog(this);

    if (resultado == JFileChooser.APPROVE_OPTION) {

        File archivo = explorador.getSelectedFile();

        if (!archivo.isFile()) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un archivo válido.");
            return null;
        }

        campo.setText(archivo.getName());
        campo.setForeground(java.awt.Color.BLACK);

        return archivo;
    }

    return null;
}

private void verificarArchivos() {

    boolean completos =
            archivoFinanciera != null &&
            archivoServicio != null &&
            archivoOficina != null &&
            archivoAdministrativa != null;

    btnGuardar.setEnabled(completos);
}


private void guardarVisita() {
    
    if (idSupervision <= 0) {
    JOptionPane.showMessageDialog(this,
            "Error: No se recibió un ID de supervisión válido.\n"
            + "Debe abrir este formulario desde la Bandeja de Ejecución.");
    return;
}

    File[] archivos = {
        archivoFinanciera,
        archivoServicio,
        archivoOficina,
        archivoAdministrativa
    };

    String[] tipos = {
        "Financiera",
        "Servicio",
        "Oficina",
        "Administrativa"
    };

    // Validar los cuatro archivos
    for (File archivo : archivos) {
        if (archivo == null || !archivo.isFile()
                || !archivo.getName().toLowerCase().endsWith(".pdf")) {

            JOptionPane.showMessageDialog(this,
                "Debe seleccionar los cuatro archivos PDF válidos.");
            return;
        }
    }

    List<Path> archivosCopiados = new ArrayList<>();
    boolean guardado = false;

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            throw new SQLException("No se pudo conectar con SQL Server.");
        }

        con.setAutoCommit(false);

        try {
            // 1. Cambiar el estado y registrar el historial (única función
            // utilitaria para cambiar Supervisiones.estado en todo el sistema)
            dao.SupervisionDAO.actualizarEstadoYHistorial(con, idSupervision, "INICIADO", "EJECUTADO",
                    "Finalización de visita con 4 PDF", null, false);

            // 2. Crear carpeta para los documentos
            Path carpeta = Paths.get(
                "documentos",
                "supervision_" + idSupervision
            );

            Files.createDirectories(carpeta);

            // 3. Copiar y registrar los cuatro PDF
            String sqlDocumento =
                "INSERT INTO dbo.Documentos_Supervision "
                + "(id_supervision, tipo_documento, ruta_archivo) "
                + "VALUES (?, ?, ?)";

            try (PreparedStatement ps =
                    con.prepareStatement(sqlDocumento)) {

                for (int i = 0; i < archivos.length; i++) {

                    String nombreArchivo = tipos[i] + "_"
                            + UUID.randomUUID() + ".pdf";

                    Path destino = carpeta.resolve(nombreArchivo);

                    Files.copy(
                        archivos[i].toPath(),
                        destino,
                        StandardCopyOption.COPY_ATTRIBUTES
                    );

                    archivosCopiados.add(destino);

                    ps.setInt(1, idSupervision);
                    ps.setString(2, tipos[i]);
                    ps.setString(3, destino.toAbsolutePath().toString());

                    ps.executeUpdate();
                }
            }

            con.commit();
            guardado = true;

        } catch (Exception ex) {
            con.rollback();
            throw ex;
        }

    } catch (Exception ex) {

        // Eliminar copias si no se guardó la transacción
        if (!guardado) {
            for (Path archivo : archivosCopiados) {
                try {
                    Files.deleteIfExists(archivo);
                } catch (Exception ignored) {
                }
            }
        }

        JOptionPane.showMessageDialog(this,
            "Error al guardar la visita:\n" + ex.getMessage());

        return;
    }

    JOptionPane.showMessageDialog(this,
        "Visita finalizada correctamente.\n"
        + "Los cuatro PDF fueron registrados.\n"
        + "Nuevo estado: EJECUTADO");

    dispose();
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
        txtFinanciera = new javax.swing.JTextField();
        txtServicio = new javax.swing.JTextField();
        txtOficina = new javax.swing.JTextField();
        txtAdministrativa = new javax.swing.JTextField();
        btn1 = new javax.swing.JButton();
        btn2 = new javax.swing.JButton();
        btn3 = new javax.swing.JButton();
        btn4 = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Finalizar visita");

        jLabel1.setText("Financiera");

        jLabel2.setText("Servicio");

        jLabel3.setText("Oficina");

        jLabel4.setText("Administrativa");

        txtFinanciera.setEditable(false);
        txtFinanciera.addActionListener(this::txtFinancieraActionPerformed);

        txtServicio.setEditable(false);

        txtOficina.setEditable(false);

        txtAdministrativa.setEditable(false);

        btn1.setText("Examinar");
        btn1.addActionListener(this::btn1ActionPerformed);

        btn2.setText("Examinar");
        btn2.addActionListener(this::btn2ActionPerformed);

        btn3.setText("Examinar");
        btn3.addActionListener(this::btn3ActionPerformed);

        btn4.setText("Examinar");
        btn4.addActionListener(this::btn4ActionPerformed);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        btnGuardar.setText("Guardar (estado EJECUTADO");
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(189, 189, 189)
                        .addComponent(btnCancelar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGuardar)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(63, 63, 63)
                                .addComponent(txtServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn2))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(63, 63, 63)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtFinanciera, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtAdministrativa, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtOficina, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(38, 38, 38)
                                        .addComponent(btn1)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btn4, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(btn3, javax.swing.GroupLayout.Alignment.TRAILING))))))))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFinanciera, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btn1))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(btn2))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOficina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(btn3))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAdministrativa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(btn4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnGuardar))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtFinancieraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFinancieraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFinancieraActionPerformed

    private void btn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn1ActionPerformed
        File archivo = seleccionarPDF(txtFinanciera);

if (archivo != null) {
    archivoFinanciera = archivo;
}
verificarArchivos();
    }//GEN-LAST:event_btn1ActionPerformed

    private void btn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn2ActionPerformed
        File archivo = seleccionarPDF(txtServicio);

if (archivo != null) {
    archivoServicio = archivo;
}

verificarArchivos();
    }//GEN-LAST:event_btn2ActionPerformed

    private void btn3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn3ActionPerformed
        File archivo = seleccionarPDF(txtOficina);

if (archivo != null) {
    archivoOficina = archivo;
}

verificarArchivos();
    }//GEN-LAST:event_btn3ActionPerformed

    private void btn4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn4ActionPerformed
        File archivo = seleccionarPDF(txtAdministrativa);

if (archivo != null) {
    archivoAdministrativa = archivo;
}

verificarArchivos();
    }//GEN-LAST:event_btn4ActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarVisita();
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
        java.awt.EventQueue.invokeLater(() -> new frmFinalizarVisita().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn1;
    private javax.swing.JButton btn2;
    private javax.swing.JButton btn3;
    private javax.swing.JButton btn4;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtAdministrativa;
    private javax.swing.JTextField txtFinanciera;
    private javax.swing.JTextField txtOficina;
    private javax.swing.JTextField txtServicio;
    // End of variables declaration//GEN-END:variables
}
