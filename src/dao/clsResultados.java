/*
 * Autor: Andrea Sofia Guerrero Castro
 * Carné: 9490-25-14813
 */
package dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import util.clsConnection;

public class clsResultados {

    // Datos de cada hallazgo
    public static class Hallazgo {

        public String sigla;
        public String descripcion;
        public String normativa;
        public File evidencia;
        public String accion;

        public Hallazgo(
                String sigla,
                String descripcion,
                String normativa,
                File evidencia,
                String accion) {

            this.sigla = sigla;
            this.descripcion = descripcion;
            this.normativa = normativa;
            this.evidencia = evidencia;
            this.accion = accion;
        }
    }

    // Guardar resultado y sus hallazgos
    public void guardarResultados(
            int idSupervision,
            LocalDate fechaPresentacion,
            LocalDate fechaLimite,
            String enlace,
            String observaciones,
            List<Hallazgo> hallazgos) throws Exception {

        if (idSupervision <= 0) {
            throw new Exception("ID de supervisión inválido.");
        }

        if (hallazgos == null || hallazgos.isEmpty()) {
            throw new Exception("Debe agregar al menos un hallazgo.");
        }

        // Verificar archivos antes de guardar
        for (Hallazgo h : hallazgos) {
            if (h.evidencia == null
                    || !h.evidencia.isFile()
                    || !h.evidencia.getName()
                            .toLowerCase().endsWith(".pdf")) {

                throw new Exception(
                    "Falta una evidencia PDF válida.");
            }
        }

        List<Path> archivosCopiados = new ArrayList<>();
        boolean guardado = false;

        try (Connection con = clsConnection.ConnectionDB()) {

            if (con == null) {
                throw new SQLException(
                    "No se pudo conectar con SQL Server.");
            }

            con.setAutoCommit(false);

            try {

                // 1. Verificar estado de supervisión
                String sqlEstado =
                    "SELECT estado FROM dbo.Supervisiones "
                    + "WHERE id_supervision = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlEstado)) {

                    ps.setInt(1, idSupervision);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()
                                || !"EJECUTADO".equals(
                                        rs.getString("estado"))) {

                            throw new SQLException(
                                "La supervisión debe estar "
                                + "en estado EJECUTADO.");
                        }
                    }
                }

                // 2. Buscar resultado existente
                Integer idResultado = null;

                String sqlBuscar =
                    "SELECT id_resultado, estado_resultado "
                    + "FROM dbo.Resultados_Supervision "
                    + "WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE id_supervision = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlBuscar)) {

                    ps.setInt(1, idSupervision);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                            idResultado = rs.getInt("id_resultado");

                            if (!"CREADO".equals(
                                    rs.getString("estado_resultado"))) {

                                throw new SQLException(
                                    "El resultado ya fue enviado "
                                    + "y no puede editarse.");
                            }

