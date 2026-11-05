/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package view;

/**
 *
 * @author by_estuardo
 */
public class frmAsignacionSupervision extends javax.swing.JInternalFrame {

    /** Texto usado como opción "sin seleccionar" en los combos de lugar. */
    private static final String PLACEHOLDER = "-- Seleccione --";

    /** Null cuando la pantalla está creando una asignación nueva; con valor cuando está editando una existente. */
    private Integer idSupervision;

    /**
     * Creates new form frmAsignacionSupervision
     */
    public frmAsignacionSupervision() {
        initComponents();
        configurarSpinnersDeFecha();
        cargarCatalogosFijos();
        cargarSupervisores();
        actualizarHabilitacionPorTipo();
    }

    /** Abre la pantalla en modo edición, precargada con los datos de la supervisión indicada. */
    public frmAsignacionSupervision(int idSupervision) {
        this();
        this.idSupervision = idSupervision;
        setTitle("Asignación de Supervisión – Editar asignación");
        btnGuardar.setText("Actualizar");
        cargarDatosParaEdicion();
    }

    /** Trae la supervisión a editar y llena con sus datos todos los campos del formulario. */
    private void cargarDatosParaEdicion() {
        try {
            model.Supervision s = new controller.AsignacionSupervisionController().obtenerSupervisionPorId(idSupervision);
            if (s == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "No se encontró la supervisión.", "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            seleccionarSupervisorPorCui(s.getCuiSupervisor());
            autocompletarDatosSupervisor();
            cmbRegion.setSelectedItem(s.getRegion());
            cmbOrden.setSelectedItem(s.getOrdenAsignacion());
            spnFechaInicio.setValue(s.getFechaInicioComision());
            spnFechaFin.setValue(s.getFechaFinComision());
            spnAlcanceInicial.setValue(s.getFechaAlcanceInicial());
            spnAlcanceFinal.setValue(s.getFechaAlcanceFinal());
            rdoCampo.setSelected("CAMPO".equals(s.getTipoNombramiento()));
            rdoEspecifica.setSelected(!"CAMPO".equals(s.getTipoNombramiento()));
            actualizarHabilitacionPorTipo();
            seleccionarSiNoVacio(cmbDireccionAdmin, s.getDireccionAdministrativa());
            seleccionarSiNoVacio(cmbDeptoAdmin, s.getDepartamentoAdministrativo());
            seleccionarSiNoVacio(cmbDepartamento, s.getDepartamento());
            seleccionarSiNoVacio(cmbMunicipio, s.getMunicipio());
            seleccionarSiNoVacio(cmbOficina, s.getOficinaAuxiliatura());
            cmbViaticos.setSelectedItem(s.isAutorizacionViaticos() ? "SI" : "NO");
            cmbVehiculo.setSelectedItem(s.isVehiculoPropio() ? "SI" : "NO");
            txtObservaciones.setText(s.getObservaciones());
        } catch (controller.AsignacionSupervisionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Busca en cmbSupervisor el usuario con ese CUI y lo deja seleccionado. */
    private void seleccionarSupervisorPorCui(String cui) {
        for (int i = 0; i < cmbSupervisor.getItemCount(); i++) {
            model.Usuario usuario = cmbSupervisor.getItemAt(i);
            if (usuario != null && usuario.getCui() != null && usuario.getCui().equals(cui)) {
                cmbSupervisor.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Selecciona un valor en un combo de lugar solo si no viene vacío (si no, se deja en el PLACEHOLDER). */
    private void seleccionarSiNoVacio(javax.swing.JComboBox<String> combo, String valor) {
        if (valor != null && !valor.isEmpty()) {
            combo.setSelectedItem(valor);
        }
    }

    /** Configura los 4 JSpinner de fecha para usar SpinnerDateModel con formato dd/MM/yyyy. */
    private void configurarSpinnersDeFecha() {
        java.util.Date hoy = new java.util.Date();
        spnFechaInicio.setModel(new javax.swing.SpinnerDateModel(hoy, null, null, java.util.Calendar.DAY_OF_MONTH));
        spnFechaInicio.setEditor(new javax.swing.JSpinner.DateEditor(spnFechaInicio, "dd/MM/yyyy"));
        spnFechaFin.setModel(new javax.swing.SpinnerDateModel(hoy, null, null, java.util.Calendar.DAY_OF_MONTH));
        spnFechaFin.setEditor(new javax.swing.JSpinner.DateEditor(spnFechaFin, "dd/MM/yyyy"));
        spnAlcanceInicial.setModel(new javax.swing.SpinnerDateModel(hoy, null, null, java.util.Calendar.DAY_OF_MONTH));
        spnAlcanceInicial.setEditor(new javax.swing.JSpinner.DateEditor(spnAlcanceInicial, "dd/MM/yyyy"));
        spnAlcanceFinal.setModel(new javax.swing.SpinnerDateModel(hoy, null, null, java.util.Calendar.DAY_OF_MONTH));
        spnAlcanceFinal.setEditor(new javax.swing.JSpinner.DateEditor(spnAlcanceFinal, "dd/MM/yyyy"));
    }

    /** Llena los combos que no dependen de la base de datos: región, orden, SI/NO y los catálogos de lugar. */
    private void cargarCatalogosFijos() {
        javax.swing.DefaultComboBoxModel<String> modeloRegion = new javax.swing.DefaultComboBoxModel<>();
        for (int i = 1; i <= 30; i++) {
            modeloRegion.addElement(String.valueOf(i));
        }
        cmbRegion.setModel(modeloRegion);

        javax.swing.DefaultComboBoxModel<String> modeloOrden = new javax.swing.DefaultComboBoxModel<>();
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            modeloOrden.addElement(String.valueOf(letra));
        }
        cmbOrden.setModel(modeloOrden);

        cmbViaticos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"SI", "NO"}));
        cmbViaticos.setSelectedItem("NO");
        cmbVehiculo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"SI", "NO"}));
        cmbVehiculo.setSelectedItem("NO");

