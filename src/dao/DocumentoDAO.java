/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentoDAO {

    /**
     * Busca la ruta del documento más reciente de un tipo dado para una
     * supervisión. Devuelve null si no hay ninguno registrado.
     */
    public String buscarRuta(int idSupervision, String tipoDocumento) throws SQLException {
        String sql = "SELECT TOP 1 ruta_archivo FROM dbo.Documentos_Supervision "
                + "WHERE id_supervision = ? AND tipo_documento = ? "
                + "ORDER BY fecha_carga DESC";
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            ps.setString(2, tipoDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("ruta_archivo") : null;
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
