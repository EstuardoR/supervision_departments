/*
 * Autor: Andrea Sofia Guerrero Castro
 * Carné: 9490-25-14813
 */
package view;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import util.clsConnection;

public class frmBandejaEjecucion extends javax.swing.JInternalFrame {

    /**
     * Creates new form frmBandejaEjecucion
     */
    public frmBandejaEjecucion() {
        initComponents();
        
configurarTabla();

    btnSupervision.setEnabled(false);
    btnFinalizar.setEnabled(false);
    btnResultados.setEnabled(false);
    btnInforme.setEnabled(false);

    tablaEjecucion.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            verificarSeleccion();
        }
    });

    setCuiSupervisor(util.Sesion.getInstancia().getUsuarioActual().getCui());

    }
    
    private void configurarTabla() {

    javax.swing.table.DefaultTableModel modelo =
        new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Oficina", "Inicio", "Fin", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

    tablaEjecucion.setModel(modelo);

    tablaEjecucion.setSelectionMode(
        javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    tablaEjecucion.setRowSelectionAllowed(true);
    tablaEjecucion.setColumnSelectionAllowed(false);
}
    
    private String cuiSupervisor;

public void setCuiSupervisor(String cuiSupervisor) {
    this.cuiSupervisor = cuiSupervisor;
    cargarSupervisiones();
}

private void cargarSupervisiones() {

    DefaultTableModel modelo =
        (DefaultTableModel) tablaEjecucion.getModel();

    modelo.setRowCount(0);

    String sql = """
        SELECT
            id_supervision,
            oficina_auxiliatura,
            fecha_inicio_comision,
            fecha_fin_comision,
            estado
        FROM dbo.Supervisiones
        WHERE cui_supervisor = ?
        AND estado IN ('APROBADO', 'INICIADO', 'EJECUTADO', 'ACCIONES')
        ORDER BY fecha_inicio_comision DESC
        """;

    if (cuiSupervisor == null || cuiSupervisor.isBlank()) {
        return;
    }

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            JOptionPane.showMessageDialog(this,
                "No se pudo conectar con la base de datos.");
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cuiSupervisor);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Object[] fila = {
                        rs.getInt("id_supervision"),
                        rs.getString("oficina_auxiliatura"),
                        rs.getDate("fecha_inicio_comision"),
                        rs.getDate("fecha_fin_comision"),
                        rs.getString("estado")
                    };

                    modelo.addRow(fila);
                }
            }
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(this,
            "Error al cargar supervisiones: " + e.getMessage());
    }
}

private void verificarSeleccion() {

    int fila = tablaEjecucion.getSelectedRow();

    if (fila == -1) {
        btnSupervision.setEnabled(false);
        btnFinalizar.setEnabled(false);
        btnResultados.setEnabled(false);
        btnInforme.setEnabled(false);
        return;
    }

    int filaModelo = tablaEjecucion.convertRowIndexToModel(fila);

    String estado = tablaEjecucion.getModel()
            .getValueAt(filaModelo, 4).toString();

    btnSupervision.setEnabled(estado.equals("APROBADO"));

    btnFinalizar.setEnabled(estado.equals("INICIADO"));

    btnResultados.setEnabled(estado.equals("EJECUTADO"));

    btnInforme.setEnabled(estado.equals("ACCIONES"));
}

private void btnResultadosActionPerformed(java.awt.event.ActionEvent evt) {

    int idSupervision = obtenerIdSupervision();

    if (idSupervision == -1) {
        return;
    }

    view.frmResultadosSupervision pantalla =
            new view.frmResultadosSupervision(idSupervision);

    getDesktopPane().add(pantalla);
    pantalla.setVisible(true);

    pantalla.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
        public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
            cargarSupervisiones();
        }
    });
}

private void btnInformeActionPerformed(java.awt.event.ActionEvent evt) {

    int idSupervision = obtenerIdSupervision();

    if (idSupervision == -1) {
        return;
    }

    view.frmInformeFinal pantalla =
            new view.frmInformeFinal(idSupervision);

    getDesktopPane().add(pantalla);
    pantalla.setVisible(true);

    pantalla.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
        public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
            cargarSupervisiones();
        }
    });
}

