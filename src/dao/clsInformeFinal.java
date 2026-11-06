/*
 * Autor: Andrea Sofia Guerrero Castro
 * Carné: 9490-25-14813
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import util.clsConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.clsConnection;

public class clsInformeFinal {

    public void guardarInforme(
            int idSupervision,
            String objetivos,
            String documentos,
            List<Integer> hallazgosSeleccionados
    ) throws SQLException {

        if (idSupervision <= 0) {
            throw new SQLException("ID de supervisión inválido.");
        }

        if (objetivos == null || objetivos.isBlank()
                || documentos == null || documentos.isBlank()) {
            throw new SQLException("Debe completar los campos.");
        }

        if (hallazgosSeleccionados == null
                || hallazgosSeleccionados.isEmpty()) {
            throw new SQLException(
                    "Debe seleccionar al menos un hallazgo.");
        }

        try (Connection con = clsConnection.ConnectionDB()) {

            if (con == null) {
                throw new SQLException(
                        "No se pudo conectar con SQL Server.");
            }

            con.setAutoCommit(false);

            try {

                // Buscar un informe existente
                Integer idInforme = null;

                String sqlBuscar =
                        "SELECT id_informe, estado_informe "
                        + "FROM dbo.Informe_Supervision "
                        + "WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE id_supervision = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlBuscar)) {

                    ps.setInt(1, idSupervision);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            idInforme = rs.getInt("id_informe");

                            if (!"CREADO".equals(
                                    rs.getString("estado_informe"))) {
                                throw new SQLException(
                                        "El informe ya fue finalizado.");
                            }

                            if (rs.next()) {
                                throw new SQLException(
                                        "Hay informes duplicados.");
                            }
                        }
                    }
                }

                if (idInforme == null) {

                    // Crear informe
                    String sqlInsertar =
                            "INSERT INTO dbo.Informe_Supervision "
                            + "(id_supervision, objetivos_especificos, "
                            + "documentos_examinados, estado_informe) "
                            + "OUTPUT INSERTED.id_informe "
                            + "VALUES (?, ?, ?, 'CREADO')";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlInsertar)) {

                        ps.setInt(1, idSupervision);
                        ps.setString(2, objetivos);
                        ps.setString(3, documentos);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "No se generó el ID del informe.");
                            }

                            idInforme = rs.getInt(1);
                        }
                    }

                } else {

                    // Actualizar borrador
                    String sqlActualizar =
                            "UPDATE dbo.Informe_Supervision "
                            + "SET objetivos_especificos = ?, "
                            + "documentos_examinados = ? "
                            + "WHERE id_informe = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlActualizar)) {

                        ps.setString(1, objetivos);
                        ps.setString(2, documentos);
                        ps.setInt(3, idInforme);

                        ps.executeUpdate();
                    }

                    // Limpiar selecciones anteriores
                    String sqlEliminar =
                            "UPDATE dbo.Detalle_Resultados "
                            + "SET id_informe = NULL "
                            + "WHERE id_informe = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlEliminar)) {

                        ps.setInt(1, idInforme);
                        ps.executeUpdate();
                    }
                }

                // Guardar hallazgos seleccionados
                // Verificar que pertenezcan a esta supervisión
                String sqlHallazgo =
                        "UPDATE d SET d.id_informe = ? "
                        + "FROM dbo.Detalle_Resultados d "
                        + "INNER JOIN dbo.Resultados_Supervision r "
                        + "ON d.id_resultado = r.id_resultado "
                        + "WHERE d.id_detalle_resultado = ? "
                        + "AND r.id_supervision = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlHallazgo)) {

                    for (Integer idHallazgo : hallazgosSeleccionados) {

                        ps.setInt(1, idInforme);
                        ps.setInt(2, idHallazgo);
                        ps.setInt(3, idSupervision);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Hallazgo inválido: " + idHallazgo);
                        }
                    }
                }

                con.commit();

            } catch (Exception e) {

                con.rollback();

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        "Error al guardar el informe.", e);
            }
        }
    }
    
    public static class InformeGuardado {

    public int idInforme;
    public String objetivosEspecificos;
    public String documentosExaminados;
    public String estadoInforme;

    public List<Integer> hallazgosSeleccionados =
            new ArrayList<>();
}
    
    
public InformeGuardado cargarInforme(
        int idSupervision) throws SQLException {

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            throw new SQLException(
                    "No se pudo conectar con SQL Server.");
        }

        String sqlInforme =
                "SELECT id_informe, objetivos_especificos, "
                + "documentos_examinados, estado_informe "
                + "FROM dbo.Informe_Supervision "
                + "WHERE id_supervision = ?";

        InformeGuardado informe = new InformeGuardado();

        try (PreparedStatement ps =
                con.prepareStatement(sqlInforme)) {

            ps.setInt(1, idSupervision);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                informe.idInforme =
                        rs.getInt("id_informe");

                informe.objetivosEspecificos =
                        rs.getString("objetivos_especificos");

                informe.documentosExaminados =
                        rs.getString("documentos_examinados");

                informe.estadoInforme =
                        rs.getString("estado_informe");

                if (rs.next()) {
                    throw new SQLException(
                            "Existen informes duplicados "
                            + "para esta supervisión.");
                }
            }
        }

        // Recuperar los hallazgos incluidos
        String sqlHallazgos =
                "SELECT id_detalle_resultado "
                + "FROM dbo.Detalle_Resultados "
                + "WHERE id_informe = ?";

        try (PreparedStatement ps =
                con.prepareStatement(sqlHallazgos)) {

            ps.setInt(1, informe.idInforme);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    informe.hallazgosSeleccionados.add(
                            rs.getInt("id_detalle_resultado")
                    );
                }
            }
        }

        return informe;
    }
}


public boolean puedeGenerarDocumento(
        int idSupervision) throws SQLException {

    String sql =
        "SELECT COUNT(*) AS total "
        + "FROM dbo.Informe_Supervision i "
        + "WHERE i.id_supervision = ? "
        + "AND i.estado_informe = 'CREADO' "
        + "AND EXISTS ("
        + "SELECT 1 FROM dbo.Detalle_Resultados d "
        + "WHERE d.id_informe = i.id_informe)";

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            throw new SQLException(
                    "No se pudo conectar con SQL Server.");
        }

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idSupervision);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("total") == 1;
                }
            }
        }
    }

    return false;
}


}
