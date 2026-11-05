/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package model;

/**
 * Una fila de cualquiera de los reportes por oficina: el nombre de la
 * oficina/auxiliatura y la cantidad calculada para ese indicador.
 */
public class FilaReporte {

    private String oficina;
    private int cantidad;

    public String getOficina() {
        return oficina;
    }

    public void setOficina(String oficina) {
        this.oficina = oficina;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