private int obtenerIdSupervision() {

    int fila = tablaEjecucion.getSelectedRow();

    if (fila == -1) {
        JOptionPane.showMessageDialog(this,
                "Seleccione una supervisión.");
        return -1;
    }

    int filaModelo = tablaEjecucion.convertRowIndexToModel(fila);

    return Integer.parseInt(
            tablaEjecucion.getModel()
                    .getValueAt(filaModelo, 0).toString()
    );
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaEjecucion = new javax.swing.JTable();
        btnMenu = new javax.swing.JButton();
        btnSupervision = new javax.swing.JButton();
        btnFinalizar = new javax.swing.JButton();
        btnResultados = new javax.swing.JButton();
        btnInforme = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Bandeja de Ejecución");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Supervisiones asignadas");

        tablaEjecucion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Oficina", "Inicio", "Fin", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tablaEjecucion);

        btnMenu.setText("Menú principal");
        btnMenu.addActionListener(this::btnMenuActionPerformed);

        btnSupervision.setText("Iniciar Supervisión");
        btnSupervision.addActionListener(this::btnSupervisionActionPerformed);

        btnFinalizar.setBackground(new java.awt.Color(51, 153, 255));
        btnFinalizar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizar.setText("Finalizar visita");
        btnFinalizar.addActionListener(this::btnFinalizarActionPerformed);

        btnResultados.setText("Registrar resultados");
        btnResultados.addActionListener(this::btnResultadosActionPerformed);

        btnInforme.setText("Informe final");
        btnInforme.addActionListener(this::btnInformeActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 577, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnMenu)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSupervision)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFinalizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnResultados)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnInforme)))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMenu)
                    .addComponent(btnSupervision)
                    .addComponent(btnFinalizar)
                    .addComponent(btnResultados)
                    .addComponent(btnInforme))
                .addContainerGap(63, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSupervisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSupervisionActionPerformed


int idSupervision = obtenerIdSupervision();

if (idSupervision == -1) {
    return;
}

int respuesta = JOptionPane.showConfirmDialog(
        this,
        "¿Desea iniciar la supervisión No. " + idSupervision + "?",
        "Confirmar inicio",
        JOptionPane.YES_NO_OPTION
);

if (respuesta != JOptionPane.YES_OPTION) {
    return;
}

try {
    dao.SupervisionDAO.cambiarEstadoDelSupervisorAsignado(
            idSupervision,
            "APROBADO",
            "INICIADO",
            "Inicio de supervisión"
    );

    JOptionPane.showMessageDialog(
            this,
            "Supervisión iniciada correctamente."
    );

    cargarSupervisiones();

} catch (dao.SupervisionException e) {

    JOptionPane.showMessageDialog(
            this,
            "Error al iniciar supervisión: " + e.getMessage()
    );
}

    }//GEN-LAST:event_btnSupervisionActionPerformed

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarActionPerformed
        int idSupervision = obtenerIdSupervision();

if (idSupervision == -1) {
    return;
}

int fila = tablaEjecucion.getSelectedRow();
int filaModelo = tablaEjecucion.convertRowIndexToModel(fila);

String estado = tablaEjecucion.getModel()
        .getValueAt(filaModelo, 4).toString();

if (!estado.equals("INICIADO")) {
    JOptionPane.showMessageDialog(this,
            "Solo puede finalizar una supervisión en estado INICIADO.");
    return;
}

frmFinalizarVisita ventana =
        new frmFinalizarVisita(idSupervision);

ventana.addWindowListener(new java.awt.event.WindowAdapter() {
    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
        cargarSupervisiones();
    }
});

ventana.setVisible(true);
    }//GEN-LAST:event_btnFinalizarActionPerformed

    private void btnMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnMenuActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnInforme;
    private javax.swing.JButton btnMenu;
    private javax.swing.JButton btnResultados;
    private javax.swing.JButton btnSupervision;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tablaEjecucion;
    // End of variables declaration//GEN-END:variables
}
