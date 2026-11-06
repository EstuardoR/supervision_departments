package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import model.CausaRaiz;
import model.HallazgoAccion;

/**
 * Acceso a datos de las pantallas de acciones y matriz de seguimiento:
 * Detalle_Resultados, Acciones_Preventivas_Correctivas, Analisis_Causa_Raiz
 * y Detalle_Cinco_Porque.
 */
public class AccionDAO {

    private static final String SQL_INSERT_ACCION = "INSERT INTO dbo.Acciones_Preventivas_Correctivas "
            + "(id_detalle_resultado, accion_inmediata, ruta_respaldo, acciones_por_efectuar, "
            + "direccion_responsable, departamento_responsable, estado_accion) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_ACCION = "UPDATE dbo.Acciones_Preventivas_Correctivas SET "
            + "accion_inmediata = ?, ruta_respaldo = ?, acciones_por_efectuar = ?, "
            + "direccion_responsable = ?, departamento_responsable = ?, estado_accion = ? "
            + "WHERE id_accion = ?";

    private AccionDAO() {
    }

    private static Connection abrir() throws SupervisionException {
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new SupervisionException("No se pudo establecer conexión con la base de datos.");
        }
        return conn;
    }

    private static void cerrar(Connection conn) {
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    /**
     * Lista supervisiones de un estado. Devuelve filas {id, oficina, fecha, estado}.
     *
     * @param cuiSupervisor  si no es null, solo las de ese supervisor
     * @param oficinaLike    si no es null, filtra oficina_auxiliatura LIKE ese valor
     * @param fechaInforme   true: fecha del informe (matriz); false: inicio de comisión
     */
    public static List<Object[]> listarSupervisiones(String estado, String cuiSupervisor,
            String oficinaLike, boolean fechaInforme) throws SupervisionException {
        String campoFecha = fechaInforme
                ? "(SELECT MAX(i.fecha_generacion) FROM dbo.Informe_Supervision i "
                + "WHERE i.id_supervision = s.id_supervision)"
                : "s.fecha_inicio_comision";
        StringBuilder sql = new StringBuilder("SELECT s.id_supervision, s.oficina_auxiliatura, "
                + campoFecha + " AS fecha, s.estado FROM dbo.Supervisiones s WHERE s.estado = ?");
        List<Object> params = new ArrayList<>();
        params.add(estado);
        if (cuiSupervisor != null) {
            sql.append(" AND s.cui_supervisor = ?");
            params.add(cuiSupervisor);
        }
        if (oficinaLike != null) {
            sql.append(" AND s.oficina_auxiliatura LIKE ?");
            params.add(oficinaLike);
        }
        sql.append(" ORDER BY s.id_supervision DESC");

        Connection conn = abrir();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<Object[]> filas = new ArrayList<>();
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha");
                    filas.add(new Object[]{
                        rs.getInt("id_supervision"),
                        rs.getString("oficina_auxiliatura"),
                        ts == null ? "" : formato.format(ts),
                        rs.getString("estado")});
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al listar las supervisiones.", e);
        } finally {
            cerrar(conn);
        }
    }

    /** Hallazgos de una supervisión con la sigla dada ("OM" o "D"), con su acción si ya existe. */
    public static List<HallazgoAccion> listarHallazgos(int idSupervision, String sigla)
            throws SupervisionException {
        String sql = "SELECT d.id_detalle_resultado, d.sigla, d.descripcion_resultado, "
                + "a.id_accion, a.accion_inmediata, a.ruta_respaldo, a.acciones_por_efectuar, "
                + "a.direccion_responsable, a.departamento_responsable, a.estado_accion, "
                + "CASE WHEN c.id_causa_raiz IS NULL THEN 0 ELSE 1 END AS tiene_causa "
                + "FROM dbo.Detalle_Resultados d "
                + "JOIN dbo.Resultados_Supervision r ON r.id_resultado = d.id_resultado "
                + "LEFT JOIN dbo.Acciones_Preventivas_Correctivas a "
                + "ON a.id_detalle_resultado = d.id_detalle_resultado "
                + "LEFT JOIN dbo.Analisis_Causa_Raiz c ON c.id_accion = a.id_accion "
                + "WHERE r.id_supervision = ? AND d.sigla = ? "
                + "ORDER BY d.id_detalle_resultado";
        Connection conn = abrir();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            ps.setString(2, sigla);
            List<HallazgoAccion> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HallazgoAccion h = new HallazgoAccion();
                    h.setIdDetalle(rs.getInt("id_detalle_resultado"));
                    h.setSigla(rs.getString("sigla"));
                    h.setDescripcion(rs.getString("descripcion_resultado"));
                    int idAccion = rs.getInt("id_accion");
                    if (!rs.wasNull()) {
                        h.setIdAccion(idAccion);
                        h.setAccionInmediata(rs.getString("accion_inmediata"));
                        h.setRutaRespaldo(rs.getString("ruta_respaldo"));
                        h.setPorEfectuar(rs.getBoolean("acciones_por_efectuar"));
                        h.setDireccionResponsable(rs.getString("direccion_responsable"));
                        h.setDepartamentoResponsable(rs.getString("departamento_responsable"));
                        h.setEstadoAccion(rs.getString("estado_accion"));
                        h.setTieneCausaRaiz(rs.getInt("tiene_causa") == 1);
                    }
                    lista.add(h);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al obtener los hallazgos.", e);
        } finally {
            cerrar(conn);
        }
    }

    /**
     * Guarda (inserta o actualiza) las acciones de la lista y, cuando el hallazgo
     * trae una causa raíz nueva, la guarda junto con sus 5 porqués. Todo en una
     * sola transacción.
     */
    public static void guardarAcciones(List<HallazgoAccion> lista) throws SupervisionException {
        Connection conn = abrir();
        try {
            conn.setAutoCommit(false);
            for (HallazgoAccion h : lista) {
                Integer idAccion = h.getIdAccion();
                if (idAccion == null) {
                    try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ACCION,
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, h.getIdDetalle());
                        llenarAccion(ps, 2, h);
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            rs.next();
                            idAccion = rs.getInt(1);
                        }
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ACCION)) {
                        llenarAccion(ps, 1, h);
                        ps.setInt(7, idAccion);
                        ps.executeUpdate();
                    }
                }
                if (h.getCausaRaiz() != null) {
                    guardarCausaRaiz(conn, idAccion, h.getCausaRaiz());
                }
            }
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new SupervisionException("Ocurrió un error al guardar las acciones.", e);
        } finally {
            cerrar(conn);
        }
    }

    private static void llenarAccion(PreparedStatement ps, int inicio, HallazgoAccion h)
            throws SQLException {
        ps.setString(inicio, h.getAccionInmediata());
        ps.setString(inicio + 1, h.getRutaRespaldo());
        ps.setBoolean(inicio + 2, Boolean.TRUE.equals(h.getPorEfectuar()));
        ps.setString(inicio + 3, h.getDireccionResponsable());
        ps.setString(inicio + 4, h.getDepartamentoResponsable());
        ps.setString(inicio + 5, h.getEstadoAccion());
    }

    /** Reemplaza la causa raíz de una acción (borra la anterior, si había) y guarda la nueva. */
    private static void guardarCausaRaiz(Connection conn, int idAccion, CausaRaiz c) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dbo.Detalle_Cinco_Porque WHERE id_causa_raiz IN "
                + "(SELECT id_causa_raiz FROM dbo.Analisis_Causa_Raiz WHERE id_accion = ?)")) {
            ps.setInt(1, idAccion);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dbo.Analisis_Causa_Raiz WHERE id_accion = ?")) {
            ps.setInt(1, idAccion);
            ps.executeUpdate();
        }

        int idCausa;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO dbo.Analisis_Causa_Raiz (id_accion, causa_raiz_principal, "
                + "contramedidas_propuestas, responsable_ejecucion, fecha_compromiso) "
                + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idAccion);
            ps.setString(2, c.getCausaRaizPrincipal());
            ps.setString(3, c.getContramedidas());
            ps.setString(4, c.getResponsable());
            ps.setDate(5, new java.sql.Date(c.getFechaCompromiso().getTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                idCausa = rs.getInt(1);
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO dbo.Detalle_Cinco_Porque (id_causa_raiz, numero_iteracion, "
                + "pregunta_porque, causa_identificada) VALUES (?, ?, ?, ?)")) {
            for (int i = 0; i < 5; i++) {
                ps.setInt(1, idCausa);
                ps.setInt(2, i + 1);
                ps.setString(3, c.getPreguntas()[i]);
                ps.setString(4, c.getCausas()[i]);
                ps.executeUpdate();
            }
        }
    }

    /** Cuántos hallazgos D/OM de la supervisión todavía no tienen una acción guardada. */
    public static int contarHallazgosSinAccion(int idSupervision) throws SupervisionException {
        String sql = "SELECT COUNT(*) FROM dbo.Detalle_Resultados d "
                + "JOIN dbo.Resultados_Supervision r ON r.id_resultado = d.id_resultado "
                + "LEFT JOIN dbo.Acciones_Preventivas_Correctivas a "
                + "ON a.id_detalle_resultado = d.id_detalle_resultado "
                + "WHERE r.id_supervision = ? AND d.sigla IN ('D', 'OM') AND a.id_accion IS NULL";
        Connection conn = abrir();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al revisar los hallazgos.", e);
        } finally {
            cerrar(conn);
        }
    }

    /** Correo institucional del supervisor de la supervisión (puede ser null). */
    public static String obtenerCorreoSupervisor(int idSupervision) throws SupervisionException {
        String sql = "SELECT u.correo_institucional FROM dbo.Supervisiones s "
                + "JOIN dbo.Usuarios u ON u.cui = s.cui_supervisor WHERE s.id_supervision = ?";
        Connection conn = abrir();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al obtener el correo del supervisor.", e);
        } finally {
            cerrar(conn);
        }
    }

    /**
     * Matriz de seguimiento: una fila por hallazgo (D/OM) con su acción, causa raíz,
     * contramedida, responsable y fecha compromiso. Columnas:
     * {hallazgo, descripción, acción inmediata, causa raíz, contramedida, responsable, fecha}.
     */
    public static List<Object[]> listarMatriz(int idSupervision) throws SupervisionException {
        String sql = "SELECT d.sigla, d.descripcion_resultado, a.accion_inmediata, "
                + "c.causa_raiz_principal, c.contramedidas_propuestas, "
                + "COALESCE(c.responsable_ejecucion, a.departamento_responsable) AS responsable, "
                + "c.fecha_compromiso "
                + "FROM dbo.Detalle_Resultados d "
                + "JOIN dbo.Resultados_Supervision r ON r.id_resultado = d.id_resultado "
                + "LEFT JOIN dbo.Acciones_Preventivas_Correctivas a "
                + "ON a.id_detalle_resultado = d.id_detalle_resultado "
                + "LEFT JOIN dbo.Analisis_Causa_Raiz c ON c.id_accion = a.id_accion "
                + "WHERE r.id_supervision = ? AND d.sigla IN ('D', 'OM') "
                + "ORDER BY d.sigla DESC, d.id_detalle_resultado";
        Connection conn = abrir();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            List<Object[]> filas = new ArrayList<>();
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            int numeroD = 0;
            int numeroOM = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sigla = rs.getString("sigla");
                    String etiqueta = "D".equals(sigla) ? "D-" + (++numeroD) : "OM-" + (++numeroOM);
                    java.sql.Date fecha = rs.getDate("fecha_compromiso");
                    filas.add(new Object[]{
                        etiqueta,
                        rs.getString("descripcion_resultado"),
                        rs.getString("accion_inmediata"),
                        rs.getString("causa_raiz_principal"),
                        rs.getString("contramedidas_propuestas"),
                        rs.getString("responsable"),
                        fecha == null ? "" : formato.format(fecha)});
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new SupervisionException("Ocurrió un error al generar la matriz.", e);
        } finally {
            cerrar(conn);
        }
    }
}