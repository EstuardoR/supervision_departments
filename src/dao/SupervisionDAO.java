/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */


package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import model.Supervision;
import model.Usuario;
import util.Sesion;

public class SupervisionDAO {

    private static final List<String> ESTADOS_VALIDOS = Arrays.asList(
            "PROGRAMADO", "REPROGRAMADO", "APROBADO", "INICIADO", "EJECUTADO",
            "RESULTADOS", "ACCIONES", "INFORME", "SEGUIMIENTO", "CANCELADO");

    private SupervisionDAO() {
    }

    /**
     * Inserta una nueva supervisión con estado PROGRAMADO y registra la
     * primera fila de su historial, todo dentro de una sola transacción.
     */
    public static void crear(Supervision s) throws SupervisionException {
        Usuario usuarioActual = Sesion.getInstancia().getUsuarioActual();
        if (usuarioActual == null) {
            throw new SupervisionException("No hay una sesión activa para registrar la asignación.");
        }
        String responsableCui = usuarioActual.getCui();

        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try {
            conn.setAutoCommit(false);

            int idSupervision;
            String insertSupervisionSql = "INSERT INTO dbo.Supervisiones ("
                    + "cui_supervisor, region, orden_asignacion, fecha_inicio_comision, fecha_fin_comision, "
                    + "fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, direccion_administrativa, "
                    + "departamento_administrativo, departamento, municipio, oficina_auxiliatura, "
                    + "autorizacion_viaticos, vehiculo_propio, observaciones, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PROGRAMADO')";
            try (PreparedStatement ps = conn.prepareStatement(insertSupervisionSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, s.getCuiSupervisor());
                ps.setString(2, s.getRegion());
                ps.setString(3, s.getOrdenAsignacion());
                ps.setDate(4, new java.sql.Date(s.getFechaInicioComision().getTime()));
                ps.setDate(5, new java.sql.Date(s.getFechaFinComision().getTime()));
                ps.setDate(6, new java.sql.Date(s.getFechaAlcanceInicial().getTime()));
                ps.setDate(7, new java.sql.Date(s.getFechaAlcanceFinal().getTime()));
                ps.setString(8, s.getTipoNombramiento());
                ps.setString(9, s.getDireccionAdministrativa());
                ps.setString(10, s.getDepartamentoAdministrativo());
                ps.setString(11, s.getDepartamento());
                ps.setString(12, s.getMunicipio());
                ps.setString(13, s.getOficinaAuxiliatura());
                ps.setBoolean(14, s.isAutorizacionViaticos());
                ps.setBoolean(15, s.isVehiculoPropio());
                ps.setString(16, s.getObservaciones());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SupervisionException("No se pudo obtener el id de la supervisión creada.");
                    }
                    idSupervision = rs.getInt(1);
                }
            }

            String insertHistorialSql = "INSERT INTO dbo.Historial_Supervision "
                    + "(id_supervision, estado_anterior, estado_nuevo, responsable_cui) "
                    + "VALUES (?, NULL, 'PROGRAMADO', ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertHistorialSql)) {
                ps.setInt(1, idSupervision);
                ps.setString(2, responsableCui);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new SupervisionException("Ocurrió un error al guardar la asignación de supervisión.", e);
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
