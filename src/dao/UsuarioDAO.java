package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Usuario;

public class UsuarioDAO {

    public Usuario buscarPorCui(String cui) throws SQLException {
        Connection conn = util.clsConnection.getConexion();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            String sql = "SELECT cui, contrasena, nombre_completo, perfil, "
                    + "direccion_rrhh, unidad_rrhh, puesto_rrhh, "
                    + "codigo_empleado, correo_institucional "
                    + "FROM dbo.Usuarios WHERE cui = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cui);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Usuario usuario = new Usuario();
                    usuario.setCui(rs.getString("cui"));
                    usuario.setContrasena(rs.getString("contrasena"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setDireccionRrhh(rs.getString("direccion_rrhh"));
                    usuario.setUnidadRrhh(rs.getString("unidad_rrhh"));
                    usuario.setPuestoRrhh(rs.getString("puesto_rrhh"));
                    usuario.setCodigoEmpleado(rs.getString("codigo_empleado"));
                    usuario.setCorreoInstitucional(rs.getString("correo_institucional"));
                    return usuario;
                }
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
