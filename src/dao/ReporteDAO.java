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
import model.FilaReporte;
import model.FiltroReporte;

/**
 * Consultas de solo lectura para la pantalla de reportes/indicadores, todas
 * agrupadas por oficina/auxiliatura.
 */
public class ReporteDAO {

    private static final String ESTADOS_VISITADA =
            "'INICIADO','EJECUTADO','RESULTADOS','ACCIONES','INFORME','SEGUIMIENTO'";

    private ReporteDAO() {
    }

    /** Cantidad de supervisiones "visitadas" (en un estado posterior a aprobado) por oficina. */
    public static List<FilaReporte> visitas(FiltroReporte filtro, boolean descendente) throws ReporteException {
        List<Object> parametros = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.oficina_auxiliatura, COUNT(*) AS cantidad "
                        + "FROM dbo.Supervisiones s "
                        + "WHERE s.oficina_auxiliatura IS NOT NULL "
                        + "AND s.estado IN (" + ESTADOS_VISITADA + ")");
        sql.append(construirFiltros(filtro, parametros));
        sql.append(" GROUP BY s.oficina_auxiliatura ORDER BY cantidad ").append(descendente ? "DESC" : "ASC");
        return ejecutar(sql.toString(), parametros);
    }

    /** Cantidad de hallazgos de un tipo (sigla F, D u OM) por oficina, solo de resultados ENVIADO. */
    public static List<FilaReporte> hallazgosPorSigla(FiltroReporte filtro, String sigla, boolean descendente)
            throws ReporteException {
        List<Object> parametros = new ArrayList<>();
        parametros.add(sigla);
        StringBuilder sql = new StringBuilder(
                "SELECT s.oficina_auxiliatura, COUNT(*) AS cantidad "
                        + "FROM dbo.Supervisiones s "
                        + "JOIN dbo.Resultados_Supervision r ON r.id_supervision = s.id_supervision "
                        + "JOIN dbo.Detalle_Resultados d ON d.id_resultado = r.id_resultado "
                        + "WHERE s.oficina_auxiliatura IS NOT NULL "
                        + "AND d.sigla = ? AND r.estado_resultado = 'ENVIADO'");
        sql.append(construirFiltros(filtro, parametros));
        sql.append(" GROUP BY s.oficina_auxiliatura ORDER BY cantidad ").append(descendente ? "DESC" : "ASC");
        return ejecutar(sql.toString(), parametros);
    }

    /** Hallazgos D u OM que todavía no tienen ninguna acción preventiva/correctiva registrada, por oficina. */
    public static List<FilaReporte> pendientesPorAtender(FiltroReporte filtro) throws ReporteException {
        List<Object> parametros = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.oficina_auxiliatura, COUNT(*) AS cantidad "
                        + "FROM dbo.Supervisiones s "
                        + "JOIN dbo.Resultados_Supervision r ON r.id_supervision = s.id_supervision "
                        + "JOIN dbo.Detalle_Resultados d ON d.id_resultado = r.id_resultado "
                        + "LEFT JOIN dbo.Acciones_Preventivas_Correctivas a ON a.id_detalle_resultado = d.id_detalle_resultado "
                        + "WHERE s.oficina_auxiliatura IS NOT NULL "
                        + "AND d.sigla IN ('D','OM') AND a.id_accion IS NULL AND r.estado_resultado = 'ENVIADO'");
        sql.append(construirFiltros(filtro, parametros));
        sql.append(" GROUP BY s.oficina_auxiliatura ORDER BY cantidad DESC");
        return ejecutar(sql.toString(), parametros);
    }

    /** Oficinas que ya atendieron todos sus hallazgos D/OM (ninguno quedó sin acción registrada). */
    public static List<FilaReporte> sinPendientes(FiltroReporte filtro) throws ReporteException {
        List<Object> parametros = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.oficina_auxiliatura, COUNT(*) AS cantidad "
                        + "FROM dbo.Supervisiones s "
                        + "JOIN dbo.Resultados_Supervision r ON r.id_supervision = s.id_supervision "
                        + "JOIN dbo.Detalle_Resultados d ON d.id_resultado = r.id_resultado "
                        + "LEFT JOIN dbo.Acciones_Preventivas_Correctivas a ON a.id_detalle_resultado = d.id_detalle_resultado "
                        + "WHERE s.oficina_auxiliatura IS NOT NULL "
                        + "AND d.sigla IN ('D','OM') AND r.estado_resultado = 'ENVIADO'");
        sql.append(construirFiltros(filtro, parametros));
        sql.append(" GROUP BY s.oficina_auxiliatura "
                + "HAVING SUM(CASE WHEN a.id_accion IS NULL THEN 1 ELSE 0 END) = 0 "
                + "ORDER BY cantidad DESC");
        return ejecutar(sql.toString(), parametros);
    }

    /** Agrega a la consulta, con PreparedStatement, solo los filtros que el usuario haya llenado. */
    private static String construirFiltros(FiltroReporte filtro, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();
        if (filtro.getAnio() != null) {
            sql.append(" AND YEAR(s.fecha_inicio_comision) = ?");
            parametros.add(filtro.getAnio());
        }
        if (filtro.getRegion() != null) {
            sql.append(" AND s.region = ?");
            parametros.add(filtro.getRegion());
        }
        if (filtro.getDepartamento() != null) {
            sql.append(" AND s.departamento = ?");
            parametros.add(filtro.getDepartamento());
        }
        if (filtro.getFechaDesde() != null) {
            sql.append(" AND s.fecha_inicio_comision >= ?");
            parametros.add(new java.sql.Date(filtro.getFechaDesde().getTime()));
        }
        if (filtro.getFechaHasta() != null) {
            sql.append(" AND s.fecha_inicio_comision <= ?");
            parametros.add(new java.sql.Date(filtro.getFechaHasta().getTime()));
        }
        return sql.toString();
    }

    /** Ejecuta la consulta armada con sus parámetros y arma la lista de filas oficina/cantidad. */
    private static List<FilaReporte> ejecutar(String sql, List<Object> parametros) throws ReporteException {
        Connection conn = util.clsConnection.ConnectionDB();
        if (conn == null) {
            throw new ReporteException("No se pudo establecer conexión con la base de datos.");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            List<FilaReporte> filas = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FilaReporte fila = new FilaReporte();
                    fila.setOficina(rs.getString("oficina_auxiliatura"));
                    fila.setCantidad(rs.getInt("cantidad"));
                    filas.add(fila);
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new ReporteException("Ocurrió un error al generar el reporte.", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
