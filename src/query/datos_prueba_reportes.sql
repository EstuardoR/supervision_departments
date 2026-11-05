/*==============================================================================
  Datos de prueba para FrmReportes (Visitas, Fortalezas, Deficiencias,
  Oportunidades de mejora, Pendientes por atender, Sin pendientes).

  Ejecución sugerida: desde SSMS, con la base supervision_db ya creada por
  supervisor_dependenciasDB.sql (ejecutar ese script primero).

  Supervisor usado: CUI 1000000040404 (SUPERVISOR_DCI, ya sembrado).
==============================================================================*/
USE supervision_db;
GO

DECLARE @supAntigua1 AS INT, @supAntigua2 AS INT, @supAntiguaProgramada AS INT;
DECLARE @resAntigua AS INT;
DECLARE @detAntiguaD AS INT, @detAntiguaOM AS INT;

-- ============================================================================
-- Oficina Antigua: 2 supervisiones "visitadas" (EJECUTADO e INFORME) y una
-- PROGRAMADA (no debe contar como visita). Resultado con 1 fortaleza, 1
-- deficiencia ya atendida y 1 oportunidad de mejora todavía pendiente.
-- ============================================================================
INSERT INTO dbo.Supervisiones (
    cui_supervisor, region, orden_asignacion, fecha_inicio_comision, fecha_fin_comision,
    fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, direccion_administrativa,
    departamento_administrativo, departamento, municipio, oficina_auxiliatura,
    autorizacion_viaticos, vehiculo_propio, observaciones, estado
)
VALUES ('1000000040404', '5', 'OA-PRUEBA-001', '2026-02-10', '2026-02-12', '2026-01-01', '2026-06-30',
        'CAMPO', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión',
        'Sacatepéquez', 'Antigua Guatemala', 'Oficina Auxiliatura Municipal Antigua',
        1, 0, 'Dato de prueba para reportes.', 'EJECUTADO');
SET @supAntigua1 = SCOPE_IDENTITY();

INSERT INTO dbo.Supervisiones (
    cui_supervisor, region, orden_asignacion, fecha_inicio_comision, fecha_fin_comision,
    fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, direccion_administrativa,
    departamento_administrativo, departamento, municipio, oficina_auxiliatura,
    autorizacion_viaticos, vehiculo_propio, observaciones, estado
)
VALUES ('1000000040404', '5', 'OA-PRUEBA-002', '2026-03-05', '2026-03-06', '2026-01-01', '2026-06-30',
        'CAMPO', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión',
        'Sacatepéquez', 'Antigua Guatemala', 'Oficina Auxiliatura Municipal Antigua',
        1, 0, 'Segunda visita de prueba a la misma oficina.', 'INFORME');
SET @supAntigua2 = SCOPE_IDENTITY();

INSERT INTO dbo.Supervisiones (
    cui_supervisor, region, orden_asignacion, fecha_inicio_comision, fecha_fin_comision,
    fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, direccion_administrativa,
    departamento_administrativo, departamento, municipio, oficina_auxiliatura,
    autorizacion_viaticos, vehiculo_propio, observaciones, estado
)
VALUES ('1000000040404', '5', 'OA-PRUEBA-003', '2026-04-01', '2026-04-03', '2026-01-01', '2026-06-30',
        'CAMPO', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión',
        'Sacatepéquez', 'Antigua Guatemala', 'Oficina Auxiliatura Municipal Antigua',
        1, 0, 'Programada, aún no se visita: no debe contar en Visitas.', 'PROGRAMADO');
SET @supAntiguaProgramada = SCOPE_IDENTITY();

INSERT INTO dbo.Resultados_Supervision (
    id_supervision, nombre_registrador_civil, fecha_presentacion, enlace_videoconferencia,
    fecha_limite_acciones, observaciones, estado_resultado
)
VALUES (@supAntigua1, 'Registrador de prueba Antigua', '2026-02-12', 'https://meet.supervision.gob.gt/prueba-antigua',
        '2026-03-05', 'Resultado de prueba para reportes.', 'ENVIADO');
SET @resAntigua = SCOPE_IDENTITY();

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resAntigua, 'F', 'Buen manejo del archivo físico.', 'Manual de Procedimientos Registrales', NULL);

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resAntigua, 'D', 'Expedientes incompletos.', 'Manual de Procedimientos Registrales, numeral 4.2', 'AC');
SET @detAntiguaD = SCOPE_IDENTITY();

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resAntigua, 'OM', 'Oportunidad de digitalizar el archivo.', 'Plan Estratégico Institucional', 'AP');
SET @detAntiguaOM = SCOPE_IDENTITY();

