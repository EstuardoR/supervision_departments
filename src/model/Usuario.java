
/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */

package model;

public class Usuario {

    private String cui;
    private String contrasena;
    private String nombreCompleto;
    private String perfil;
    private String direccionRrhh;
    private String unidadRrhh;
    private String puestoRrhh;
    private String codigoEmpleado;
    private String correoInstitucional;

    public Usuario() {
    }

    public String getCui() {
        return cui;
    }

    public void setCui(String cui) {
        this.cui = cui;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getDireccionRrhh() {
        return direccionRrhh;
    }

    public void setDireccionRrhh(String direccionRrhh) {
        this.direccionRrhh = direccionRrhh;
    }

    public String getUnidadRrhh() {
        return unidadRrhh;
    }

    public void setUnidadRrhh(String unidadRrhh) {
        this.unidadRrhh = unidadRrhh;
    }

    public String getPuestoRrhh() {
        return puestoRrhh;
    }

    public void setPuestoRrhh(String puestoRrhh) {
        this.puestoRrhh = puestoRrhh;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    /** Se usa para que los JComboBox que listan usuarios muestren el nombre completo. */
    @Override
    public String toString() {
        return nombreCompleto;
    }
}
