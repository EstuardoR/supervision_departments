/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package model;

import java.util.Date;

/**
 * Agrupa los filtros opcionales de la pantalla de reportes. Un campo en
 * null significa "sin filtro" para ese criterio.
 */
public class FiltroReporte {

    private Integer anio;
    private String region;
    private String departamento;
    private Date fechaDesde;
    private Date fechaHasta;

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
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
}
