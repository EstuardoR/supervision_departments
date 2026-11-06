/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.DocumentoDAO;
import dao.SupervisionDAO;
import dao.SupervisionException;
import dao.UsuarioDAO;
import java.io.IOException;
import model.FiltroSupervision;
import model.HistorialSupervision;
import model.Supervision;
import model.SupervisionResumen;
import model.Usuario;
import servicios.GeneradorDocumentoService;
import servicios.NotificacionService;

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

    /** Tipo de documento con el que se registra el Anexo I (Nombramiento) en Documentos_Supervision. */
    public static final String TIPO_OFICIO_NOMBRAMIENTO = "Oficio de Nombramiento";

    /**
     * Aprueba (firma) una supervisión en estado PROGRAMADO, dejándola en APROBADO, y genera
     * el documento de Nombramiento (Anexo I). Un fallo al generar el documento no deshace la
     * aprobación ya guardada, igual que un fallo de correo.
     */
    public void aprobar(int idSupervision) throws BandejaPlanificacionException {
        try {
            SupervisionDAO.cambiarEstado(idSupervision, "PROGRAMADO", "APROBADO", "Aprobación (firma) del nombramiento");
            generarNombramiento(idSupervision);
        } catch (SupervisionException e) {
            throw new BandejaPlanificacionException(e.getMessage(), e);
        }
    }

    private void generarNombramiento(int idSupervision) {
        try {
            Supervision s = SupervisionDAO.buscarPorId(idSupervision);
            Usuario supervisor = usuarioDAO.buscarPorCui(s.getCuiSupervisor());
            String cuerpo = "<table>"
                    + fila("No. de supervisión", String.valueOf(idSupervision))
                    + fila("Supervisor DCI", supervisor != null ? supervisor.getNombreCompleto() : s.getCuiSupervisor())
                    + fila("Región", s.getRegion())
                    + fila("Tipo de nombramiento", s.getTipoNombramiento())
                    + fila("Oficina/Dirección", s.getOficinaAuxiliatura() != null
                            ? s.getOficinaAuxiliatura() : s.getDireccionAdministrativa())
                    + fila("Fecha inicio de comisión", String.valueOf(s.getFechaInicioComision()))
                    + fila("Fecha fin de comisión", String.valueOf(s.getFechaFinComision()))
                    + "</table>"
                    + "<p class=\"firma\">Aprobado (firma simulada) el " + java.time.LocalDateTime.now()
                    + "</p>";
            String ruta = GeneradorDocumentoService.generar(idSupervision, "Anexo_I_Nombramiento",
                    "Anexo I - Nombramiento de supervisión", cuerpo);
            documentoDAO.registrar(idSupervision, TIPO_OFICIO_NOMBRAMIENTO, ruta);
        } catch (SupervisionException | SQLException | IOException e) {
            // Un fallo al generar el documento no debe impedir que la aprobación ya quede guardada.
        }
    }

    private static String fila(String etiqueta, String valor) {
        return "<tr><th>" + etiqueta + "</th><td>" + (valor == null ? "" : valor) + "</td></tr>";
    }

    /**
     * Registra la confirmación de recepción del supervisor, sin cambiar el estado,
     * y notifica por correo a JEFE_SUPERVISION, DIRECTOR_DCI y al supervisor asignado.
     *
     * @return true si la notificación se envió, false si falló el envío.
     */
    public boolean registrarRecepcion(int idSupervision) throws BandejaPlanificacionException {
        try {
            SupervisionDAO.registrarRecepcion(idSupervision);
            Supervision supervision = SupervisionDAO.buscarPorId(idSupervision);
            return enviarNotificacionRecepcion(idSupervision, supervision.getCuiSupervisor());
        } catch (SupervisionException e) {
            throw new BandejaPlanificacionException(e.getMessage(), e);
        }
    }

    private boolean enviarNotificacionRecepcion(int idSupervision, String cuiSupervisor) {
        try {
            List<String> correos = new ArrayList<>();
            agregarCorreosDePerfil(correos, "JEFE_SUPERVISION");
            agregarCorreosDePerfil(correos, "DIRECTOR_DCI");
            agregarSiTieneCorreo(correos, usuarioDAO.buscarPorCui(cuiSupervisor));

            String asunto = "Supervisión #" + idSupervision + " - Recepción confirmada";
            String cuerpo = "El supervisor confirmó la recepción del nombramiento de la supervisión #"
                    + idSupervision + ".";

            return NotificacionService.enviar(correos, asunto, cuerpo);
        } catch (SQLException e) {
            return false;
        }
    }

    private void agregarCorreosDePerfil(List<String> correos, String perfil) throws SQLException {
        for (Usuario usuario : usuarioDAO.listarPorPerfil(perfil)) {
            agregarSiTieneCorreo(correos, usuario);
        }
    }

    private void agregarSiTieneCorreo(List<String> correos, Usuario usuario) {
        if (usuario != null && usuario.getCorreoInstitucional() != null
                && !usuario.getCorreoInstitucional().isEmpty()) {
            correos.add(usuario.getCorreoInstitucional());
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
