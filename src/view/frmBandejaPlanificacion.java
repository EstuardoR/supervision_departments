/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package view;

/**
 *
 * @author by_estuardo
 */
public class frmBandejaPlanificacion extends javax.swing.JInternalFrame {

    private static final String TODOS = "Todos";
    private static final String TODAS = "Todas";
    private static final java.text.SimpleDateFormat FORMATO_FECHA = new java.text.SimpleDateFormat("dd/MM/yyyy");

    private static final String TIPO_OFICIO_NOMBRAMIENTO = "Oficio de Nombramiento";
    private static final String TIPO_OFICIO_NEGATIVA_VEHICULO = "Oficio de Negativa de Vehículo";
    private static final String TIPO_OFICIO_REPROGRAMACION = "Oficio de Reprogramación";
    private static final String TIPO_OFICIO_AUTORIZACION_VEHICULO = "Oficio de Autorización de Vehículo";

    /** Resultados de la última búsqueda, en el mismo orden que las filas de la tabla (modelo, no vista). */
    private java.util.List<model.SupervisionResumen> resultados = new java.util.ArrayList<>();

    /**
     * Creates new form frmBandejaPlanificacion
     */
    public frmBandejaPlanificacion() {
        initComponents();
        cargarFiltrosFijos();
        cargarSupervisoresFiltro();
        aplicarRestriccionPorRol();
        actualizarBotonesSegunSeleccion();
        buscar();
    }

