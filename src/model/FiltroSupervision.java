/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package model;

import java.util.Date;

/**
 * Agrupa los filtros opcionales de búsqueda de la bandeja de planificación.
 * Un campo en null significa "sin filtro" para ese criterio.
 */
public class FiltroSupervision {

    private Date fechaDesde;
    private Date fechaHasta;
    private String region;
    private String departamento;
    private String municipio;
    private String oficinaAuxiliatura;
    private String tipoNombramiento;
    private String cuiSupervisor;
    private String estado;

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getOficinaAuxiliatura() {
        return oficinaAuxiliatura;
    }

    public void setOficinaAuxiliatura(String oficinaAuxiliatura) {
        this.oficinaAuxiliatura = oficinaAuxiliatura;
    }

    public String getTipoNombramiento() {
        return tipoNombramiento;
    }

    public void setTipoNombramiento(String tipoNombramiento) {
        this.tipoNombramiento = tipoNombramiento;
    }

    public String getCuiSupervisor() {
        return cuiSupervisor;
    }

    public void setCuiSupervisor(String cuiSupervisor) {
        this.cuiSupervisor = cuiSupervisor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
