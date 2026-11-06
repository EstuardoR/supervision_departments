
/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */


package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControlAcceso {

    public static final String FRM_ASIGNACION_SUPERVISION = "frmAsignacionSupervision";
    public static final String FRM_BANDEJA_PLANIFICACION = "frmBandejaPlanificacion";
    public static final String FRM_BANDEJA_EJECUCION = "frmBandejaEjecucion";
    public static final String FRM_RESULTADOS_SUPERVISION = "frmResultadosSupervision";
    public static final String FRM_ACCIONES = "frmAcciones";
    public static final String FRM_INFORME_FINAL = "frmInformeFinal";
    public static final String FRM_MATRIZ_SEGUIMIENTO = "frmMatrizSeguimiento";
    public static final String FRM_REPORTES = "frmReportes";
    public static final String FRM_USUARIOS = "frmUsuarios";

    private static final Map<String, Set<String>> PANTALLAS_POR_PERFIL = new HashMap<>();

    static {
        PANTALLAS_POR_PERFIL.put("ADMINISTRADOR", Set.of(
                FRM_REPORTES, FRM_USUARIOS));

        PANTALLAS_POR_PERFIL.put("DIRECTOR_DCI", Set.of(
                FRM_ASIGNACION_SUPERVISION, FRM_BANDEJA_PLANIFICACION, FRM_REPORTES));

        PANTALLAS_POR_PERFIL.put("JEFE_SUPERVISION", Set.of(
                FRM_ASIGNACION_SUPERVISION, FRM_BANDEJA_PLANIFICACION, FRM_REPORTES));

        PANTALLAS_POR_PERFIL.put("SUPERVISOR_DCI", Set.of(
                FRM_BANDEJA_PLANIFICACION, FRM_BANDEJA_EJECUCION, FRM_RESULTADOS_SUPERVISION,
                FRM_INFORME_FINAL, FRM_MATRIZ_SEGUIMIENTO));

        PANTALLAS_POR_PERFIL.put("OPERADOR", Set.of());

        PANTALLAS_POR_PERFIL.put("JEFE_OFICINA", Set.of(
                FRM_ACCIONES));
    }

    private ControlAcceso() {
    }

    public static boolean tieneAcceso(String perfil, String pantalla) {
        return PANTALLAS_POR_PERFIL.getOrDefault(perfil, Set.of()).contains(pantalla);
    }
}
