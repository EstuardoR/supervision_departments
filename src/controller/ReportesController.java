/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package controller;

import java.util.List;
import dao.ReporteDAO;
import dao.ReporteException;
import model.FilaReporte;
import model.FiltroReporte;

/**
 * Puente simple entre la pantalla de reportes y el ReporteDAO: cada método
 * ejecuta una consulta y traduce la excepción del DAO a una de pantalla.
 */
public class ReportesController {

    public List<FilaReporte> visitas(FiltroReporte filtro, boolean descendente) throws ReportesException {
        try {
            return ReporteDAO.visitas(filtro, descendente);
        } catch (ReporteException e) {
            throw new ReportesException(e.getMessage(), e);
        }
    }

    public List<FilaReporte> hallazgosPorSigla(FiltroReporte filtro, String sigla, boolean descendente)
            throws ReportesException {
        try {
            return ReporteDAO.hallazgosPorSigla(filtro, sigla, descendente);
        } catch (ReporteException e) {
            throw new ReportesException(e.getMessage(), e);
        }
    }

    public List<FilaReporte> pendientesPorAtender(FiltroReporte filtro) throws ReportesException {
        try {
            return ReporteDAO.pendientesPorAtender(filtro);
        } catch (ReporteException e) {
            throw new ReportesException(e.getMessage(), e);
        }
    }

    public List<FilaReporte> sinPendientes(FiltroReporte filtro) throws ReportesException {
        try {
            return ReporteDAO.sinPendientes(filtro);
        } catch (ReporteException e) {
            throw new ReportesException(e.getMessage(), e);
        }
    }
}
