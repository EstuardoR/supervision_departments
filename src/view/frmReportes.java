/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package view;

/**
 *
 * @author by_estuardo
 */
public class frmReportes extends javax.swing.JInternalFrame {

    private static final String TODOS = "Todos";
    private static final String TODAS = "Todas";

    /**
     * Creates new form frmReportes
     */
    public frmReportes() {
        initComponents();
        cargarFiltrosFijos();
        generar();
    }

    /** Llena los combos de filtro: años desde 2024 al actual, región 1-30, y departamentos del catálogo. */
    private void cargarFiltrosFijos() {
        int anioActual = java.time.Year.now().getValue();
        javax.swing.DefaultComboBoxModel<String> modeloAnio = new javax.swing.DefaultComboBoxModel<>();
        modeloAnio.addElement(TODOS);
        for (int anio = 2024; anio <= anioActual; anio++) {
            modeloAnio.addElement(String.valueOf(anio));
        }
        cmbAnio.setModel(modeloAnio);

        javax.swing.DefaultComboBoxModel<String> modeloRegion = new javax.swing.DefaultComboBoxModel<>();
        modeloRegion.addElement(TODAS);
        for (int i = 1; i <= 30; i++) {
            modeloRegion.addElement(String.valueOf(i));
        }
        cmbRegion.setModel(modeloRegion);

        javax.swing.DefaultComboBoxModel<String> modeloDepartamento = new javax.swing.DefaultComboBoxModel<>();
        modeloDepartamento.addElement(TODOS);
        for (String departamento : util.Catalogos.departamentos()) {
            modeloDepartamento.addElement(departamento);
        }
        cmbDepartamento.setModel(modeloDepartamento);
    }

    /** Restablece todos los filtros y el orden de cada pestaña a su valor por defecto, y vuelve a generar. */
    private void limpiarFiltros() {
        cmbAnio.setSelectedIndex(0);
        cmbRegion.setSelectedIndex(0);
        cmbDepartamento.setSelectedIndex(0);
        txtDesde.setText("");
        txtHasta.setText("");
        rdoVisitasMas.setSelected(true);
        rdoFortalezasMas.setSelected(true);
        rdoDeficienciasMas.setSelected(true);
        rdoOportunidadesMas.setSelected(true);
        generar();
    }