    /** Llena los combos de filtro que no dependen de la base de datos. */
    private void cargarFiltrosFijos() {
        javax.swing.DefaultComboBoxModel<String> modeloRegion = new javax.swing.DefaultComboBoxModel<>();
        modeloRegion.addElement(TODAS);
        for (int i = 1; i <= 30; i++) {
            modeloRegion.addElement(String.valueOf(i));
        }
        cmbRegion.setModel(modeloRegion);

        cargarComboConTodos(cmbDepartamento, util.Catalogos.departamentos());
        cargarComboConTodos(cmbMunicipio, util.Catalogos.municipios());
        cargarComboConTodos(cmbOficina, util.Catalogos.oficinasAuxiliaturas());

        cmbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{TODOS, "CAMPO", "ESPECÍFICA"}));

        javax.swing.DefaultComboBoxModel<String> modeloEstado = new javax.swing.DefaultComboBoxModel<>();
        modeloEstado.addElement(TODOS);
        for (String estado : dao.SupervisionDAO.ESTADOS_VALIDOS) {
            modeloEstado.addElement(estado);
        }
        cmbEstado.setModel(modeloEstado);
    }

    /** Arma el modelo de un combo de filtro agregando primero la opción "Todos". */
    private void cargarComboConTodos(javax.swing.JComboBox<String> combo, java.util.List<String> valores) {
        javax.swing.DefaultComboBoxModel<String> modelo = new javax.swing.DefaultComboBoxModel<>();
        modelo.addElement(TODOS);
        for (String valor : valores) {
            modelo.addElement(valor);
        }
        combo.setModel(modelo);
    }

    /** Trae de la base de datos los supervisores (SUPERVISOR_DCI) para el combo de filtro. */
    private void cargarSupervisoresFiltro() {
        try {
            java.util.List<model.Usuario> supervisores =
                    new controller.BandejaPlanificacionController().listarSupervisores();
            javax.swing.DefaultComboBoxModel<model.Usuario> modelo = new javax.swing.DefaultComboBoxModel<>();
            model.Usuario todos = new model.Usuario();
            todos.setNombreCompleto(TODOS);
            modelo.addElement(todos);
            for (model.Usuario supervisor : supervisores) {
                modelo.addElement(supervisor);
            }
            cmbSupervisor.setModel(modelo);
        } catch (controller.BandejaPlanificacionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Si el usuario en sesión es SUPERVISOR_DCI, fija el filtro de supervisor en él mismo y lo bloquea. */
    private void aplicarRestriccionPorRol() {
        if (!util.Sesion.getInstancia().tienePerfil("SUPERVISOR_DCI")) {
            return;
        }
        String cuiPropio = util.Sesion.getInstancia().getUsuarioActual().getCui();
        for (int i = 0; i < cmbSupervisor.getItemCount(); i++) {
            model.Usuario usuario = cmbSupervisor.getItemAt(i);
            if (usuario.getCui() != null && usuario.getCui().equals(cuiPropio)) {
                cmbSupervisor.setSelectedIndex(i);
                break;
            }
        }
        cmbSupervisor.setEnabled(false);
    }

    /** Lee los filtros del formulario y ejecuta la búsqueda, mostrando el resultado en la tabla. */
    private void buscar() {
        try {
            model.FiltroSupervision filtro = construirFiltroDesdeFormulario();
            resultados = new controller.BandejaPlanificacionController().buscar(filtro);
            llenarTabla();
            actualizarBotonesSegunSeleccion();
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Fecha inválida",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (controller.BandejaPlanificacionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Arma el objeto de filtros a partir de lo que el usuario haya llenado en el panel de arriba. */
    private model.FiltroSupervision construirFiltroDesdeFormulario() {
        model.FiltroSupervision filtro = new model.FiltroSupervision();
        filtro.setFechaDesde(parsearFecha(txtDesde.getText()));
        filtro.setFechaHasta(parsearFecha(txtHasta.getText()));
        filtro.setRegion(valorSinTodos(cmbRegion, TODAS));
        filtro.setDepartamento(valorSinTodos(cmbDepartamento, TODOS));
        filtro.setMunicipio(valorSinTodos(cmbMunicipio, TODOS));
        filtro.setOficinaAuxiliatura(valorSinTodos(cmbOficina, TODOS));

        String tipo = valorSinTodos(cmbTipo, TODOS);
        filtro.setTipoNombramiento(tipo == null ? null : ("ESPECÍFICA".equals(tipo) ? "ESPECIFICA" : tipo));

        filtro.setEstado(valorSinTodos(cmbEstado, TODOS));

        if (util.Sesion.getInstancia().tienePerfil("SUPERVISOR_DCI")) {
            filtro.setCuiSupervisor(util.Sesion.getInstancia().getUsuarioActual().getCui());
        } else {
            model.Usuario seleccionado = (model.Usuario) cmbSupervisor.getSelectedItem();
            filtro.setCuiSupervisor(seleccionado != null ? seleccionado.getCui() : null);
        }
        return filtro;
    }

    /** Devuelve el valor elegido en un combo de filtro, o null si está en la opción "Todos"/"Todas". */
    private String valorSinTodos(javax.swing.JComboBox<String> combo, String todos) {
        String valor = (String) combo.getSelectedItem();
        return (valor == null || todos.equals(valor)) ? null : valor;
    }

    /** Convierte un texto dd/MM/yyyy a Date; vacío devuelve null (sin filtro); formato inválido lanza error. */
    private java.util.Date parsearFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false);
        try {
            return formato.parse(texto.trim());
        } catch (java.text.ParseException e) {
            throw new IllegalArgumentException("La fecha '" + texto + "' no es válida. Use el formato dd/MM/yyyy.");
        }
    }

    /** Restablece todos los filtros a su valor por defecto y vuelve a buscar. */
    private void limpiarFiltros() {
        txtDesde.setText("");
        txtHasta.setText("");
        cmbRegion.setSelectedIndex(0);
        cmbDepartamento.setSelectedIndex(0);
        cmbMunicipio.setSelectedIndex(0);
        cmbOficina.setSelectedIndex(0);
        cmbTipo.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        if (cmbSupervisor.isEnabled()) {
            cmbSupervisor.setSelectedIndex(0);
        }
        buscar();
    }

    /** Vuelca la lista de resultados en el DefaultTableModel de la tabla. */
    private void llenarTabla() {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblSupervisiones.getModel();
        modelo.setRowCount(0);
        for (model.SupervisionResumen r : resultados) {
            modelo.addRow(new Object[]{
                r.getIdSupervision(),
                r.getRegion(),
                r.getDepartamento(),
                r.getMunicipio(),
                r.getOficinaAuxiliatura(),
                r.getDireccionAdministrativa(),
                r.getDepartamentoAdministrativo(),
                r.getFechaInicioComision() != null ? FORMATO_FECHA.format(r.getFechaInicioComision()) : "",
                r.getFechaFinComision() != null ? FORMATO_FECHA.format(r.getFechaFinComision()) : "",
                r.getNombreSupervisor(),
                r.getEstado()
            });
        }
    }

    /** Devuelve la supervisión seleccionada en la tabla (traduciendo índice de vista a índice de modelo). */
    private model.SupervisionResumen obtenerSeleccionActual() {
        int filaVista = tblSupervisiones.getSelectedRow();
        if (filaVista < 0) {
            return null;
        }
        int filaModelo = tblSupervisiones.convertRowIndexToModel(filaVista);
        return resultados.get(filaModelo);
    }

    /** Activa o desactiva cada botón según el estado de la fila seleccionada y el rol en sesión. */
    private void actualizarBotonesSegunSeleccion() {
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            btnEditar.setEnabled(false);
            btnVerDocumentos.setEnabled(false);
            btnAprobar.setEnabled(false);
            btnRecepcion.setEnabled(false);
            btnReprogramar.setEnabled(false);
            btnHistorial.setEnabled(false);
            return;
        }
        String estado = seleccionado.getEstado();
        boolean esSupervisor = util.Sesion.getInstancia().tienePerfil("SUPERVISOR_DCI");

        btnEditar.setEnabled("PROGRAMADO".equals(estado));
        btnVerDocumentos.setEnabled(true);
        btnAprobar.setEnabled("PROGRAMADO".equals(estado));
        btnRecepcion.setEnabled("APROBADO".equals(estado) && esSupervisor);
        btnReprogramar.setEnabled("PROGRAMADO".equals(estado));
        btnHistorial.setEnabled(true);
    }

    /** Busca y abre el documento del tipo indicado; avisa si no hay ninguno registrado. */
    private void abrirDocumento(String tipoDocumento) {
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            return;
        }
        try {
            String ruta = new controller.BandejaPlanificacionController()
                    .buscarRutaDocumento(seleccionado.getIdSupervision(), tipoDocumento);
            java.io.File archivo = (ruta == null || ruta.isEmpty()) ? null : new java.io.File(ruta);
            if (archivo == null || !archivo.exists()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Documento no disponible", "Documentos",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            java.awt.Desktop.getDesktop().open(archivo);
        } catch (controller.BandejaPlanificacionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo abrir el documento: " + ex.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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

        popupDocumentos = new javax.swing.JPopupMenu();
        mnuOficioNombramiento = new javax.swing.JMenuItem();
        mnuOficioNegativaVehiculo = new javax.swing.JMenuItem();
        mnuOficioReprogramacion = new javax.swing.JMenuItem();
        mnuOficioAutorizacionVehiculo = new javax.swing.JMenuItem();
        lblDesde = new javax.swing.JLabel();
        txtDesde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        txtHasta = new javax.swing.JTextField();
        lblRegion = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        lblDepartamento = new javax.swing.JLabel();
        cmbDepartamento = new javax.swing.JComboBox<>();
        lblMunicipio = new javax.swing.JLabel();
        cmbMunicipio = new javax.swing.JComboBox<>();
        lblOficina = new javax.swing.JLabel();
        cmbOficina = new javax.swing.JComboBox<>();
        lblTipo = new javax.swing.JLabel();
        cmbTipo = new javax.swing.JComboBox<>();
        lblSupervisor = new javax.swing.JLabel();
        cmbSupervisor = new javax.swing.JComboBox<>();
        lblEstado = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox<>();
        btnLimpiar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tblSupervisiones = new javax.swing.JTable();
        btnEditar = new javax.swing.JButton();
        btnVerDocumentos = new javax.swing.JButton();
        btnAprobar = new javax.swing.JButton();
        btnRecepcion = new javax.swing.JButton();
        btnReprogramar = new javax.swing.JButton();
        btnHistorial = new javax.swing.JButton();

        mnuOficioNombramiento.setText("Oficio de Nombramiento");
        mnuOficioNombramiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOficioNombramientoActionPerformed(evt);
            }
        });
        popupDocumentos.add(mnuOficioNombramiento);

        mnuOficioNegativaVehiculo.setText("Oficio de Negativa de Vehículo");
        mnuOficioNegativaVehiculo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOficioNegativaVehiculoActionPerformed(evt);
            }
        });
        popupDocumentos.add(mnuOficioNegativaVehiculo);

        mnuOficioReprogramacion.setText("Oficio de Reprogramación");
        mnuOficioReprogramacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOficioReprogramacionActionPerformed(evt);
            }
        });
        popupDocumentos.add(mnuOficioReprogramacion);

        mnuOficioAutorizacionVehiculo.setText("Oficio de Autorización de Vehículo");
        mnuOficioAutorizacionVehiculo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOficioAutorizacionVehiculoActionPerformed(evt);
            }
        });
        popupDocumentos.add(mnuOficioAutorizacionVehiculo);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Bandeja de Planificación");

        lblDesde.setText("Desde");

        lblHasta.setText("Hasta");

        lblRegion.setText("Región");

        lblDepartamento.setText("Departamento");

        lblMunicipio.setText("Municipio");

        lblOficina.setText("Oficina");

        lblTipo.setText("Tipo");

        lblSupervisor.setText("Supervisor");

        lblEstado.setText("Estado");

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        tblSupervisiones.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Región", "Departamento", "Municipio", "Oficina", "Dirección administrativa", "Departamento administrativo", "Fecha inicio", "Fecha fin", "Supervisor", "Estado"}
        ) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tblSupervisiones.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblSupervisiones.setAutoCreateRowSorter(true);
        tblSupervisiones.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                tblSupervisionesValueChanged(evt);
            }
        });
        scrollTabla.setViewportView(tblSupervisiones);

        btnEditar.setText("Editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnVerDocumentos.setText("Ver documentos");
        btnVerDocumentos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerDocumentosActionPerformed(evt);
            }
        });

        btnAprobar.setText("Aprobar");
        btnAprobar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAprobarActionPerformed(evt);
            }
        });

        btnRecepcion.setText("Recepción");
        btnRecepcion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecepcionActionPerformed(evt);
            }
        });

        btnReprogramar.setText("Reprogramar / Anular");
        btnReprogramar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReprogramarActionPerformed(evt);
            }
        });

        btnHistorial.setText("Historial");
        btnHistorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistorialActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDesde)
                            .addComponent(txtDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHasta)
                            .addComponent(txtHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRegion)
                            .addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDepartamento)
                            .addComponent(cmbDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMunicipio)
                            .addComponent(cmbMunicipio, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblOficina)
                            .addComponent(cmbOficina, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTipo)
                            .addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSupervisor)
                            .addComponent(cmbSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEstado)
                            .addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnLimpiar)
                        .addGap(10, 10, 10)
                        .addComponent(btnBuscar))
                    .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnEditar)
                        .addGap(10, 10, 10)
                        .addComponent(btnVerDocumentos)
                        .addGap(10, 10, 10)
                        .addComponent(btnAprobar)
                        .addGap(10, 10, 10)
                        .addComponent(btnRecepcion)
                        .addGap(10, 10, 10)
                        .addComponent(btnReprogramar)
                        .addGap(10, 10, 10)
                        .addComponent(btnHistorial)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addComponent(lblDesde).addGap(4, 4, 4).addComponent(txtDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblHasta).addGap(4, 4, 4).addComponent(txtHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblRegion).addGap(4, 4, 4).addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblDepartamento).addGap(4, 4, 4).addComponent(cmbDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblMunicipio).addGap(4, 4, 4).addComponent(cmbMunicipio, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblOficina).addGap(4, 4, 4).addComponent(cmbOficina, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addGroup(layout.createSequentialGroup().addComponent(lblTipo).addGap(4, 4, 4).addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblSupervisor).addGap(4, 4, 4).addComponent(cmbSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblEstado).addGap(4, 4, 4).addComponent(cmbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(btnBuscar))
                    .addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(btnLimpiar)))
                .addGap(20, 20, 20)
                .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditar)
                    .addComponent(btnVerDocumentos)
                    .addComponent(btnAprobar)
                    .addComponent(btnRecepcion)
                    .addComponent(btnReprogramar)
                    .addComponent(btnHistorial))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void tblSupervisionesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_tblSupervisionesValueChanged
        if (!evt.getValueIsAdjusting()) {
            actualizarBotonesSegunSeleccion();
        }
    }//GEN-LAST:event_tblSupervisionesValueChanged

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            return;
        }
        view.frmAsignacionSupervision pantalla = new view.frmAsignacionSupervision(seleccionado.getIdSupervision());
        getDesktopPane().add(pantalla);
        pantalla.setVisible(true);
        pantalla.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                buscar();
            }
        });
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnVerDocumentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDocumentosActionPerformed
        if (obtenerSeleccionActual() == null) {
            return;
        }
        popupDocumentos.show(btnVerDocumentos, 0, btnVerDocumentos.getHeight());
    }//GEN-LAST:event_btnVerDocumentosActionPerformed

    private void btnAprobarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAprobarActionPerformed
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            return;
        }
        // No existe un FirmaService en el proyecto: se usa un JOptionPane de confirmación como firma simulada.
        int opcion = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Está seguro de FIRMAR el nombramiento?", "Confirmar aprobación",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (opcion != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try {
            new controller.BandejaPlanificacionController().aprobar(seleccionado.getIdSupervision());
            buscar();
        } catch (controller.BandejaPlanificacionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAprobarActionPerformed

    private void btnRecepcionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecepcionActionPerformed
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            return;
        }
        try {
            boolean enviado = new controller.BandejaPlanificacionController()
                    .registrarRecepcion(seleccionado.getIdSupervision());
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Recepción registrada.\n"
                    + (enviado ? "Se notificó por correo." : "No se pudo enviar el correo de notificación."),
                    "Éxito", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            buscar();
        } catch (controller.BandejaPlanificacionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRecepcionActionPerformed

    private void btnReprogramarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReprogramarActionPerformed
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            return;
        }
        java.awt.Frame ventanaPrincipal = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        view.dlgReprogramarAnular dialogo =
                new view.dlgReprogramarAnular(ventanaPrincipal, seleccionado.getIdSupervision());
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            buscar();
        }
    }//GEN-LAST:event_btnReprogramarActionPerformed

    private void btnHistorialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistorialActionPerformed
        model.SupervisionResumen seleccionado = obtenerSeleccionActual();
        if (seleccionado == null) {
            return;
        }
        java.awt.Frame ventanaPrincipal = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        new view.dlgHistorialSupervision(ventanaPrincipal, seleccionado.getIdSupervision()).setVisible(true);
    }//GEN-LAST:event_btnHistorialActionPerformed

    private void mnuOficioNombramientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOficioNombramientoActionPerformed
        abrirDocumento(TIPO_OFICIO_NOMBRAMIENTO);
    }//GEN-LAST:event_mnuOficioNombramientoActionPerformed

    private void mnuOficioNegativaVehiculoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOficioNegativaVehiculoActionPerformed
        abrirDocumento(TIPO_OFICIO_NEGATIVA_VEHICULO);
    }//GEN-LAST:event_mnuOficioNegativaVehiculoActionPerformed

    private void mnuOficioReprogramacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOficioReprogramacionActionPerformed
        abrirDocumento(TIPO_OFICIO_REPROGRAMACION);
    }//GEN-LAST:event_mnuOficioReprogramacionActionPerformed

    private void mnuOficioAutorizacionVehiculoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOficioAutorizacionVehiculoActionPerformed
        abrirDocumento(TIPO_OFICIO_AUTORIZACION_VEHICULO);
    }//GEN-LAST:event_mnuOficioAutorizacionVehiculoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAprobar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnHistorial;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRecepcion;
    private javax.swing.JButton btnReprogramar;
    private javax.swing.JButton btnVerDocumentos;
    private javax.swing.JComboBox<String> cmbDepartamento;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<String> cmbMunicipio;
    private javax.swing.JComboBox<String> cmbOficina;
    private javax.swing.JComboBox<String> cmbRegion;
    private javax.swing.JComboBox<model.Usuario> cmbSupervisor;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JLabel lblDepartamento;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblMunicipio;
    private javax.swing.JLabel lblOficina;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JLabel lblSupervisor;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JMenuItem mnuOficioAutorizacionVehiculo;
    private javax.swing.JMenuItem mnuOficioNegativaVehiculo;
    private javax.swing.JMenuItem mnuOficioNombramiento;
    private javax.swing.JMenuItem mnuOficioReprogramacion;
    private javax.swing.JPopupMenu popupDocumentos;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tblSupervisiones;
    private javax.swing.JTextField txtDesde;
    private javax.swing.JTextField txtHasta;
    // End of variables declaration//GEN-END:variables
}