        cargarComboDeLugar(cmbDireccionAdmin, util.Catalogos.direccionesAdministrativas());
        cargarComboDeLugar(cmbDeptoAdmin, util.Catalogos.departamentosAdministrativos());
        cargarComboDeLugar(cmbDepartamento, util.Catalogos.departamentos());
        cargarComboDeLugar(cmbMunicipio, util.Catalogos.municipios());
        cargarComboDeLugar(cmbOficina, util.Catalogos.oficinasAuxiliaturas());
    }

    /** Arma el modelo de un combo de lugar agregando primero la opción en blanco (PLACEHOLDER). */
    private void cargarComboDeLugar(javax.swing.JComboBox<String> combo, java.util.List<String> valores) {
        javax.swing.DefaultComboBoxModel<String> modelo = new javax.swing.DefaultComboBoxModel<>();
        modelo.addElement(PLACEHOLDER);
        for (String valor : valores) {
            modelo.addElement(valor);
        }
        combo.setModel(modelo);
    }

    /** Trae de la base de datos los usuarios con perfil SUPERVISOR_DCI y llena cmbSupervisor. */
    private void cargarSupervisores() {
        try {
            java.util.List<model.Usuario> supervisores =
                    new controller.AsignacionSupervisionController().listarSupervisores();
            javax.swing.DefaultComboBoxModel<model.Usuario> modelo = new javax.swing.DefaultComboBoxModel<>();
            for (model.Usuario supervisor : supervisores) {
                modelo.addElement(supervisor);
            }
            cmbSupervisor.setModel(modelo);
            if (!supervisores.isEmpty()) {
                autocompletarDatosSupervisor();
            }
        } catch (controller.AsignacionSupervisionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Busca los datos de RRHH del supervisor seleccionado y llena los 4 campos grises de solo lectura. */
    private void autocompletarDatosSupervisor() {
        model.Usuario seleccionado = (model.Usuario) cmbSupervisor.getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        try {
            model.Usuario completo =
                    new controller.AsignacionSupervisionController().obtenerDatosSupervisor(seleccionado.getCui());
            if (completo != null) {
                txtDireccion.setText(completo.getDireccionRrhh());
                txtUnidad.setText(completo.getUnidadRrhh());
                txtPuesto.setText(completo.getPuestoRrhh());
                txtCodigo.setText(completo.getCodigoEmpleado());
            }
        } catch (controller.AsignacionSupervisionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Habilita/deshabilita las listas de lugar según el tipo de nombramiento elegido:
     * CAMPO habilita Departamento/Municipio/Oficina; ESPECÍFICA habilita Dirección/Depto. admin.
     * Las listas que quedan deshabilitadas se limpian (vuelven al PLACEHOLDER).
     */
    private void actualizarHabilitacionPorTipo() {
        boolean esCampo = rdoCampo.isSelected();

        cmbDepartamento.setEnabled(esCampo);
        cmbMunicipio.setEnabled(esCampo);
        cmbOficina.setEnabled(esCampo);
        cmbDireccionAdmin.setEnabled(!esCampo);
        cmbDeptoAdmin.setEnabled(!esCampo);

        if (esCampo) {
            cmbDireccionAdmin.setSelectedItem(PLACEHOLDER);
            cmbDeptoAdmin.setSelectedItem(PLACEHOLDER);
        } else {
            cmbDepartamento.setSelectedItem(PLACEHOLDER);
            cmbMunicipio.setSelectedItem(PLACEHOLDER);
            cmbOficina.setSelectedItem(PLACEHOLDER);
        }
    }

    /** Lee todos los campos del formulario y arma el objeto Supervision que se enviará al controlador. */
    private model.Supervision construirSupervisionDesdeFormulario() {
        model.Supervision s = new model.Supervision();
        model.Usuario supervisor = (model.Usuario) cmbSupervisor.getSelectedItem();
        s.setCuiSupervisor(supervisor != null ? supervisor.getCui() : null);
        s.setRegion((String) cmbRegion.getSelectedItem());
        s.setOrdenAsignacion((String) cmbOrden.getSelectedItem());
        s.setFechaInicioComision((java.util.Date) spnFechaInicio.getValue());
        s.setFechaFinComision((java.util.Date) spnFechaFin.getValue());
        s.setFechaAlcanceInicial((java.util.Date) spnAlcanceInicial.getValue());
        s.setFechaAlcanceFinal((java.util.Date) spnAlcanceFinal.getValue());
        s.setTipoNombramiento(rdoCampo.isSelected() ? "CAMPO" : "ESPECIFICA");
        s.setDireccionAdministrativa(valorDeLugar(cmbDireccionAdmin));
        s.setDepartamentoAdministrativo(valorDeLugar(cmbDeptoAdmin));
        s.setDepartamento(valorDeLugar(cmbDepartamento));
        s.setMunicipio(valorDeLugar(cmbMunicipio));
        s.setOficinaAuxiliatura(valorDeLugar(cmbOficina));
        s.setAutorizacionViaticos("SI".equals(cmbViaticos.getSelectedItem()));
        s.setVehiculoPropio("SI".equals(cmbVehiculo.getSelectedItem()));
        s.setObservaciones(txtObservaciones.getText());
        return s;
    }

    /** Devuelve el valor elegido en un combo de lugar, o null si quedó en el PLACEHOLDER (sin seleccionar). */
    private String valorDeLugar(javax.swing.JComboBox<String> combo) {
        String valor = (String) combo.getSelectedItem();
        return (valor == null || PLACEHOLDER.equals(valor)) ? null : valor;
    }

    /** Vuelve a dejar el formulario en su estado inicial, listo para capturar una nueva asignación. */
    private void limpiarFormulario() {
        if (cmbSupervisor.getItemCount() > 0) {
            cmbSupervisor.setSelectedIndex(0);
            autocompletarDatosSupervisor();
        }
        cmbRegion.setSelectedIndex(0);
        cmbOrden.setSelectedIndex(0);
        java.util.Date hoy = new java.util.Date();
        spnFechaInicio.setValue(hoy);
        spnFechaFin.setValue(hoy);
        spnAlcanceInicial.setValue(hoy);
        spnAlcanceFinal.setValue(hoy);
        cmbViaticos.setSelectedItem("NO");
        cmbVehiculo.setSelectedItem("NO");
        rdoCampo.setSelected(true);
        actualizarHabilitacionPorTipo();
        txtObservaciones.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpTipoNombramiento = new javax.swing.ButtonGroup();
        lblSeccionSupervisor = new javax.swing.JLabel();
        sepSupervisor = new javax.swing.JSeparator();
        lblSupervisor = new javax.swing.JLabel();
        cmbSupervisor = new javax.swing.JComboBox<>();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        lblUnidad = new javax.swing.JLabel();
        txtUnidad = new javax.swing.JTextField();
        lblPuesto = new javax.swing.JLabel();
        txtPuesto = new javax.swing.JTextField();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblSeccionParametros = new javax.swing.JLabel();
        sepParametros = new javax.swing.JSeparator();
        lblRegion = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        lblOrden = new javax.swing.JLabel();
        cmbOrden = new javax.swing.JComboBox<>();
        lblFechaInicio = new javax.swing.JLabel();
        spnFechaInicio = new javax.swing.JSpinner();
        lblFechaFin = new javax.swing.JLabel();
        spnFechaFin = new javax.swing.JSpinner();
        lblAlcanceInicial = new javax.swing.JLabel();
        spnAlcanceInicial = new javax.swing.JSpinner();
        lblAlcanceFinal = new javax.swing.JLabel();
        spnAlcanceFinal = new javax.swing.JSpinner();
        lblViaticos = new javax.swing.JLabel();
        cmbViaticos = new javax.swing.JComboBox<>();
        lblVehiculo = new javax.swing.JLabel();
        cmbVehiculo = new javax.swing.JComboBox<>();
        lblSeccionTipo = new javax.swing.JLabel();
        sepTipo = new javax.swing.JSeparator();
        rdoEspecifica = new javax.swing.JRadioButton();
        rdoCampo = new javax.swing.JRadioButton();
        lblDireccionAdmin = new javax.swing.JLabel();
        cmbDireccionAdmin = new javax.swing.JComboBox<>();
        lblDeptoAdmin = new javax.swing.JLabel();
        cmbDeptoAdmin = new javax.swing.JComboBox<>();
        lblDepartamento = new javax.swing.JLabel();
        cmbDepartamento = new javax.swing.JComboBox<>();
        lblMunicipio = new javax.swing.JLabel();
        cmbMunicipio = new javax.swing.JComboBox<>();
        lblOficina = new javax.swing.JLabel();
        cmbOficina = new javax.swing.JComboBox<>();
        lblObservaciones = new javax.swing.JLabel();
        scrollObservaciones = new javax.swing.JScrollPane();
        txtObservaciones = new javax.swing.JTextArea();
        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Asignación de Supervisión – Nueva asignación");

        lblSeccionSupervisor.setFont(lblSeccionSupervisor.getFont().deriveFont(lblSeccionSupervisor.getFont().getStyle() | java.awt.Font.BOLD));
        lblSeccionSupervisor.setText("Datos del supervisor (autocompletados desde RRHH)");

        lblSupervisor.setText("Supervisor DCI");

        cmbSupervisor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSupervisorActionPerformed(evt);
            }
        });

        lblDireccion.setText("Dirección");

        txtDireccion.setEditable(false);
        txtDireccion.setBackground(new java.awt.Color(230, 230, 230));

        lblUnidad.setText("Unidad");

        txtUnidad.setEditable(false);
        txtUnidad.setBackground(new java.awt.Color(230, 230, 230));

        lblPuesto.setText("Puesto");

        txtPuesto.setEditable(false);
        txtPuesto.setBackground(new java.awt.Color(230, 230, 230));

        lblCodigo.setText("Código de empleado");

        txtCodigo.setEditable(false);
        txtCodigo.setBackground(new java.awt.Color(230, 230, 230));

        lblSeccionParametros.setFont(lblSeccionParametros.getFont().deriveFont(lblSeccionParametros.getFont().getStyle() | java.awt.Font.BOLD));
        lblSeccionParametros.setText("Parámetros de la asignación");

        lblRegion.setText("Región");

        lblOrden.setText("Orden");

        lblFechaInicio.setText("Fecha inicio");

        lblFechaFin.setText("Fecha fin (máx. 5 días hábiles)");

        lblAlcanceInicial.setText("Alcance inicial");

        lblAlcanceFinal.setText("Alcance final");

        lblViaticos.setText("Viáticos");

        lblVehiculo.setText("Vehículo propio");

        lblSeccionTipo.setFont(lblSeccionTipo.getFont().deriveFont(lblSeccionTipo.getFont().getStyle() | java.awt.Font.BOLD));
        lblSeccionTipo.setText("Tipo de nombramiento");

        grpTipoNombramiento.add(rdoEspecifica);
        rdoEspecifica.setText("ESPECÍFICA");
        rdoEspecifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoEspecificaActionPerformed(evt);
            }
        });

        grpTipoNombramiento.add(rdoCampo);
        rdoCampo.setSelected(true);
        rdoCampo.setText("CAMPO");
        rdoCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoCampoActionPerformed(evt);
            }
        });

        lblDireccionAdmin.setText("Dirección admin.");

        lblDeptoAdmin.setText("Depto. admin.");

        lblDepartamento.setText("Departamento");

        lblMunicipio.setText("Municipio");

        lblOficina.setText("Oficina / Auxiliatura");

        lblObservaciones.setText("Observaciones");

        txtObservaciones.setColumns(20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setRows(4);
        txtObservaciones.setWrapStyleWord(true);
        scrollObservaciones.setViewportView(txtObservaciones);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnGuardar.setText("Guardar (estado PROGRAMADO)");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSeccionSupervisor)
                    .addComponent(sepSupervisor, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSupervisor)
                            .addComponent(cmbSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDireccion)
                            .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUnidad)
                            .addComponent(txtUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPuesto)
                            .addComponent(txtPuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCodigo)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblSeccionParametros)
                    .addComponent(sepParametros, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRegion)
                            .addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblOrden)
                            .addComponent(cmbOrden, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaInicio)
                            .addComponent(spnFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaFin)
                            .addComponent(spnFechaFin, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAlcanceInicial)
                            .addComponent(spnAlcanceInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAlcanceFinal)
                            .addComponent(spnAlcanceFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblViaticos)
                            .addComponent(cmbViaticos, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblVehiculo)
                            .addComponent(cmbVehiculo, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblSeccionTipo)
                    .addComponent(sepTipo, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rdoEspecifica)
                        .addGap(24, 24, 24)
                        .addComponent(rdoCampo))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDireccionAdmin)
                            .addComponent(cmbDireccionAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDeptoAdmin)
                            .addComponent(cmbDeptoAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDepartamento)
                            .addComponent(cmbDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMunicipio)
                            .addComponent(cmbMunicipio, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblOficina)
                            .addComponent(cmbOficina, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblObservaciones)
                    .addComponent(scrollObservaciones, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancelar)
                        .addGap(18, 18, 18)
                        .addComponent(btnGuardar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSeccionSupervisor)
                .addGap(8, 8, 8)
                .addComponent(sepSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addComponent(lblSupervisor).addGap(4, 4, 4).addComponent(cmbSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblDireccion).addGap(4, 4, 4).addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblUnidad).addGap(4, 4, 4).addComponent(txtUnidad, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblPuesto).addGap(4, 4, 4).addComponent(txtPuesto, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblCodigo).addGap(4, 4, 4).addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addComponent(lblSeccionParametros)
                .addGap(8, 8, 8)
                .addComponent(sepParametros, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addComponent(lblRegion).addGap(4, 4, 4).addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblOrden).addGap(4, 4, 4).addComponent(cmbOrden, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblFechaInicio).addGap(4, 4, 4).addComponent(spnFechaInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblFechaFin).addGap(4, 4, 4).addComponent(spnFechaFin, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addComponent(lblAlcanceInicial).addGap(4, 4, 4).addComponent(spnAlcanceInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblAlcanceFinal).addGap(4, 4, 4).addComponent(spnAlcanceFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblViaticos).addGap(4, 4, 4).addComponent(cmbViaticos, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblVehiculo).addGap(4, 4, 4).addComponent(cmbVehiculo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addComponent(lblSeccionTipo)
                .addGap(8, 8, 8)
                .addComponent(sepTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoEspecifica)
                    .addComponent(rdoCampo))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addComponent(lblDireccionAdmin).addGap(4, 4, 4).addComponent(cmbDireccionAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblDeptoAdmin).addGap(4, 4, 4).addComponent(cmbDeptoAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblDepartamento).addGap(4, 4, 4).addComponent(cmbDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblMunicipio).addGap(4, 4, 4).addComponent(cmbMunicipio, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblOficina).addGap(4, 4, 4).addComponent(cmbOficina, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20)
                .addComponent(lblObservaciones)
                .addGap(4, 4, 4)
                .addComponent(scrollObservaciones, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnGuardar))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbSupervisorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSupervisorActionPerformed
        autocompletarDatosSupervisor();
    }//GEN-LAST:event_cmbSupervisorActionPerformed

    private void rdoEspecificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoEspecificaActionPerformed
        actualizarHabilitacionPorTipo();
    }//GEN-LAST:event_rdoEspecificaActionPerformed

    private void rdoCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoCampoActionPerformed
        actualizarHabilitacionPorTipo();
    }//GEN-LAST:event_rdoCampoActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        model.Supervision supervision = construirSupervisionDesdeFormulario();
        controller.AsignacionSupervisionController controlador = new controller.AsignacionSupervisionController();
        try {
            if (idSupervision == null) {
                controlador.guardarAsignacion(supervision);
                javax.swing.JOptionPane.showMessageDialog(this, "Asignación guardada", "Éxito",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
            } else {
                supervision.setIdSupervision(idSupervision);
                controlador.actualizarAsignacion(supervision);
                javax.swing.JOptionPane.showMessageDialog(this, "Asignación actualizada", "Éxito",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (controller.AsignacionSupervisionException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JComboBox<String> cmbDeptoAdmin;
    private javax.swing.JComboBox<String> cmbDepartamento;
    private javax.swing.JComboBox<String> cmbDireccionAdmin;
    private javax.swing.JComboBox<String> cmbMunicipio;
    private javax.swing.JComboBox<String> cmbOficina;
    private javax.swing.JComboBox<String> cmbOrden;
    private javax.swing.JComboBox<String> cmbRegion;
    private javax.swing.JComboBox<model.Usuario> cmbSupervisor;
    private javax.swing.JComboBox<String> cmbVehiculo;
    private javax.swing.JComboBox<String> cmbViaticos;
    private javax.swing.ButtonGroup grpTipoNombramiento;
    private javax.swing.JLabel lblAlcanceFinal;
    private javax.swing.JLabel lblAlcanceInicial;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblDepartamento;
    private javax.swing.JLabel lblDeptoAdmin;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblDireccionAdmin;
    private javax.swing.JLabel lblFechaFin;
    private javax.swing.JLabel lblFechaInicio;
    private javax.swing.JLabel lblMunicipio;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblOficina;
    private javax.swing.JLabel lblOrden;
    private javax.swing.JLabel lblPuesto;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JLabel lblSeccionParametros;
    private javax.swing.JLabel lblSeccionSupervisor;
    private javax.swing.JLabel lblSeccionTipo;
    private javax.swing.JLabel lblSupervisor;
    private javax.swing.JLabel lblUnidad;
    private javax.swing.JLabel lblVehiculo;
    private javax.swing.JLabel lblViaticos;
    private javax.swing.JRadioButton rdoCampo;
    private javax.swing.JRadioButton rdoEspecifica;
    private javax.swing.JScrollPane scrollObservaciones;
    private javax.swing.JSeparator sepParametros;
    private javax.swing.JSeparator sepSupervisor;
    private javax.swing.JSeparator sepTipo;
    private javax.swing.JSpinner spnAlcanceFinal;
    private javax.swing.JSpinner spnAlcanceInicial;
    private javax.swing.JSpinner spnFechaFin;
    private javax.swing.JSpinner spnFechaInicio;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextArea txtObservaciones;
    private javax.swing.JTextField txtPuesto;
    private javax.swing.JTextField txtUnidad;
    // End of variables declaration//GEN-END:variables
}
