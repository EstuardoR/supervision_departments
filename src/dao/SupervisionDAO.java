package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import model.Usuario;
import util.Sesion;

public class SupervisionDAO {

    private static final List<String> ESTADOS_VALIDOS = Arrays.asList(
            "PROGRAMADO", "REPROGRAMADO", "APROBADO", "INICIADO", "EJECUTADO",
            "RESULTADOS", "ACCIONES", "INFORME", "SEGUIMIENTO", "CANCELADO");

    private SupervisionDAO() {
    }

    public static void cambiarEstado(int idSupervision, String nuevoEstado, String motivoCambio)
            throws SupervisionException {
        if (nuevoEstado == null || !ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new SupervisionException("El estado '" + nuevoEstado + "' no es válido.");
        }

        Usuario usuarioActual = Sesion.getInstancia().getUsuarioActual();
        if (usuarioActual == null) {
            throw new SupervisionException("No hay una sesión activa para registrar el cambio.");
        }
        String responsableCui = usuarioActual.getCui();

        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            conn.setAutoCommit(false);

            String estadoAnterior;
            String selectSql = "SELECT estado FROM dbo.Supervisiones WHERE id_supervision = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, idSupervision);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SupervisionException(
                                "No existe la supervisión con id " + idSupervision + ".");
                    }
                    estadoAnterior = rs.getString("estado");
                }
            }

            String updateSql = "UPDATE dbo.Supervisiones SET estado = ? WHERE id_supervision = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idSupervision);
                ps.executeUpdate();
            }

            String insertSql = "INSERT INTO dbo.Historial_Supervision "
                    + "(id_supervision, estado_anterior, estado_nuevo, motivo_cambio, responsable_cui) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, idSupervision);
                ps.setString(2, estadoAnterior);
                ps.setString(3, nuevoEstado);
                ps.setString(4, motivoCambio);
                ps.setString(5, responsableCui);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new SupervisionException(
                    "Ocurrió un error al cambiar el estado de la supervisión.", e);
        } catch (SupervisionException e) {
            rollback(conn);
            throw e;
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }
}
