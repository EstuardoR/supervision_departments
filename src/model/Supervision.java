/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package model;

import java.util.Date;

/**
 * Representa una fila de la tabla Supervisiones. Es un objeto simple (POJO)
 * que solo transporta los datos entre la vista, el controlador y el DAO.
 */
public class Supervision {

    /** Null cuando es una supervisión nueva; con valor cuando se está editando una existente. */
    private Integer idSupervision;
    private String cuiSupervisor;
    private String region;
    private String ordenAsignacion;
    private Date fechaInicioComision;
    private Date fechaFinComision;
    private Date fechaAlcanceInicial;
    private Date fechaAlcanceFinal;
    private String tipoNombramiento;
    private String direccionAdministrativa;
    private String departamentoAdministrativo;
    private String departamento;
    private String municipio;
    private String oficinaAuxiliatura;
    private boolean autorizacionViaticos;
    private boolean vehiculoPropio;
    private String observaciones;
    private String estado;

    public Integer getIdSupervision() {
        return idSupervision;
    }

    public void setIdSupervision(Integer idSupervision) {
        this.idSupervision = idSupervision;
    }

    public String getCuiSupervisor() {
        return cuiSupervisor;
    }

    public void setCuiSupervisor(String cuiSupervisor) {
        this.cuiSupervisor = cuiSupervisor;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getOrdenAsignacion() {
        return ordenAsignacion;
    }

    public void setOrdenAsignacion(String ordenAsignacion) {
        this.ordenAsignacion = ordenAsignacion;
    }

    public Date getFechaInicioComision() {
        return fechaInicioComision;
    }

    public void setFechaInicioComision(Date fechaInicioComision) {
        this.fechaInicioComision = fechaInicioComision;
    }

    public Date getFechaFinComision() {
        return fechaFinComision;
    }

    public void setFechaFinComision(Date fechaFinComision) {
        this.fechaFinComision = fechaFinComision;
    }

    public Date getFechaAlcanceInicial() {
        return fechaAlcanceInicial;
    }

    public void setFechaAlcanceInicial(Date fechaAlcanceInicial) {
        this.fechaAlcanceInicial = fechaAlcanceInicial;
    }

    public Date getFechaAlcanceFinal() {
        return fechaAlcanceFinal;
    }

    public void setFechaAlcanceFinal(Date fechaAlcanceFinal) {
        this.fechaAlcanceFinal = fechaAlcanceFinal;
    }

    public String getTipoNombramiento() {
        return tipoNombramiento;
    }

    public void setTipoNombramiento(String tipoNombramiento) {
        this.tipoNombramiento = tipoNombramiento;
    }

    public String getDireccionAdministrativa() {
        return direccionAdministrativa;
    }

    public void setDireccionAdministrativa(String direccionAdministrativa) {
        this.direccionAdministrativa = direccionAdministrativa;
    }

    public String getDepartamentoAdministrativo() {
        return departamentoAdministrativo;
    }

    public void setDepartamentoAdministrativo(String departamentoAdministrativo) {
        this.departamentoAdministrativo = departamentoAdministrativo;
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

    public boolean isAutorizacionViaticos() {
        return autorizacionViaticos;
    }

    public void setAutorizacionViaticos(boolean autorizacionViaticos) {
        this.autorizacionViaticos = autorizacionViaticos;
    }

    public boolean isVehiculoPropio() {
        return vehiculoPropio;
    }

    public void setVehiculoPropio(boolean vehiculoPropio) {
        this.vehiculoPropio = vehiculoPropio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
