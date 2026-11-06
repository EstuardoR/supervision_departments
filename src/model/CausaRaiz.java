/*
 * Autor: María José Pelicó López
 * Carné: 9490-25-11528
 */
package model;

import java.util.Date;

/** Datos del análisis "5 ¿Por qué?" capturados en DlgCausaRaiz. */
public class CausaRaiz {

    private String[] preguntas = new String[5];
    private String[] causas = new String[5];
    private String causaRaizPrincipal;
    private String contramedidas;
    private String responsable;
    private Date fechaCompromiso;

    public String[] getPreguntas() { return preguntas; }
    public void setPreguntas(String[] preguntas) { this.preguntas = preguntas; }
    public String[] getCausas() { return causas; }
    public void setCausas(String[] causas) { this.causas = causas; }
    public String getCausaRaizPrincipal() { return causaRaizPrincipal; }
    public void setCausaRaizPrincipal(String v) { this.causaRaizPrincipal = v; }
    public String getContramedidas() { return contramedidas; }
    public void setContramedidas(String v) { this.contramedidas = v; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String v) { this.responsable = v; }
    public Date getFechaCompromiso() { return fechaCompromiso; }
    public void setFechaCompromiso(Date v) { this.fechaCompromiso = v; }
}