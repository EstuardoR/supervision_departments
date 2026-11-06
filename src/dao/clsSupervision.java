
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.clsConnection;

public class clsSupervision {

    public boolean cambiarEstado(
            int idSupervision,
            String estadoAnterior,
            String estadoNuevo,
            String cuiSupervisor) throws SQLException {

        Connection con = clsConnection.ConnectionDB();

        if (con == null) {
            throw new SQLException("No se pudo conectar con SQL Server.");
        }

        try (con) {
            con.setAutoCommit(false);

            try {
                String sqlActualizar =
                    "UPDATE dbo.Supervisiones "
                    + "SET estado = ? "
                    + "WHERE id_supervision = ? "
                    + "AND cui_supervisor = ? "
                    + "AND estado = ?";

                int filas;

                try (PreparedStatement ps =
                        con.prepareStatement(sqlActualizar)) {

                    ps.setString(1, estadoNuevo);
                    ps.setInt(2, idSupervision);
                    ps.setString(3, cuiSupervisor);
                    ps.setString(4, estadoAnterior);

                    filas = ps.executeUpdate();
                }

                if (filas != 1) {
                    con.rollback();
                    return false;
                }

                String sqlHistorial =
                    "INSERT INTO dbo.Historial_Supervision "
                    + "(id_supervision, estado_anterior, "
                    + "estado_nuevo, motivo_cambio, responsable_cui) "
                    + "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlHistorial)) {

                    ps.setInt(1, idSupervision);
                    ps.setString(2, estadoAnterior);
                    ps.setString(3, estadoNuevo);
                    ps.setString(4, "Inicio de supervisión");
                    ps.setString(5, cuiSupervisor);

                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}