    /** Arma el filtro a partir de lo que el usuario haya llenado, validando fechas. */
    private model.FiltroReporte construirFiltroDesdeFormulario() {
        model.FiltroReporte filtro = new model.FiltroReporte();

        String anioTexto = (String) cmbAnio.getSelectedItem();
        filtro.setAnio((anioTexto == null || TODOS.equals(anioTexto)) ? null : Integer.valueOf(anioTexto));
        filtro.setRegion(valorSinTodos(cmbRegion, TODAS));
        filtro.setDepartamento(valorSinTodos(cmbDepartamento, TODOS));

        java.util.Date desde = parsearFecha(txtDesde.getText());
        java.util.Date hasta = parsearFecha(txtHasta.getText());
        if (desde != null && hasta != null && desde.after(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser posterior a 'Hasta'.");
        }
        filtro.setFechaDesde(desde);
        filtro.setFechaHasta(hasta);
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

    /** Ejecuta la consulta de la pestaña activa con los filtros actuales y la muestra en su tabla. */
    private void generar() {
        try {
            model.FiltroReporte filtro = construirFiltroDesdeFormulario();
            java.util.List<model.FilaReporte> filas;
            javax.swing.JTable tablaActiva;

            switch (tabReportes.getSelectedIndex()) {
                case 0:
                    filas = new controller.ReportesController().visitas(filtro, rdoVisitasMas.isSelected());
                    tablaActiva = tblVisitas;
                    break;
                case 1:
                    filas = new controller.ReportesController()
                            .hallazgosPorSigla(filtro, "F", rdoFortalezasMas.isSelected());
                    tablaActiva = tblFortalezas;
                    break;
                case 2:
                    filas = new controller.ReportesController()
                            .hallazgosPorSigla(filtro, "D", rdoDeficienciasMas.isSelected());
                    tablaActiva = tblDeficiencias;
                    break;
                case 3:
                    filas = new controller.ReportesController()
                            .hallazgosPorSigla(filtro, "OM", rdoOportunidadesMas.isSelected());
                    tablaActiva = tblOportunidades;
                    break;
                case 4:
                    filas = new controller.ReportesController().pendientesPorAtender(filtro);
                    tablaActiva = tblPendientes;
                    break;
                default:
                    filas = new controller.ReportesController().sinPendientes(filtro);
                    tablaActiva = tblSinPendientes;
                    break;
            }

            llenarTabla(tablaActiva, filas);
            lblFilas.setText(filas.size() + " filas");
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Fecha inválida",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (controller.ReportesException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Vuelca la lista de filas (oficina, cantidad) en el DefaultTableModel de la tabla indicada. */
    private void llenarTabla(javax.swing.JTable tabla, java.util.List<model.FilaReporte> filas) {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        for (model.FilaReporte fila : filas) {
            modelo.addRow(new Object[]{fila.getOficina(), fila.getCantidad()});
        }
    }

    /** Devuelve la tabla de la pestaña actualmente activa. */
    private javax.swing.JTable tablaActivaActual() {
        switch (tabReportes.getSelectedIndex()) {
            case 0:
                return tblVisitas;
            case 1:
                return tblFortalezas;
            case 2:
                return tblDeficiencias;
            case 3:
                return tblOportunidades;
            case 4:
                return tblPendientes;
            default:
                return tblSinPendientes;
        }
    }

    /** Arma un panel de pestaña con las opciones Más/Menos arriba y la tabla debajo. */
    private javax.swing.JPanel crearPanelConOrden(javax.swing.JRadioButton rdoMas, javax.swing.JRadioButton rdoMenos,
            javax.swing.JScrollPane scroll) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rdoMas)
                        .addGap(18, 18, 18)
                        .addComponent(rdoMenos))
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoMas)
                    .addComponent(rdoMenos))
                .addGap(10, 10, 10)
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addContainerGap())
        );
        return panel;
    }

    /** Arma un panel de pestaña que solo tiene la tabla (sin opciones de orden). */
    private javax.swing.JPanel crearPanelSimple(javax.swing.JScrollPane scroll) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addContainerGap())
        );
        return panel;
    }

    /** Deja una tabla lista para mostrar pares oficina/cantidad: modelo no editable con esas 2 columnas. */
    private void configurarTablaReporte(javax.swing.JTable tabla) {
        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"Oficina", "Cantidad"}
        ) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblAnio = new javax.swing.JLabel();
        cmbAnio = new javax.swing.JComboBox<>();
        lblRegion = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        lblDepartamento = new javax.swing.JLabel();
        cmbDepartamento = new javax.swing.JComboBox<>();
        lblDesde = new javax.swing.JLabel();
        txtDesde = new javax.swing.JTextField();
        lblHasta = new javax.swing.JLabel();
        txtHasta = new javax.swing.JTextField();
        btnLimpiar = new javax.swing.JButton();
        btnGenerar = new javax.swing.JButton();
        tabReportes = new javax.swing.JTabbedPane();

        grpOrdenVisitas = new javax.swing.ButtonGroup();
        rdoVisitasMas = new javax.swing.JRadioButton();
        rdoVisitasMenos = new javax.swing.JRadioButton();
        scrollVisitas = new javax.swing.JScrollPane();
        tblVisitas = new javax.swing.JTable();

        grpOrdenFortalezas = new javax.swing.ButtonGroup();
        rdoFortalezasMas = new javax.swing.JRadioButton();
        rdoFortalezasMenos = new javax.swing.JRadioButton();
        scrollFortalezas = new javax.swing.JScrollPane();
        tblFortalezas = new javax.swing.JTable();

        grpOrdenDeficiencias = new javax.swing.ButtonGroup();
        rdoDeficienciasMas = new javax.swing.JRadioButton();
        rdoDeficienciasMenos = new javax.swing.JRadioButton();
        scrollDeficiencias = new javax.swing.JScrollPane();
        tblDeficiencias = new javax.swing.JTable();

        grpOrdenOportunidades = new javax.swing.ButtonGroup();
        rdoOportunidadesMas = new javax.swing.JRadioButton();
        rdoOportunidadesMenos = new javax.swing.JRadioButton();
        scrollOportunidades = new javax.swing.JScrollPane();
        tblOportunidades = new javax.swing.JTable();

        scrollPendientes = new javax.swing.JScrollPane();
        tblPendientes = new javax.swing.JTable();

        scrollSinPendientes = new javax.swing.JScrollPane();
        tblSinPendientes = new javax.swing.JTable();

        lblFilas = new javax.swing.JLabel();
        btnExportarCsv = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Reportes – Indicadores");

        lblAnio.setText("Año");

        lblRegion.setText("Región");

        lblDepartamento.setText("Departamento");

        lblDesde.setText("Desde");

        lblHasta.setText("Hasta");

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnGenerar.setText("Generar");
        btnGenerar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarActionPerformed(evt);
            }
        });

        // Pestaña 1: Visitas
        grpOrdenVisitas.add(rdoVisitasMas);
        rdoVisitasMas.setSelected(true);
        rdoVisitasMas.setText("Más");
        rdoVisitasMas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        grpOrdenVisitas.add(rdoVisitasMenos);
        rdoVisitasMenos.setText("Menos");
        rdoVisitasMenos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        configurarTablaReporte(tblVisitas);
        scrollVisitas.setViewportView(tblVisitas);
        tabReportes.addTab("Visitas", crearPanelConOrden(rdoVisitasMas, rdoVisitasMenos, scrollVisitas));

        // Pestaña 2: Fortalezas
        grpOrdenFortalezas.add(rdoFortalezasMas);
        rdoFortalezasMas.setSelected(true);
        rdoFortalezasMas.setText("Más");
        rdoFortalezasMas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        grpOrdenFortalezas.add(rdoFortalezasMenos);
        rdoFortalezasMenos.setText("Menos");
        rdoFortalezasMenos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        configurarTablaReporte(tblFortalezas);
        scrollFortalezas.setViewportView(tblFortalezas);
        tabReportes.addTab("Fortalezas", crearPanelConOrden(rdoFortalezasMas, rdoFortalezasMenos, scrollFortalezas));

        // Pestaña 3: Deficiencias
        grpOrdenDeficiencias.add(rdoDeficienciasMas);
        rdoDeficienciasMas.setSelected(true);
        rdoDeficienciasMas.setText("Más");
        rdoDeficienciasMas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        grpOrdenDeficiencias.add(rdoDeficienciasMenos);
        rdoDeficienciasMenos.setText("Menos");
        rdoDeficienciasMenos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        configurarTablaReporte(tblDeficiencias);
        scrollDeficiencias.setViewportView(tblDeficiencias);
        tabReportes.addTab("Deficiencias", crearPanelConOrden(rdoDeficienciasMas, rdoDeficienciasMenos, scrollDeficiencias));

        // Pestaña 4: Oportunidades de mejora
        grpOrdenOportunidades.add(rdoOportunidadesMas);
        rdoOportunidadesMas.setSelected(true);
        rdoOportunidadesMas.setText("Más");
        rdoOportunidadesMas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        grpOrdenOportunidades.add(rdoOportunidadesMenos);
        rdoOportunidadesMenos.setText("Menos");
        rdoOportunidadesMenos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ordenCambiadoActionPerformed(evt);
            }
        });
        configurarTablaReporte(tblOportunidades);
        scrollOportunidades.setViewportView(tblOportunidades);
        tabReportes.addTab("Oportunidades de mejora", crearPanelConOrden(rdoOportunidadesMas, rdoOportunidadesMenos, scrollOportunidades));

        // Pestaña 5: Pendientes por atender
        configurarTablaReporte(tblPendientes);
        scrollPendientes.setViewportView(tblPendientes);
        tabReportes.addTab("Pendientes por atender", crearPanelSimple(scrollPendientes));

        // Pestaña 6: Sin pendientes
        configurarTablaReporte(tblSinPendientes);
        scrollSinPendientes.setViewportView(tblSinPendientes);
        tabReportes.addTab("Sin pendientes", crearPanelSimple(scrollSinPendientes));

        tabReportes.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabReportesStateChanged(evt);
            }
        });

        lblFilas.setText("0 filas");

        btnExportarCsv.setText("Exportar CSV");
        btnExportarCsv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarCsvActionPerformed(evt);
            }
        });

        btnImprimir.setText("Imprimir");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
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
                            .addComponent(lblAnio)
                            .addComponent(cmbAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRegion)
                            .addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDepartamento)
                            .addComponent(cmbDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDesde)
                            .addComponent(txtDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblHasta)
                            .addComponent(txtHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnLimpiar)
                        .addGap(10, 10, 10)
                        .addComponent(btnGenerar))
                    .addComponent(tabReportes, javax.swing.GroupLayout.DEFAULT_SIZE, 1150, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFilas)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnExportarCsv)
                        .addGap(10, 10, 10)
                        .addComponent(btnImprimir)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addComponent(lblAnio).addGap(4, 4, 4).addComponent(cmbAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblRegion).addGap(4, 4, 4).addComponent(cmbRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblDepartamento).addGap(4, 4, 4).addComponent(cmbDepartamento, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblDesde).addGap(4, 4, 4).addComponent(txtDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addComponent(lblHasta).addGap(4, 4, 4).addComponent(txtHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(btnLimpiar))
                    .addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(btnGenerar)))
                .addGap(20, 20, 20)
                .addComponent(tabReportes, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFilas)
                    .addComponent(btnExportarCsv)
                    .addComponent(btnImprimir))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarActionPerformed
        generar();
    }//GEN-LAST:event_btnGenerarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void tabReportesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabReportesStateChanged
        generar();
    }//GEN-LAST:event_tabReportesStateChanged

    private void ordenCambiadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ordenCambiadoActionPerformed
        generar();
    }//GEN-LAST:event_ordenCambiadoActionPerformed

    private void btnExportarCsvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarCsvActionPerformed
        util.ExportUtil.exportarCsv(tablaActivaActual(), this);
    }//GEN-LAST:event_btnExportarCsvActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        util.ExportUtil.imprimir(tablaActivaActual(), tabReportes.getTitleAt(tabReportes.getSelectedIndex()));
    }//GEN-LAST:event_btnImprimirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportarCsv;
    private javax.swing.JButton btnGenerar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<String> cmbAnio;
    private javax.swing.JComboBox<String> cmbDepartamento;
    private javax.swing.JComboBox<String> cmbRegion;
    private javax.swing.ButtonGroup grpOrdenDeficiencias;
    private javax.swing.ButtonGroup grpOrdenFortalezas;
    private javax.swing.ButtonGroup grpOrdenOportunidades;
    private javax.swing.ButtonGroup grpOrdenVisitas;
    private javax.swing.JLabel lblAnio;
    private javax.swing.JLabel lblDepartamento;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblFilas;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JRadioButton rdoDeficienciasMas;
    private javax.swing.JRadioButton rdoDeficienciasMenos;
    private javax.swing.JRadioButton rdoFortalezasMas;
    private javax.swing.JRadioButton rdoFortalezasMenos;
    private javax.swing.JRadioButton rdoOportunidadesMas;
    private javax.swing.JRadioButton rdoOportunidadesMenos;
    private javax.swing.JRadioButton rdoVisitasMas;
    private javax.swing.JRadioButton rdoVisitasMenos;
    private javax.swing.JScrollPane scrollDeficiencias;
    private javax.swing.JScrollPane scrollFortalezas;
    private javax.swing.JScrollPane scrollOportunidades;
    private javax.swing.JScrollPane scrollPendientes;
    private javax.swing.JScrollPane scrollSinPendientes;
    private javax.swing.JScrollPane scrollVisitas;
    private javax.swing.JTabbedPane tabReportes;
    private javax.swing.JTable tblDeficiencias;
    private javax.swing.JTable tblFortalezas;
    private javax.swing.JTable tblOportunidades;
    private javax.swing.JTable tblPendientes;
    private javax.swing.JTable tblSinPendientes;
    private javax.swing.JTable tblVisitas;
    private javax.swing.JTextField txtDesde;
    private javax.swing.JTextField txtHasta;
    // End of variables declaration//GEN-END:variables
}
