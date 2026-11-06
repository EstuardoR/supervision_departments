/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.SupervisionDAO;
import dao.SupervisionException;
import dao.UsuarioDAO;
import model.Supervision;
import model.Usuario;
import servicios.NotificacionService;
import util.ArchivoService;

/**
 * Contiene la lógica del diálogo Reprogramar/Anular: traer la supervisión,
 * guardar el cambio (con su documento opcional) y notificar por correo.
 */
public class ReprogramarAnularController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Trae la supervisión por id, para mostrar su fase actual y validar el flujo. */
    public Supervision obtenerSupervision(int idSupervision) throws ReprogramarAnularException {
        try {
            return SupervisionDAO.buscarPorId(idSupervision);
        } catch (SupervisionException e) {
            throw new ReprogramarAnularException(e.getMessage(), e);
        }
    }

    /**
     * Guarda la reprogramación/anulación: copia el archivo adjunto (si hay),
     * cambia el estado y registra el historial/documento en una sola
     * transacción, y finalmente intenta notificar por correo.
     *
     * @return true si la notificación se envió (o no había nada que notificar
     *         por no existir aún el servicio), false si falló el envío.
     */
    public boolean guardar(int idSupervision, String nuevoEstado, String motivo, String observaciones,
            File archivoAdjunto, String cuiSupervisorAsignado) throws ReprogramarAnularException {
        String rutaDestino = null;
        String tipoDocumento = null;
        if (archivoAdjunto != null) {
            tipoDocumento = "REPROGRAMADO".equals(nuevoEstado)
                    ? "OFICIO DE REPROGRAMACION" : "JUSTIFICACION DE ANULACION";
            try {
                rutaDestino = ArchivoService.copiarAExpediente(idSupervision, archivoAdjunto);
            } catch (IOException e) {
                throw new ReprogramarAnularException("No se pudo copiar el archivo adjunto.", e);
            }
        }

        try {
            SupervisionDAO.reprogramarOAnular(idSupervision, nuevoEstado, motivo, observaciones,
                    tipoDocumento, rutaDestino);
        } catch (SupervisionException e) {
            throw new ReprogramarAnularException(e.getMessage(), e);
        }

        return enviarNotificacion(idSupervision, nuevoEstado, motivo, cuiSupervisorAsignado);
    }

    /**
     * Arma la lista de destinatarios (JEFE_SUPERVISION, DIRECTOR_DCI y el
     * supervisor asignado) y el texto de la notificación. El fallo al enviar
     * el correo no afecta el cambio ya guardado; solo se avisa aparte.
     */
    private boolean enviarNotificacion(int idSupervision, String nuevoEstado, String motivo,
            String cuiSupervisorAsignado) {
        try {
            List<String> correos = new ArrayList<>();
            agregarCorreosDePerfil(correos, "JEFE_SUPERVISION");
            agregarCorreosDePerfil(correos, "DIRECTOR_DCI");
            Usuario supervisor = usuarioDAO.buscarPorCui(cuiSupervisorAsignado);
            agregarSiTieneCorreo(correos, supervisor);

            String asunto = "Supervisión #" + idSupervision + " - " + nuevoEstado;
            String cuerpo = "La supervisión #" + idSupervision + " fue marcada como " + nuevoEstado
                    + ".\nMotivo: " + motivo;

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
}
