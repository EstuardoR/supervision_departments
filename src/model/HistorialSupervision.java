/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package model;

import java.util.Date;

/**
 * Una fila del historial de una supervisión, ya con el nombre del
 * responsable resuelto (para mostrarla directamente en una tabla).
 */
public class HistorialSupervision {

    private String estado;
    private Date fechaHora;
    private String observaciones;
    private String responsableNombre;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getResponsableNombre() {
        return responsableNombre;
    }

    public void setResponsableNombre(String responsableNombre) {
        this.responsableNombre = responsableNombre;
    }
}
