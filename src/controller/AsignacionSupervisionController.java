/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package controller;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import dao.SupervisionDAO;
import dao.SupervisionException;
import dao.UsuarioDAO;
import model.Supervision;
import model.Usuario;

/**
 * Contiene la lógica de la pantalla de asignación de supervisión: obtener los
 * datos que necesita la vista y validar/guardar una nueva asignación.
 */
public class AsignacionSupervisionController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Obtiene los usuarios con perfil SUPERVISOR_DCI, para llenar el combo de supervisores. */
    public List<Usuario> listarSupervisores() throws AsignacionSupervisionException {
        try {
            return usuarioDAO.listarPorPerfil("SUPERVISOR_DCI");
        } catch (SQLException e) {
            throw new AsignacionSupervisionException("No se pudo cargar la lista de supervisores.", e);
        }
    }

    /** Busca los datos de RRHH de un supervisor a partir de su CUI, para autocompletar la pantalla. */
    public Usuario obtenerDatosSupervisor(String cui) throws AsignacionSupervisionException {
        try {
            return usuarioDAO.buscarPorCui(cui);
        } catch (SQLException e) {
            throw new AsignacionSupervisionException("No se pudo obtener la información del supervisor.", e);
        }
    }

    /** Valida los datos de la asignación y, si son correctos, la guarda junto con su historial inicial. */
    public void guardarAsignacion(Supervision s) throws AsignacionSupervisionException {
        validar(s);
        try {
            SupervisionDAO.crear(s);
        } catch (SupervisionException e) {
            throw new AsignacionSupervisionException(e.getMessage(), e);
        }
    }

    /** Aplica, en el orden pedido, las reglas de negocio de la asignación de supervisión. */
    private void validar(Supervision s) throws AsignacionSupervisionException {
        if (isVacio(s.getCuiSupervisor()) || isVacio(s.getRegion()) || isVacio(s.getOrdenAsignacion())
                || s.getFechaInicioComision() == null || s.getFechaFinComision() == null
                || s.getFechaAlcanceInicial() == null || s.getFechaAlcanceFinal() == null) {
            throw new AsignacionSupervisionException(
                    "Supervisor, región, orden y las cuatro fechas son obligatorios.");
        }

        LocalDate inicio = aLocalDate(s.getFechaInicioComision());
        LocalDate fin = aLocalDate(s.getFechaFinComision());

        if (fin.isBefore(inicio)) {
            throw new AsignacionSupervisionException(
                    "La fecha fin de comisión no puede ser anterior a la fecha de inicio.");
        }

        if (diasHabilesEntre(inicio, fin) > 5) {
            throw new AsignacionSupervisionException(
                    "La comisión no puede exceder 5 días hábiles (lunes a viernes).");
        }

        if ("CAMPO".equals(s.getTipoNombramiento())) {
            if (isVacio(s.getDepartamento()) || isVacio(s.getMunicipio()) || isVacio(s.getOficinaAuxiliatura())) {
                throw new AsignacionSupervisionException(
                        "Para el tipo CAMPO debe indicar departamento, municipio y oficina/auxiliatura.");
            }
        } else {
            if (isVacio(s.getDireccionAdministrativa()) || isVacio(s.getDepartamentoAdministrativo())) {
                throw new AsignacionSupervisionException(
                        "Para el tipo ESPECÍFICA debe indicar dirección administrativa y departamento administrativo.");
            }
        }
    }

    private boolean isVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private LocalDate aLocalDate(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Cuenta los días hábiles (lunes a viernes, sin feriados) entre dos fechas, incluyendo ambos extremos. */
    private long diasHabilesEntre(LocalDate inicio, LocalDate fin) {
        long dias = 0;
        LocalDate actual = inicio;
        while (!actual.isAfter(fin)) {
            DayOfWeek diaSemana = actual.getDayOfWeek();
            if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {
                dias++;
            }
            actual = actual.plusDays(1);
        }
        return dias;
    }
}
