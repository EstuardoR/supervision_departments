/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

/**
 *
 * @author by_estuardo
 */
public class frmPrincipal extends javax.swing.JFrame {

    /**
     * Creates new form frmPrincipal
     */
    public frmPrincipal() {
        initComponents();
        setLocationRelativeTo(null);
        cargarDatosUsuario();
        aplicarControlAcceso();
    }

    private void cargarDatosUsuario() {
        model.Usuario usuario = util.Sesion.getInstancia().getUsuarioActual();
        if (usuario != null) {
            lblUsuario.setText("Usuario: " + usuario.getNombreCompleto());
            lblRol.setText("Rol: " + usuario.getPerfil());
        } else {
            lblUsuario.setText("Usuario: -");
            lblRol.setText("Rol: -");
        }
    }

    private void aplicarControlAcceso() {
        model.Usuario usuario = util.Sesion.getInstancia().getUsuarioActual();
        String perfil = (usuario != null) ? usuario.getPerfil() : null;

        mnuAsignacionSupervision.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_ASIGNACION_SUPERVISION));
        mnuListarSupervisiones.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_BANDEJA_PLANIFICACION));
        mnuEjecucionCampo.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_BANDEJA_EJECUCION));
        mnuResultadosSupervision.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_RESULTADOS_SUPERVISION));
        mnuInformeFinal.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_INFORME_FINAL));
        mnuMatrizSeguimiento.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_MATRIZ_SEGUIMIENTO));
        mnuAcciones.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_ACCIONES));
        mnuIndicadores.setVisible(util.ControlAcceso.tieneAcceso(perfil, util.ControlAcceso.FRM_REPORTES));

        ocultarMenuSiVacio(mnuAdministracion);
        ocultarMenuSiVacio(mnuSupervision);
        ocultarMenuSiVacio(mnuGestion);
        ocultarMenuSiVacio(mnuReportes);
    }

    private void ocultarMenuSiVacio(javax.swing.JMenu menu) {
        boolean algunaOpcionVisible = false;
        for (int i = 0; i < menu.getItemCount(); i++) {
            javax.swing.JMenuItem item = menu.getItem(i);
            if (item != null && item.isVisible()) {
                algunaOpcionVisible = true;
                break;
            }
        }
        menu.setVisible(algunaOpcionVisible);
    }

    private void mnuAsignacionSupervisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAsignacionSupervisionActionPerformed
        view.frmAsignacionSupervision pantalla = new view.frmAsignacionSupervision();
        desktop.add(pantalla);
        pantalla.setVisible(true);
    }//GEN-LAST:event_mnuAsignacionSupervisionActionPerformed

    private void mnuListarSupervisionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuListarSupervisionesActionPerformed
        view.frmBandejaPlanificacion pantalla = new view.frmBandejaPlanificacion();
        desktop.add(pantalla);
        pantalla.setVisible(true);
    }//GEN-LAST:event_mnuListarSupervisionesActionPerformed

    private void mnuIndicadoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuIndicadoresActionPerformed
        view.frmReportes pantalla = new view.frmReportes();
        desktop.add(pantalla);
        pantalla.setVisible(true);
    }//GEN-LAST:event_mnuIndicadoresActionPerformed

    private void mnuEjecucionCampoActionPerformed(java.awt.event.ActionEvent evt) {
        view.frmBandejaEjecucion pantalla = new view.frmBandejaEjecucion();
        desktop.add(pantalla);
        pantalla.setVisible(true);
    }

    private void mnuResultadosSupervisionActionPerformed(java.awt.event.ActionEvent evt) {
        abrirConSeleccionDeSupervision("EJECUTADO", idSupervision -> {
            view.frmResultadosSupervision pantalla = new view.frmResultadosSupervision(idSupervision);
            desktop.add(pantalla);
            pantalla.setVisible(true);
        });
    }

    private void mnuInformeFinalActionPerformed(java.awt.event.ActionEvent evt) {
        abrirConSeleccionDeSupervision("ACCIONES", idSupervision -> {
            view.frmInformeFinal pantalla = new view.frmInformeFinal(idSupervision);
            desktop.add(pantalla);
            pantalla.setVisible(true);
        });
    }

    /**
     * Muestra un selector con las supervisiones del supervisor en sesión que estén en el
     * estado indicado, y abre la pantalla recibida con el ID elegido. Se usa para los accesos
     * de Resultados/Informe final del menú principal, que (a diferencia de Acciones/Matriz de
     * seguimiento) no traen su propia tabla de selección.
     */
    private void abrirConSeleccionDeSupervision(String estado, java.util.function.IntConsumer abrirPantalla) {
        try {
            model.Usuario usuario = util.Sesion.getInstancia().getUsuarioActual();
            String cui = (usuario != null) ? usuario.getCui() : null;
            java.util.List<Object[]> filas = dao.AccionDAO.listarSupervisiones(estado, cui, null, false);
            if (filas.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "No hay supervisiones en estado " + estado + " asignadas a usted.");
                return;
            }
            Object[] opciones = filas.stream()
                    .map(fila -> fila[0] + " - " + fila[1] + " (" + fila[2] + ")")
                    .toArray();
            Object seleccion = javax.swing.JOptionPane.showInputDialog(this,
                    "Seleccione una supervisión:", "Supervisiones en " + estado,
                    javax.swing.JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
            if (seleccion == null) {
                return;
            }
            int indice = java.util.Arrays.asList(opciones).indexOf(seleccion);
            int idSupervision = (Integer) filas.get(indice)[0];
            abrirPantalla.accept(idSupervision);
        } catch (dao.SupervisionException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudieron listar las supervisiones: " + e.getMessage());
        }
    }

    private void mnuMatrizSeguimientoActionPerformed(java.awt.event.ActionEvent evt) {
        view.frmMatrizSeguimiento pantalla = new view.frmMatrizSeguimiento();
        desktop.add(pantalla);
        pantalla.setVisible(true);
    }

    private void mnuAccionesActionPerformed(java.awt.event.ActionEvent evt) {
        view.frmAcciones pantalla = new view.frmAcciones();
        desktop.add(pantalla);
        pantalla.setVisible(true);
    }

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {
        int respuesta = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesión actual?", "Cerrar sesión",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (respuesta != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        util.Sesion.getInstancia().cerrarSesion();
        new view.frmLogin().setVisible(true);
        this.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        mnuAdministracion = new javax.swing.JMenu();
        mnuAcciones = new javax.swing.JMenuItem();
        mnuSupervision = new javax.swing.JMenu();
        mnuAsignacionSupervision = new javax.swing.JMenuItem();
        mnuListarSupervisiones = new javax.swing.JMenuItem();
        mnuGestion = new javax.swing.JMenu();
        mnuEjecucionCampo = new javax.swing.JMenuItem();
        mnuResultadosSupervision = new javax.swing.JMenuItem();
        mnuInformeFinal = new javax.swing.JMenuItem();
        mnuMatrizSeguimiento = new javax.swing.JMenuItem();
        mnuReportes = new javax.swing.JMenu();
        mnuIndicadores = new javax.swing.JMenuItem();
        desktop = new javax.swing.JDesktopPane();
        pnlEstado = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblRol = new javax.swing.JLabel();
        btnCerrarSesion = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mnuAdministracion.setText("Administración");

        mnuAcciones.setText("Acciones");
        mnuAcciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAccionesActionPerformed(evt);
            }
        });
        mnuAdministracion.add(mnuAcciones);

        menuBar.add(mnuAdministracion);

        mnuSupervision.setText("Supervisión");

        mnuAsignacionSupervision.setText("Asignación de supervisión");
        mnuAsignacionSupervision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAsignacionSupervisionActionPerformed(evt);
            }
        });
        mnuSupervision.add(mnuAsignacionSupervision);

        mnuListarSupervisiones.setText("Listar supervisiones");
        mnuListarSupervisiones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuListarSupervisionesActionPerformed(evt);
            }
        });
        mnuSupervision.add(mnuListarSupervisiones);

        menuBar.add(mnuSupervision);

        mnuGestion.setText("Gestión");

        mnuEjecucionCampo.setText("Ejecución de campo");
        mnuEjecucionCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEjecucionCampoActionPerformed(evt);
            }
        });
        mnuGestion.add(mnuEjecucionCampo);

        mnuResultadosSupervision.setText("Resultados de supervisión");
        mnuResultadosSupervision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuResultadosSupervisionActionPerformed(evt);
            }
        });
        mnuGestion.add(mnuResultadosSupervision);

        mnuInformeFinal.setText("Informe final");
        mnuInformeFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuInformeFinalActionPerformed(evt);
            }
        });
        mnuGestion.add(mnuInformeFinal);

        mnuMatrizSeguimiento.setText("Matriz de seguimiento");
        mnuMatrizSeguimiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuMatrizSeguimientoActionPerformed(evt);
            }
        });
        mnuGestion.add(mnuMatrizSeguimiento);

        menuBar.add(mnuGestion);

        mnuReportes.setText("Reportes");

        mnuIndicadores.setText("Indicadores");
        mnuIndicadores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuIndicadoresActionPerformed(evt);
            }
        });
        mnuReportes.add(mnuIndicadores);

        menuBar.add(mnuReportes);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout desktopLayout = new javax.swing.GroupLayout(desktop);
        desktop.setLayout(desktopLayout);
        desktopLayout.setHorizontalGroup(
            desktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 900, Short.MAX_VALUE)
        );
        desktopLayout.setVerticalGroup(
            desktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );

        lblUsuario.setText("Usuario:");

        lblRol.setText("Rol:");

        btnCerrarSesion.setText("Cerrar sesión");
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlEstadoLayout = new javax.swing.GroupLayout(pnlEstado);
        pnlEstado.setLayout(pnlEstadoLayout);
        pnlEstadoLayout.setHorizontalGroup(
            pnlEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadoLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblUsuario)
                .addGap(40, 40, 40)
                .addComponent(lblRol)
                .addContainerGap(100, Short.MAX_VALUE)
                .addComponent(btnCerrarSesion)
                .addGap(10, 10, 10))
        );
        pnlEstadoLayout.setVerticalGroup(
            pnlEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEstadoLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsuario)
                    .addComponent(lblRol)
                    .addComponent(btnCerrarSesion))
                .addGap(6, 6, 6))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
            .addComponent(pnlEstado, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                .addComponent(pnlEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mnuAcciones;
    private javax.swing.JMenu mnuAdministracion;
    private javax.swing.JMenuItem mnuAsignacionSupervision;
    private javax.swing.JMenuItem mnuEjecucionCampo;
    private javax.swing.JMenu mnuGestion;
    private javax.swing.JMenuItem mnuIndicadores;
    private javax.swing.JMenuItem mnuInformeFinal;
    private javax.swing.JMenuItem mnuListarSupervisiones;
    private javax.swing.JMenuItem mnuMatrizSeguimiento;
    private javax.swing.JMenuItem mnuResultadosSupervision;
    private javax.swing.JMenu mnuReportes;
    private javax.swing.JMenu mnuSupervision;
    private javax.swing.JPanel pnlEstado;
    // End of variables declaration//GEN-END:variables
}