-- La deficiencia (D) ya tiene acción registrada: no debe quedar pendiente.
INSERT INTO dbo.Acciones_Preventivas_Correctivas (
    id_detalle_resultado, accion_inmediata, acciones_por_efectuar, direccion_responsable,
    departamento_responsable, estado_accion
)
VALUES (@detAntiguaD, 'Se instruyó completar los expedientes pendientes.', 1,
        'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'EN_PROCESO');

-- La oportunidad de mejora (OM) NO tiene acción todavía: debe aparecer en "Pendientes por atender".

GO
-- ============================================================================
-- Oficina Chimaltenango: 1 supervisión "visitada" (RESULTADOS) con una
-- deficiencia y una oportunidad de mejora, AMBAS ya atendidas: debe aparecer
-- en "Sin pendientes" y no en "Pendientes por atender".
-- ============================================================================
DECLARE @supChim AS INT, @resChim AS INT, @detChimD AS INT, @detChimOM AS INT;

INSERT INTO dbo.Supervisiones (
    cui_supervisor, region, orden_asignacion, fecha_inicio_comision, fecha_fin_comision,
    fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, direccion_administrativa,
    departamento_administrativo, departamento, municipio, oficina_auxiliatura,
    autorizacion_viaticos, vehiculo_propio, observaciones, estado
)
VALUES ('1000000040404', '4', 'OA-PRUEBA-004', '2026-02-20', '2026-02-21', '2026-01-01', '2026-06-30',
        'ESPECIFICA', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión',
        'Chimaltenango', 'Chimaltenango', 'Oficina Auxiliatura Municipal Chimaltenango',
        0, 1, 'Dato de prueba para reportes.', 'RESULTADOS');
SET @supChim = SCOPE_IDENTITY();

INSERT INTO dbo.Resultados_Supervision (
    id_supervision, nombre_registrador_civil, fecha_presentacion, enlace_videoconferencia,
    fecha_limite_acciones, observaciones, estado_resultado
)
VALUES (@supChim, 'Registrador de prueba Chimaltenango', '2026-02-21', 'https://meet.supervision.gob.gt/prueba-chimaltenango',
        '2026-03-14', 'Resultado de prueba para reportes.', 'ENVIADO');
SET @resChim = SCOPE_IDENTITY();

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resChim, 'D', 'Cobro de tarifas sin recibo.', 'Reglamento de Tarifas Institucional', 'AC');
SET @detChimD = SCOPE_IDENTITY();

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resChim, 'OM', 'Oportunidad de capacitar al personal de ventanilla.', 'Plan Estratégico Institucional', 'AP');
SET @detChimOM = SCOPE_IDENTITY();

INSERT INTO dbo.Acciones_Preventivas_Correctivas (
    id_detalle_resultado, accion_inmediata, acciones_por_efectuar, direccion_responsable,
    departamento_responsable, estado_accion
)
VALUES (@detChimD, 'Suspensión inmediata del cobro indebido.', 1,
        'Dirección de Coordinación Institucional', 'Oficina Auxiliatura Municipal Chimaltenango', 'FINALIZADO');

INSERT INTO dbo.Acciones_Preventivas_Correctivas (
    id_detalle_resultado, accion_inmediata, acciones_por_efectuar, direccion_responsable,
    departamento_responsable, estado_accion
)
VALUES (@detChimOM, 'Se programó capacitación mensual al personal.', 1,
        'Dirección de Coordinación Institucional', 'Oficina Auxiliatura Municipal Chimaltenango', 'FINALIZADO');

GO
-- ============================================================================
-- Oficina Quetzaltenango: 1 supervisión "visitada" (INICIADO) con dos
-- fortalezas y ningún hallazgo D/OM: no debe aparecer ni en "Pendientes por
-- atender" ni en "Sin pendientes" (solo oficinas con al menos un D/OM).
-- ============================================================================
DECLARE @supQuetzal AS INT, @resQuetzal AS INT;

INSERT INTO dbo.Supervisiones (
    cui_supervisor, region, orden_asignacion, fecha_inicio_comision, fecha_fin_comision,
    fecha_alcance_inicial, fecha_alcance_final, tipo_nombramiento, direccion_administrativa,
    departamento_administrativo, departamento, municipio, oficina_auxiliatura,
    autorizacion_viaticos, vehiculo_propio, observaciones, estado
)
VALUES ('1000000040404', '6', 'OA-PRUEBA-005', '2026-05-04', '2026-05-05', '2026-01-01', '2026-06-30',
        'CAMPO', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión',
        'Quetzaltenango', 'Quetzaltenango', 'Oficina Auxiliatura Municipal Quetzaltenango Prueba',
        1, 0, 'Dato de prueba para reportes.', 'INICIADO');
SET @supQuetzal = SCOPE_IDENTITY();

INSERT INTO dbo.Resultados_Supervision (
    id_supervision, nombre_registrador_civil, fecha_presentacion, enlace_videoconferencia,
    fecha_limite_acciones, observaciones, estado_resultado
)
VALUES (@supQuetzal, 'Registrador de prueba Quetzaltenango', '2026-05-05', 'https://meet.supervision.gob.gt/prueba-quetzaltenango',
        '2026-05-26', 'Resultado de prueba para reportes.', 'ENVIADO');
SET @resQuetzal = SCOPE_IDENTITY();

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resQuetzal, 'F', 'Atención rápida a usuarios.', 'Manual de Procedimientos Registrales', NULL);

INSERT INTO dbo.Detalle_Resultados (id_resultado, sigla, descripcion_resultado, politica_normativa, accion_necesaria)
VALUES (@resQuetzal, 'F', 'Instalaciones limpias y ordenadas.', 'Manual de Procedimientos Registrales', NULL);

GO
