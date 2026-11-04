/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package util;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogos fijos de ejemplo para los combos de lugar de la asignación de
 * supervisión. Son listas de prueba; se deben ajustar con los valores reales
 * más adelante.
 */
public class Catalogos {

    private Catalogos() {
    }

    /** Lista de direcciones administrativas (usada cuando el tipo es ESPECÍFICA). */
    public static List<String> direccionesAdministrativas() {
        return Arrays.asList(
                "Dirección de Coordinación Institucional",
                "Dirección de Tecnología",
                "Dirección Jurídica"
        );
    }

    /** Lista de departamentos administrativos (usada cuando el tipo es ESPECÍFICA). */
    public static List<String> departamentosAdministrativos() {
        return Arrays.asList(
                "Departamento de Supervisión",
                "Departamento de Planificación",
                "Departamento Administrativo"
        );
    }

    /** Lista de departamentos (geográficos, usada cuando el tipo es CAMPO). */
    public static List<String> departamentos() {
        return Arrays.asList(
                "Guatemala",
                "Quetzaltenango",
                "Petén",
                "Huehuetenango",
                "Escuintla"
        );
    }

    /** Lista de municipios (usada cuando el tipo es CAMPO). */
    public static List<String> municipios() {
        return Arrays.asList(
                "Guatemala",
                "Mixco",
                "Quetzaltenango",
                "Flores",
                "Escuintla"
        );
    }

    /** Lista de oficinas / auxiliaturas (usada cuando el tipo es CAMPO). */
    public static List<String> oficinasAuxiliaturas() {
        return Arrays.asList(
                "Oficina Auxiliatura Municipal Guatemala",
                "Oficina Auxiliatura Municipal Mixco",
                "Oficina Auxiliatura Municipal Quetzaltenango"
        );
    }
}
