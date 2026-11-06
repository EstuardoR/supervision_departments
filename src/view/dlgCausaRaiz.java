/*
 * Autor: María José Pelicó López
 * Carné: 9490-25-11528
 */
package view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.CausaRaiz;

/**
 * Método de los 5 ¿Por qué?: 5 filas fijas (pregunta + causa), y debajo la
 * causa raíz principal, contramedidas, responsable y fecha compromiso.
 * Cada causa escrita se convierte en la siguiente pregunta.
 *
 * Uso: CausaRaiz r = dlgCausaRaiz.mostrar(ventana, "problema", null);
 * Devuelve null si el usuario cancela.
 */
public class dlgCausaRaiz extends JDialog {

    private final JTextField[] txtPreguntas = new JTextField[5];
    private final JTextField[] txtCausas = new JTextField[5];
    private final JTextField txtPrincipal = new JTextField();
    private final JTextField txtContramedidas = new JTextField();
    private final JTextField txtResponsable = new JTextField();
    private final JTextField txtFecha = new JTextField();
    private final String problema;
    private CausaRaiz resultado;

    private dlgCausaRaiz(Window owner, String problema, CausaRaiz previo) {
        super(owner, "Análisis de causa raíz – Método 5 ¿Por qué?", Dialog.ModalityType.APPLICATION_MODAL);
        this.problema = recortar(problema);
        armarPantalla();
        if (previo != null) {
            precargar(previo);
        }
        actualizarPreguntas();
        setSize(860, 440);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /** Muestra el diálogo y espera. Devuelve los datos, o null si se canceló. */
    public static CausaRaiz mostrar(Window owner, String problema, CausaRaiz previo) {
        dlgCausaRaiz dialogo = new dlgCausaRaiz(owner, problema, previo);
        dialogo.setVisible(true);
        return dialogo.resultado;
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() > 80 ? texto.substring(0, 80) + "..." : texto;
    }

    private void armarPantalla() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        DocumentListener alCambiar = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarPreguntas(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarPreguntas(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarPreguntas(); }
        };

        for (int i = 0; i < 5; i++) {
            txtPreguntas[i] = new JTextField();
            txtPreguntas[i].setEditable(false);
            txtCausas[i] = new JTextField();
            txtCausas[i].getDocument().addDocumentListener(alCambiar);
            colocar(panel, g, new JLabel(String.valueOf(i + 1)), 0, i, 1, 0);
            colocar(panel, g, txtPreguntas[i], 1, i, 1, 1);
            colocar(panel, g, txtCausas[i], 2, i, 1, 1);
        }

        colocar(panel, g, new JLabel("Causa-raíz principal"), 1, 5, 1, 1);
        colocar(panel, g, new JLabel("Contramedidas"), 2, 5, 1, 1);
        colocar(panel, g, txtPrincipal, 1, 6, 1, 1);
        colocar(panel, g, txtContramedidas, 2, 6, 1, 1);
        colocar(panel, g, new JLabel("Responsable"), 1, 7, 1, 1);
        colocar(panel, g, new JLabel("Fecha compromiso (dd/mm/aaaa)"), 2, 7, 1, 1);
        colocar(panel, g, txtResponsable, 1, 8, 1, 1);
        colocar(panel, g, txtFecha, 2, 8, 1, 1);

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");
        btnCancelar.addActionListener(e -> {
            resultado = null;
            dispose();
        });
        btnGuardar.addActionListener(e -> guardar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        g.insets = new Insets(14, 4, 3, 4);
        colocar(panel, g, botones, 1, 9, 2, 1);

        setContentPane(panel);
    }

    private static void colocar(JPanel panel, GridBagConstraints g, Component c,
            int x, int y, int ancho, double pesoX) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = ancho;
        g.weightx = pesoX;
        panel.add(c, g);
    }

    /** La pregunta de cada fila se arma con la causa de la fila anterior. */
    private void actualizarPreguntas() {
        txtPreguntas[0].setText("¿Por qué " + problema + "?");
        for (int i = 1; i < 5; i++) {
            String causa = txtCausas[i - 1].getText().trim();
            txtPreguntas[i].setText("¿Por qué " + (causa.isEmpty() ? "[causa " + i + "]" : causa) + "?");
        }
    }

    private void precargar(CausaRaiz previo) {
        for (int i = 0; i < 5; i++) {
            txtCausas[i].setText(previo.getCausas()[i]);
        }
        txtPrincipal.setText(previo.getCausaRaizPrincipal());
        txtContramedidas.setText(previo.getContramedidas());
        txtResponsable.setText(previo.getResponsable());
        if (previo.getFechaCompromiso() != null) {
            txtFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(previo.getFechaCompromiso()));
        }
    }

    private void guardar() {
        String[] causas = new String[5];
        for (int i = 0; i < 5; i++) {
            causas[i] = txtCausas[i].getText().trim();
            if (causas[i].isEmpty()) {
                aviso("Complete la causa de la fila " + (i + 1) + ". No puede haber filas vacías.");
                txtCausas[i].requestFocus();
                return;
            }
        }
        if (vacio(txtPrincipal, "la causa-raíz principal")
                || vacio(txtContramedidas, "las contramedidas")
                || vacio(txtResponsable, "el responsable")
                || vacio(txtFecha, "la fecha compromiso")) {
            return;
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false);
        Date fecha;
        try {
            fecha = formato.parse(txtFecha.getText().trim());
        } catch (ParseException e) {
            aviso("La fecha compromiso debe tener el formato dd/mm/aaaa.");
            txtFecha.requestFocus();
            return;
        }

        String[] preguntas = new String[5];
        for (int i = 0; i < 5; i++) {
            preguntas[i] = txtPreguntas[i].getText();
        }
        CausaRaiz r = new CausaRaiz();
        r.setPreguntas(preguntas);
        r.setCausas(causas);
        r.setCausaRaizPrincipal(txtPrincipal.getText().trim());
        r.setContramedidas(txtContramedidas.getText().trim());
        r.setResponsable(txtResponsable.getText().trim());
        r.setFechaCompromiso(fecha);
        resultado = r;
        dispose();
    }

    private boolean vacio(JTextField campo, String nombre) {
        if (campo.getText().trim().isEmpty()) {
            aviso("Escriba " + nombre + ".");
            campo.requestFocus();
            return true;
        }
        return false;
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Causa raíz", JOptionPane.WARNING_MESSAGE);
    }
}