                            if (rs.next()) {
                                throw new SQLException(
                                    "Existen resultados duplicados "
                                    + "para esta supervisión.");
                            }
                        }
                    }
                }

                // 3. Insertar o actualizar resultado
                if (idResultado == null) {

                    String sqlInsertar =
                        "INSERT INTO dbo.Resultados_Supervision "
                        + "(id_supervision, fecha_presentacion, "
                        + "fecha_limite_acciones, "
                        + "enlace_videoconferencia, "
                        + "observaciones, estado_resultado) "
                        + "OUTPUT INSERTED.id_resultado "
                        + "VALUES (?, ?, ?, ?, ?, 'CREADO')";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlInsertar)) {

                        ps.setInt(1, idSupervision);
                        ps.setDate(2,
                            java.sql.Date.valueOf(fechaPresentacion));
                        ps.setDate(3,
                            java.sql.Date.valueOf(fechaLimite));
                        ps.setString(4, enlace);
                        ps.setString(5, observaciones);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                idResultado = rs.getInt(1);
                            } else {
                                throw new SQLException(
                                    "No se generó el ID del resultado.");
                            }
                        }
                    }

                } else {

                    String sqlActualizar =
                        "UPDATE dbo.Resultados_Supervision "
                        + "SET fecha_presentacion = ?, "
                        + "fecha_limite_acciones = ?, "
                        + "enlace_videoconferencia = ?, "
                        + "observaciones = ? "
                        + "WHERE id_resultado = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlActualizar)) {

                        ps.setDate(1,
                            java.sql.Date.valueOf(fechaPresentacion));
                        ps.setDate(2,
                            java.sql.Date.valueOf(fechaLimite));
                        ps.setString(3, enlace);
                        ps.setString(4, observaciones);
                        ps.setInt(5, idResultado);

                        ps.executeUpdate();
                    }

                    // Reemplazar detalles del borrador
                    String sqlEliminar =
                        "DELETE FROM dbo.Detalle_Resultados "
                        + "WHERE id_resultado = ?";

                    try (PreparedStatement ps =
                            con.prepareStatement(sqlEliminar)) {

                        ps.setInt(1, idResultado);
                        ps.executeUpdate();
                    }
                }

                // 4. Preparar carpeta de evidencias
                Path carpeta = Paths.get(
                    "documentos",
                    "supervision_" + idSupervision,
                    "evidencias"
                );

                Files.createDirectories(carpeta);

                // 5. Guardar todos los hallazgos
                String sqlDetalle =
                    "INSERT INTO dbo.Detalle_Resultados "
                    + "(id_resultado, sigla, descripcion_resultado, "
                    + "politica_normativa, ruta_evidencia_objetiva, "
                    + "accion_necesaria) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps =
                        con.prepareStatement(sqlDetalle)) {

                    for (Hallazgo h : hallazgos) {

                        String nombreArchivo =
                            UUID.randomUUID() + ".pdf";

                        Path destino = carpeta.resolve(nombreArchivo);

                        Files.copy(
                            h.evidencia.toPath(),
                            destino,
                            StandardCopyOption.REPLACE_EXISTING
                        );

                        archivosCopiados.add(destino);

                        ps.setInt(1, idResultado);
                        ps.setString(2, h.sigla);
                        ps.setString(3, h.descripcion);
                        ps.setString(4, h.normativa);
                        ps.setString(5,
                            destino.toAbsolutePath().toString());

                        if (h.accion == null
                                || h.accion.isBlank()) {
                            ps.setNull(6, java.sql.Types.VARCHAR);
                        } else {
                            ps.setString(6, h.accion);
                        }

                        ps.executeUpdate();
                    }
                }

                // 6. Confirmar toda la operación
                con.commit();
                guardado = true;

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } finally {

            // Limpiar copias nuevas si falló el guardado
            if (!guardado) {
                for (Path archivo : archivosCopiados) {
                    try {
                        Files.deleteIfExists(archivo);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
    
    
public static class ResultadoGuardado {

    public LocalDate fechaPresentacion;
    public LocalDate fechaLimite;
    public String enlace;
    public String observaciones;
    public String estado;

    public List<Hallazgo> hallazgos = new ArrayList<>();
}


public ResultadoGuardado cargarResultados(
        int idSupervision) throws SQLException {

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            throw new SQLException(
                "No se pudo conectar con SQL Server.");
        }

        String sql =
            "SELECT id_resultado, fecha_presentacion, "
            + "fecha_limite_acciones, enlace_videoconferencia, "
            + "observaciones, estado_resultado "
            + "FROM dbo.Resultados_Supervision "
            + "WHERE id_supervision = ?";

        ResultadoGuardado resultado = new ResultadoGuardado();
        int idResultado;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSupervision);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                idResultado = rs.getInt("id_resultado");

                java.sql.Date fecha1 =
                    rs.getDate("fecha_presentacion");

                java.sql.Date fecha2 =
                    rs.getDate("fecha_limite_acciones");

                resultado.fechaPresentacion =
                    fecha1 != null ? fecha1.toLocalDate() : null;

                resultado.fechaLimite =
                    fecha2 != null ? fecha2.toLocalDate() : null;

                resultado.enlace =
                    rs.getString("enlace_videoconferencia");

                resultado.observaciones =
                    rs.getString("observaciones");

                resultado.estado =
                    rs.getString("estado_resultado");

                if (rs.next()) {
                    throw new SQLException(
                        "Existen resultados duplicados "
                        + "para esta supervisión.");
                }
            }
        }

        String sqlHallazgos =
            "SELECT sigla, descripcion_resultado, "
            + "politica_normativa, ruta_evidencia_objetiva, "
            + "accion_necesaria "
            + "FROM dbo.Detalle_Resultados "
            + "WHERE id_resultado = ? "
            + "ORDER BY id_detalle_resultado";

        try (PreparedStatement ps =
                con.prepareStatement(sqlHallazgos)) {

            ps.setInt(1, idResultado);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String ruta =
                        rs.getString("ruta_evidencia_objetiva");

                    File evidencia = ruta != null
                            && !ruta.isBlank()
                            ? new File(ruta)
                            : null;

                    resultado.hallazgos.add(
                        new Hallazgo(
                            rs.getString("sigla"),
                            rs.getString("descripcion_resultado"),
                            rs.getString("politica_normativa"),
                            evidencia,
                            rs.getString("accion_necesaria")
                        )
                    );
                }
            }
        }

        return resultado;
    }
}



public boolean puedeGenerarDocumento(
        int idSupervision) throws SQLException {

    String sql =
        "SELECT COUNT(*) AS total "
        + "FROM dbo.Resultados_Supervision r "
        + "INNER JOIN dbo.Supervisiones s "
        + "ON r.id_supervision = s.id_supervision "
        + "WHERE r.id_supervision = ? "
        + "AND r.estado_resultado = 'CREADO' "
        + "AND s.estado = 'EJECUTADO' "
        + "AND EXISTS ("
        + "SELECT 1 FROM dbo.Detalle_Resultados d "
        + "WHERE d.id_resultado = r.id_resultado)";

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

/** Marca el resultado como enviado, una vez generado y firmado el documento. */
public void marcarComoEnviado(int idSupervision) throws SQLException {

    String sql =
        "UPDATE dbo.Resultados_Supervision "
        + "SET estado_resultado = 'ENVIADO' "
        + "WHERE id_supervision = ? AND estado_resultado = 'CREADO'";

    try (Connection con = clsConnection.ConnectionDB()) {

        if (con == null) {
            throw new SQLException(
                "No se pudo conectar con SQL Server.");
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSupervision);
            ps.executeUpdate();
        }
    }
}


}
