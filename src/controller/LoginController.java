package controller;

import java.sql.SQLException;
import dao.UsuarioDAO;
import model.Usuario;

public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String cui, char[] contrasena) throws LoginException {
        if (cui == null || cui.trim().isEmpty()) {
            throw new LoginException("Debe ingresar el CUI.");
        }
        if (contrasena == null || contrasena.length == 0) {
            throw new LoginException("Debe ingresar la contraseña.");
        }

        Usuario usuario;
        try {
            usuario = usuarioDAO.buscarPorCui(cui.trim());
        } catch (SQLException e) {
            throw new LoginException(
                    "No se pudo conectar con la base de datos. Intente nuevamente.", e);
        }

        String contrasenaIngresada = new String(contrasena);
        if (usuario == null || !usuario.getContrasena().equals(contrasenaIngresada)) {
            throw new LoginException("CUI o contraseña inválidos.");
        }

        util.Sesion.getInstancia().setUsuarioActual(usuario);

        return usuario;
    }
}
