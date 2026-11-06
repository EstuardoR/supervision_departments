/*
 * Autor: María José Pelicó López
 * Carné: 9490-25-11528
 */
package model;

/**
 * Un hallazgo (D u OM) de una supervisión junto con la acción que se
 * registró para él (si ya existe). Une Detalle_Resultados con
 * Acciones_Preventivas_Correctivas.
 */
public class HallazgoAccion {

    private int idDetalle;
    private String sigla;
    private String descripcion;
    private Integer idAccion;
    private String accionInmediata;
    private String rutaRespaldo;
    private Boolean porEfectuar;
    private String direccionResponsable;
    private String departamentoResponsable;
    private String estadoAccion;
    private boolean tieneCausaRaiz;
    private CausaRaiz causaRaiz;

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int v) { this.idDetalle = v; }
    public String getSigla() { return sigla; }
    public void setSigla(String v) { this.sigla = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public Integer getIdAccion() { return idAccion; }
    public void setIdAccion(Integer v) { this.idAccion = v; }
    public String getAccionInmediata() { return accionInmediata; }
    public void setAccionInmediata(String v) { this.accionInmediata = v; }
    public String getRutaRespaldo() { return rutaRespaldo; }
    public void setRutaRespaldo(String v) { this.rutaRespaldo = v; }
    public Boolean getPorEfectuar() { return porEfectuar; }
    public void setPorEfectuar(Boolean v) { this.porEfectuar = v; }
    public String getDireccionResponsable() { return direccionResponsable; }
    public void setDireccionResponsable(String v) { this.direccionResponsable = v; }
    public String getDepartamentoResponsable() { return departamentoResponsable; }
    public void setDepartamentoResponsable(String v) { this.departamentoResponsable = v; }
    public String getEstadoAccion() { return estadoAccion; }
    public void setEstadoAccion(String v) { this.estadoAccion = v; }
    /** true si en la base de datos ya existe una causa raíz para esta acción. */
    public boolean isTieneCausaRaiz() { return tieneCausaRaiz; }
    public void setTieneCausaRaiz(boolean v) { this.tieneCausaRaiz = v; }
    /** Causa raíz capturada en esta sesión y todavía sin guardar. */
    public CausaRaiz getCausaRaiz() { return causaRaiz; }
    public void setCausaRaiz(CausaRaiz v) { this.causaRaiz = v; }
}