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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.FiltroSupervision;
import model.HistorialSupervision;
import model.Supervision;
import model.SupervisionResumen;
import model.Usuario;
import util.Sesion;

public class SupervisionDAO {

    /** Lista de estados válidos del flujo de una supervisión, en orden. */
    public static final List<String> ESTADOS_VALIDOS = Arrays.asList(
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

    /**
     * Busca supervisiones aplicando solo los filtros que vengan con valor,
     * uniendo con Usuarios para traer el nombre del supervisor. Siempre usa
     * PreparedStatement con parámetros, nunca concatena los valores.
     */
    public static List<SupervisionResumen> buscar(FiltroSupervision filtro) throws SupervisionException {
        StringBuilder sql = new StringBuilder(
                "SELECT s.id_supervision, s.region, s.departamento, s.municipio, s.oficina_auxiliatura, "
                        + "s.direccion_administrativa, s.departamento_administrativo, "
                        + "s.fecha_inicio_comision, s.fecha_fin_comision, s.estado, u.nombre_completo "
                        + "FROM dbo.Supervisiones s "
                        + "JOIN dbo.Usuarios u ON u.cui = s.cui_supervisor "
                        + "WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (filtro.getFechaDesde() != null) {
            sql.append(" AND s.fecha_inicio_comision >= ?");
            parametros.add(new java.sql.Date(filtro.getFechaDesde().getTime()));
        }
        if (filtro.getFechaHasta() != null) {
            sql.append(" AND s.fecha_inicio_comision <= ?");
            parametros.add(new java.sql.Date(filtro.getFechaHasta().getTime()));
        }
        if (filtro.getRegion() != null) {
            sql.append(" AND s.region = ?");
            parametros.add(filtro.getRegion());
        }
        if (filtro.getDepartamento() != null) {
            sql.append(" AND s.departamento = ?");
            parametros.add(filtro.getDepartamento());
        }
        if (filtro.getMunicipio() != null) {
            sql.append(" AND s.municipio = ?");
            parametros.add(filtro.getMunicipio());
        }
        if (filtro.getOficinaAuxiliatura() != null) {
            sql.append(" AND s.oficina_auxiliatura = ?");
            parametros.add(filtro.getOficinaAuxiliatura());
        }
        if (filtro.getTipoNombramiento() != null) {
            sql.append(" AND s.tipo_nombramiento = ?");
            parametros.add(filtro.getTipoNombramiento());
        }
        if (filtro.getCuiSupervisor() != null) {
            sql.append(" AND s.cui_supervisor = ?");
            parametros.add(filtro.getCuiSupervisor());
        }
        if (filtro.getEstado() != null) {
            sql.append(" AND s.estado = ?");
            parametros.add(filtro.getEstado());
        }
        sql.append(" ORDER BY s.fecha_inicio_comision DESC");

        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            List<SupervisionResumen> resultados = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SupervisionResumen r = new SupervisionResumen();
                    r.setIdSupervision(rs.getInt("id_supervision"));
                    r.setRegion(rs.getString("region"));
                    r.setDepartamento(rs.getString("departamento"));
                    r.setMunicipio(rs.getString("municipio"));
                    r.setOficinaAuxiliatura(rs.getString("oficina_auxiliatura"));
                    r.setDireccionAdministrativa(rs.getString("direccion_administrativa"));
                    r.setDepartamentoAdministrativo(rs.getString("departamento_administrativo"));
                    r.setFechaInicioComision(rs.getDate("fecha_inicio_comision"));
                    r.setFechaFinComision(rs.getDate("fecha_fin_comision"));
                    r.setNombreSupervisor(rs.getString("nombre_completo"));
                    r.setEstado(rs.getString("estado"));
                    resultados.add(r);
                }
            }
            return resultados;
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al buscar las supervisiones.", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** Trae una supervisión completa por su id, para precargar la pantalla de edición. */
    public static Supervision buscarPorId(int idSupervision) throws SupervisionException {
        String sql = "SELECT cui_supervisor, region, orden_asignacion, fecha_inicio_comision, "
                + "fecha_fin_comision, fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, "
                + "direccion_administrativa, departamento_administrativo, departamento, municipio, "
                + "oficina_auxiliatura, autorizacion_viaticos, vehiculo_propio, observaciones, estado "
                + "FROM dbo.Supervisiones WHERE id_supervision = ?";
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Supervision s = new Supervision();
                s.setIdSupervision(idSupervision);
                s.setCuiSupervisor(rs.getString("cui_supervisor"));
                s.setRegion(rs.getString("region"));
                s.setOrdenAsignacion(rs.getString("orden_asignacion"));
                s.setFechaInicioComision(rs.getDate("fecha_inicio_comision"));
                s.setFechaFinComision(rs.getDate("fecha_fin_comision"));
                s.setFechaAlcanceInicial(rs.getDate("fecha_alcance_inicial"));
                s.setFechaAlcanceFinal(rs.getDate("fecha_alcance_final"));
                s.setTipoNombramiento(rs.getString("tipo_nombramiento"));
                s.setDireccionAdministrativa(rs.getString("direccion_administrativa"));
                s.setDepartamentoAdministrativo(rs.getString("departamento_administrativo"));
                s.setDepartamento(rs.getString("departamento"));
                s.setMunicipio(rs.getString("municipio"));
                s.setOficinaAuxiliatura(rs.getString("oficina_auxiliatura"));
                s.setAutorizacionViaticos(rs.getBoolean("autorizacion_viaticos"));
                s.setVehiculoPropio(rs.getBoolean("vehiculo_propio"));
                s.setObservaciones(rs.getString("observaciones"));
                s.setEstado(rs.getString("estado"));
                return s;
            }
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al obtener la supervisión.", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** Actualiza los datos editables de una supervisión existente (no cambia su estado). */
    public static void actualizar(Supervision s) throws SupervisionException {
        String sql = "UPDATE dbo.Supervisiones SET "
                + "cui_supervisor = ?, region = ?, orden_asignacion = ?, fecha_inicio_comision = ?, "
                + "fecha_fin_comision = ?, fecha_alcance_inicial = ?, fecha_alcance_final = ?, "
                + "tipo_nombramiento = ?, direccion_administrativa = ?, departamento_administrativo = ?, "
                + "departamento = ?, municipio = ?, oficina_auxiliatura = ?, autorizacion_viaticos = ?, "
                + "vehiculo_propio = ?, observaciones = ? "
                + "WHERE id_supervision = ?";
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            ps.setInt(17, s.getIdSupervision());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al actualizar la supervisión.", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Registra en el historial la confirmación de recepción por parte del
     * supervisor. No cambia el estado (la supervisión ya está APROBADA).
     */
    public static void registrarRecepcion(int idSupervision) throws SupervisionException {
        Usuario usuarioActual = Sesion.getInstancia().getUsuarioActual();
        if (usuarioActual == null) {
            throw new SupervisionException("No hay una sesión activa para registrar la recepción.");
        }
        String sql = "INSERT INTO dbo.Historial_Supervision "
                + "(id_supervision, estado_anterior, estado_nuevo, motivo_cambio, responsable_cui) "
                + "VALUES (?, 'APROBADO', 'APROBADO', ?, ?)";
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            ps.setString(2, "Confirmación de recepción por el supervisor");
            ps.setString(3, usuarioActual.getCui());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al registrar la recepción.", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Reprograma o anula una supervisión en estado PROGRAMADO: cambia su estado
     * (a REPROGRAMADO o CANCELADO), registra el historial con motivo y
     * observaciones, y si hay un documento adjunto lo registra también. Todo
     * en una sola transacción.
     */
    public static void reprogramarOAnular(int idSupervision, String nuevoEstado, String motivo,
            String observaciones, String tipoDocumento, String rutaArchivo) throws SupervisionException {
        if (!"REPROGRAMADO".equals(nuevoEstado) && !"CANCELADO".equals(nuevoEstado)) {
            throw new SupervisionException("El estado '" + nuevoEstado + "' no es válido para esta operación.");
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

            String updateSql = "UPDATE dbo.Supervisiones SET estado = ? "
                    + "WHERE id_supervision = ? AND estado = 'PROGRAMADO'";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idSupervision);
                int filasActualizadas = ps.executeUpdate();
                if (filasActualizadas == 0) {
                    throw new SupervisionException("La supervisión ya no está en estado PROGRAMADO.");
                }
            }

            String insertHistorialSql = "INSERT INTO dbo.Historial_Supervision "
                    + "(id_supervision, estado_anterior, estado_nuevo, motivo_cambio, observaciones, responsable_cui) "
                    + "VALUES (?, 'PROGRAMADO', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertHistorialSql)) {
                ps.setInt(1, idSupervision);
                ps.setString(2, nuevoEstado);
                ps.setString(3, motivo);
                ps.setString(4, observaciones);
                ps.setString(5, responsableCui);
                ps.executeUpdate();
            }

            if (rutaArchivo != null) {
                String insertDocSql = "INSERT INTO dbo.Documentos_Supervision "
                        + "(id_supervision, tipo_documento, ruta_archivo) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertDocSql)) {
                    ps.setInt(1, idSupervision);
                    ps.setString(2, tipoDocumento);
                    ps.setString(3, rutaArchivo);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new SupervisionException("Ocurrió un error al reprogramar/anular la supervisión.", e);
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

    /** Trae el historial de una supervisión, de la más antigua a la más reciente. */
    public static List<HistorialSupervision> obtenerHistorial(int idSupervision) throws SupervisionException {
        String sql = "SELECT h.estado_nuevo, h.fecha_hora, h.observaciones, u.nombre_completo "
                + "FROM dbo.Historial_Supervision h "
                + "JOIN dbo.Usuarios u ON u.cui = h.responsable_cui "
                + "WHERE h.id_supervision = ? "
                + "ORDER BY h.fecha_hora ASC";
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            List<HistorialSupervision> historial = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialSupervision h = new HistorialSupervision();
                    h.setEstado(rs.getString("estado_nuevo"));
                    h.setFechaHora(rs.getTimestamp("fecha_hora"));
                    h.setObservaciones(rs.getString("observaciones"));
                    h.setResponsableNombre(rs.getString("nombre_completo"));
                    historial.add(h);
                }
            }
            return historial;
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al obtener el historial.", e);
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
