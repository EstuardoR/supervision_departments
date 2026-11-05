/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package util;

import java.awt.Component;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Utilidades reusables para exportar/imprimir el contenido de cualquier
 * JTable de solo lectura de la aplicación.
 */
public class ExportUtil {

    private ExportUtil() {
    }

    /**
     * Pide al usuario dónde guardar y exporta la tabla a un archivo CSV
     * (encabezados, separador coma, UTF-8 con BOM para que Excel muestre
     * bien los acentos). Muestra un mensaje de confirmación al terminar.
     */
    public static void exportarCsv(JTable tabla, Component padre) {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("reporte.csv"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }

        TableModel modelo = tabla.getModel();
        try (BufferedWriter escritor = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            escritor.write('﻿');
            for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
                if (columna > 0) {
                    escritor.write(",");
                }
                escritor.write(escaparCsv(modelo.getColumnName(columna)));
            }
            escritor.newLine();
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
                    if (columna > 0) {
                        escritor.write(",");
                    }
                    Object valor = modelo.getValueAt(fila, columna);
                    escritor.write(escaparCsv(valor == null ? "" : valor.toString()));
                }
                escritor.newLine();
            }
            JOptionPane.showMessageDialog(padre, "Archivo exportado correctamente.", "Exportar CSV",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(padre, "No se pudo exportar el archivo: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Pone entre comillas (duplicando las internas) un valor CSV si contiene coma, comillas o salto de línea. */
    private static String escaparCsv(String valor) {
        boolean necesitaComillas = valor.contains(",") || valor.contains("\"") || valor.contains("\n");
        String escapado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + escapado + "\"" : escapado;
    }

    /** Imprime la tabla con JTable.print(), usando 'titulo' como encabezado de la página. */
    public static void imprimir(JTable tabla, String titulo) {
        try {
            tabla.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(titulo), null);
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(tabla, "No se pudo imprimir: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
