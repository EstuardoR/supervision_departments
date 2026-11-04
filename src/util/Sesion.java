package util;

import model.Usuario;

public class Sesion {

    private static Sesion instancia;

    private Usuario usuarioActual;

    private Sesion() {
    }

    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public boolean tienePerfil(String perfil) {
        return haySesionActiva() && usuarioActual.getPerfil().equals(perfil);
    }

    public boolean tieneAlgunPerfil(String... perfiles) {
        for (String perfil : perfiles) {
            if (tienePerfil(perfil)) {
                return true;
            }
        }
        return false;
    }
}
