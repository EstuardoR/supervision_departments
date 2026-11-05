/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package model;

import java.util.Date;

/**
 * Fila resumida de una supervisión, tal como se muestra en la tabla de la
 * bandeja de planificación (incluye el nombre del supervisor ya resuelto).
 */
public class SupervisionResumen {

    private int idSupervision;
    private String region;
    private String departamento;
    private String municipio;
    private String oficinaAuxiliatura;
    private String direccionAdministrativa;
    private String departamentoAdministrativo;
    private Date fechaInicioComision;
    private Date fechaFinComision;
    private String nombreSupervisor;
    private String estado;

    public int getIdSupervision() {
        return idSupervision;
    }

    public void setIdSupervision(int idSupervision) {
        this.idSupervision = idSupervision;
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

    public String getNombreSupervisor() {
        return nombreSupervisor;
    }

    public void setNombreSupervisor(String nombreSupervisor) {
        this.nombreSupervisor = nombreSupervisor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
