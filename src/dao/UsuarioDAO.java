
/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */



package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Usuario;

public class UsuarioDAO {

    /** Lista los usuarios (cui y nombre completo) que tienen el perfil indicado. */
    public List<Usuario> listarPorPerfil(String perfil) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            String sql = "SELECT cui, nombre_completo, correo_institucional FROM dbo.Usuarios "
                    + "WHERE perfil = ? ORDER BY nombre_completo";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, perfil);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Usuario usuario = new Usuario();
                        usuario.setCui(rs.getString("cui"));
                        usuario.setNombreCompleto(rs.getString("nombre_completo"));
                        usuario.setCorreoInstitucional(rs.getString("correo_institucional"));
                        usuarios.add(usuario);
                    }
                }
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
        return usuarios;
    }

    public Usuario buscarPorCui(String cui) throws SQLException {
        Connection conn = util.clsConnection.ConnectionDB();
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

    /** Lista todos los usuarios (para la pantalla de administración), ordenados por nombre. */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            String sql = "SELECT cui, nombre_completo, perfil, correo_institucional FROM dbo.Usuarios "
                    + "ORDER BY nombre_completo";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setCui(rs.getString("cui"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    usuario.setPerfil(rs.getString("perfil"));
                    usuario.setCorreoInstitucional(rs.getString("correo_institucional"));
                    usuarios.add(usuario);
                }
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
        return usuarios;
    }

    /** Crea un usuario nuevo. Lanza SQLException si el CUI ya existe (choca con la llave primaria). */
    public void crear(Usuario u) throws SQLException {
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            String sql = "INSERT INTO dbo.Usuarios (cui, contrasena, nombre_completo, perfil, "
                    + "direccion_rrhh, unidad_rrhh, puesto_rrhh, codigo_empleado, correo_institucional) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u.getCui());
                ps.setString(2, u.getContrasena());
                ps.setString(3, u.getNombreCompleto());
                ps.setString(4, u.getPerfil());
                ps.setString(5, u.getDireccionRrhh());
                ps.setString(6, u.getUnidadRrhh());
                ps.setString(7, u.getPuestoRrhh());
                ps.setString(8, u.getCodigoEmpleado());
                ps.setString(9, u.getCorreoInstitucional());
                ps.executeUpdate();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Actualiza los datos de un usuario existente (no cambia el CUI, es la llave primaria).
     * Si cambiarContrasena es false, la contraseña guardada se deja igual.
     */
    public void actualizar(Usuario u, boolean cambiarContrasena) throws SQLException {
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            String sql = (cambiarContrasena
                    ? "UPDATE dbo.Usuarios SET contrasena = ?, nombre_completo = ?, perfil = ?, "
                    : "UPDATE dbo.Usuarios SET nombre_completo = ?, perfil = ?, ")
                    + "direccion_rrhh = ?, unidad_rrhh = ?, puesto_rrhh = ?, codigo_empleado = ?, "
                    + "correo_institucional = ? WHERE cui = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                if (cambiarContrasena) {
                    ps.setString(i++, u.getContrasena());
                }
                ps.setString(i++, u.getNombreCompleto());
                ps.setString(i++, u.getPerfil());
                ps.setString(i++, u.getDireccionRrhh());
                ps.setString(i++, u.getUnidadRrhh());
                ps.setString(i++, u.getPuestoRrhh());
                ps.setString(i++, u.getCodigoEmpleado());
                ps.setString(i++, u.getCorreoInstitucional());
                ps.setString(i, u.getCui());
                ps.executeUpdate();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Elimina un usuario por su CUI. Lanza SQLException si el usuario tiene supervisiones o
     * historial asociado (la base de datos lo protege con llaves foráneas).
     */
    public void eliminar(String cui) throws SQLException {
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            String sql = "DELETE FROM dbo.Usuarios WHERE cui = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cui);
                ps.executeUpdate();
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
