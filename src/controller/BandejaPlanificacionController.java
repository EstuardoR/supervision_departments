/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package controller;

import java.sql.SQLException;
import java.util.List;
import dao.DocumentoDAO;
import dao.SupervisionDAO;
import dao.SupervisionException;
import dao.UsuarioDAO;
import model.FiltroSupervision;
import model.HistorialSupervision;
import model.SupervisionResumen;
import model.Usuario;

/**
 * Contiene la lógica de la bandeja de planificación: búsquedas, y las
 * acciones de aprobar, recibir, y consultar documentos/historial.
 */
public class BandejaPlanificacionController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DocumentoDAO documentoDAO = new DocumentoDAO();

    /** Lista los usuarios con perfil SUPERVISOR_DCI, para el combo de filtro. */
    public List<Usuario> listarSupervisores() throws BandejaPlanificacionException {
        try {
            return usuarioDAO.listarPorPerfil("SUPERVISOR_DCI");
        } catch (SQLException e) {
            throw new BandejaPlanificacionException("No se pudo cargar la lista de supervisores.", e);
        }
    }

    /** Ejecuta la búsqueda de supervisiones con los filtros indicados. */
    public List<SupervisionResumen> buscar(FiltroSupervision filtro) throws BandejaPlanificacionException {
        try {
            return SupervisionDAO.buscar(filtro);
        } catch (SupervisionException e) {
            throw new BandejaPlanificacionException(e.getMessage(), e);
        }
    }

    /** Aprueba (firma) una supervisión en estado PROGRAMADO, dejándola en APROBADO. */
    public void aprobar(int idSupervision) throws BandejaPlanificacionException {
        try {
            SupervisionDAO.cambiarEstado(idSupervision, "APROBADO", "Aprobación (firma) del nombramiento");
        } catch (SupervisionException e) {
            throw new BandejaPlanificacionException(e.getMessage(), e);
        }
    }

    /** Registra la confirmación de recepción del supervisor, sin cambiar el estado. */
    public void registrarRecepcion(int idSupervision) throws BandejaPlanificacionException {
        try {
            SupervisionDAO.registrarRecepcion(idSupervision);
        } catch (SupervisionException e) {
            throw new BandejaPlanificacionException(e.getMessage(), e);
        }
    }

    /** Obtiene el historial de estados de una supervisión, de más antiguo a más reciente. */
    public List<HistorialSupervision> obtenerHistorial(int idSupervision) throws BandejaPlanificacionException {
        try {
            return SupervisionDAO.obtenerHistorial(idSupervision);
        } catch (SupervisionException e) {
            throw new BandejaPlanificacionException(e.getMessage(), e);
        }
    }

    /** Busca la ruta del documento solicitado; null si no existe. */
    public String buscarRutaDocumento(int idSupervision, String tipoDocumento) throws BandejaPlanificacionException {
        try {
            return documentoDAO.buscarRuta(idSupervision, tipoDocumento);
        } catch (SQLException e) {
            throw new BandejaPlanificacionException("No se pudo consultar el documento.", e);
        }
    }
}
