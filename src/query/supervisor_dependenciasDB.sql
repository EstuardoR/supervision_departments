/*==============================================================================
  Ejecución sugerida:
  desde SQL Server Management Studio (SSMS) abriendo el archivo y
  ejecutando todo el script (los bloques están separados con GO).

------------------------------------------------------------------------------
  CREDENCIALES PREDETERMINADAS (datos de prueba)
------------------------------------------------------------------------------
  Perfil              | CUI (usuario)   | Contraseña
  --------------------|-----------------|--------------------
  ADMINISTRADOR       | 1000000010101   | admin123
  DIRECTOR_DCI        | 1000000020202   | director123
  JEFE_SUPERVISION    | 1000000030303   | jefesupervision123
  SUPERVISOR_DCI      | 1000000040404   | supervisor123
  OPERADOR            | 1000000050505   | operador123
  JEFE_OFICINA        | 1000000060606   | jefeoficina123

==============================================================================*/
-- ============================================================================
-- 1. CREACIÓN DE BASE DE DATOS
-- ============================================================================
IF NOT EXISTS (SELECT 1
               FROM   sys.databases
               WHERE  name = N'supervision_db')
    BEGIN
        CREATE DATABASE supervision_db COLLATE Modern_Spanish_CI_AS;
    END


GO
USE supervision_db;


GO
-- ============================================================================
-- 2. ELIMINACIÓN DE TABLAS EXISTENTES (orden: hijas -> padres)
-- ============================================================================
IF OBJECT_ID(N'dbo.Informe_Supervision', 'U') IS NOT NULL
    DROP TABLE dbo.Informe_Supervision;

IF OBJECT_ID(N'dbo.Detalle_Cinco_Porque', 'U') IS NOT NULL
    DROP TABLE dbo.Detalle_Cinco_Porque;

IF OBJECT_ID(N'dbo.Analisis_Causa_Raiz', 'U') IS NOT NULL
    DROP TABLE dbo.Analisis_Causa_Raiz;

IF OBJECT_ID(N'dbo.Acciones_Preventivas_Correctivas', 'U') IS NOT NULL
    DROP TABLE dbo.Acciones_Preventivas_Correctivas;

IF OBJECT_ID(N'dbo.Detalle_Resultados', 'U') IS NOT NULL
    DROP TABLE dbo.Detalle_Resultados;

IF OBJECT_ID(N'dbo.Resultados_Supervision', 'U') IS NOT NULL
    DROP TABLE dbo.Resultados_Supervision;

IF OBJECT_ID(N'dbo.Documentos_Supervision', 'U') IS NOT NULL
    DROP TABLE dbo.Documentos_Supervision;

IF OBJECT_ID(N'dbo.Historial_Supervision', 'U') IS NOT NULL
    DROP TABLE dbo.Historial_Supervision;

IF OBJECT_ID(N'dbo.Supervisiones', 'U') IS NOT NULL
    DROP TABLE dbo.Supervisiones;

IF OBJECT_ID(N'dbo.Usuarios', 'U') IS NOT NULL
    DROP TABLE dbo.Usuarios;


GO
-- ============================================================================
-- 3. CREACIÓN DE TABLAS (orden: padres -> hijas)
-- ============================================================================
-- Usuarios -------------------------------------------------------------------
CREATE TABLE dbo.Usuarios (
    cui                  VARCHAR (13)   NOT NULL,
    contrasena           VARCHAR (255)  NOT NULL,
    nombre_completo      NVARCHAR (150) NOT NULL,
    perfil               VARCHAR (20)   NOT NULL,
    direccion_rrhh       NVARCHAR (150) NULL,
    unidad_rrhh          NVARCHAR (150) NULL,
    puesto_rrhh          NVARCHAR (150) NULL,
    codigo_empleado      VARCHAR (20)   NULL,
    correo_institucional VARCHAR (150)  NULL,
    CONSTRAINT PK_Usuarios PRIMARY KEY (cui),
    CONSTRAINT CK_Usuarios_perfil CHECK (perfil IN ('ADMINISTRADOR', 'DIRECTOR_DCI', 'JEFE_SUPERVISION', 'SUPERVISOR_DCI', 'OPERADOR', 'JEFE_OFICINA'))
);


GO
-- Supervisiones ----------------------------------------------------------------
CREATE TABLE dbo.Supervisiones (
    id_supervision               INT            IDENTITY (1, 1) NOT NULL,
    cui_supervisor               VARCHAR (13)   NOT NULL,
    region                       NVARCHAR (100) NULL,
    orden_asignacion             VARCHAR (50)   NULL,
    fecha_inicio_comision        DATE           NULL,
    fecha_fin_comision           DATE           NULL,
    fecha_alcance_inicial        DATE           NULL,
    fecha_alcance_final          DATE           NULL,
    tipo_nombramiento            VARCHAR (20)   NULL,
    direccion_administrativa     NVARCHAR (150) NULL,
    departamento_administrativo  NVARCHAR (150) NULL,
    departamento                 NVARCHAR (100) NULL,
    municipio                    NVARCHAR (100) NULL,
    oficina_auxiliatura          NVARCHAR (150) NULL,
    autorizacion_viaticos        BIT            DEFAULT (0) NOT NULL,
    vehiculo_propio              BIT            DEFAULT (0) NOT NULL,
    id_nombramiento_reprogramado INT            NULL,
    observaciones                NVARCHAR (MAX) NULL,
    estado                       VARCHAR (20)   DEFAULT ('PROGRAMADO') NOT NULL,
    fecha_creacion               DATETIME       DEFAULT (GETDATE()) NOT NULL,
    CONSTRAINT PK_Supervisiones PRIMARY KEY (id_supervision),
    CONSTRAINT FK_Supervisiones_Usuarios FOREIGN KEY (cui_supervisor) REFERENCES dbo.Usuarios (cui),
    CONSTRAINT FK_Supervisiones_Reprogramado FOREIGN KEY (id_nombramiento_reprogramado) REFERENCES dbo.Supervisiones (id_supervision),
    CONSTRAINT CK_Supervisiones_tipo_nombramiento CHECK (tipo_nombramiento IN ('CAMPO', 'ESPECIFICA')),
    CONSTRAINT CK_Supervisiones_estado CHECK (estado IN ('PROGRAMADO', 'APROBADO', 'INICIADO', 'EJECUTADO', 'RESULTADOS', 'ACCIONES', 'INFORME', 'SEGUIMIENTO', 'CANCELADO'))
);


GO
-- Historial_Supervision --------------------------------------------------------
CREATE TABLE dbo.Historial_Supervision (
    id_historial    INT            IDENTITY (1, 1) NOT NULL,
    id_supervision  INT            NOT NULL,
    estado_anterior VARCHAR (20)   NULL,
    estado_nuevo    VARCHAR (20)   NULL,
    motivo_cambio   NVARCHAR (500) NULL,
    observaciones   NVARCHAR (MAX) NULL,
    responsable_cui VARCHAR (13)   NOT NULL,
    fecha_hora      DATETIME       DEFAULT (GETDATE()) NOT NULL,
    CONSTRAINT PK_Historial_Supervision PRIMARY KEY (id_historial),
    CONSTRAINT FK_Historial_Supervisiones FOREIGN KEY (id_supervision) REFERENCES dbo.Supervisiones (id_supervision),
    CONSTRAINT FK_Historial_Usuarios FOREIGN KEY (responsable_cui) REFERENCES dbo.Usuarios (cui)
);


GO
-- Documentos_Supervision ---------------------------------------------------
CREATE TABLE dbo.Documentos_Supervision (
    id_documento   INT            IDENTITY (1, 1) NOT NULL,
    id_supervision INT            NOT NULL,
    tipo_documento NVARCHAR (100) NULL,
    ruta_archivo   NVARCHAR (400) NULL,
    fecha_carga    DATETIME       DEFAULT (GETDATE()) NOT NULL,
    CONSTRAINT PK_Documentos_Supervision PRIMARY KEY (id_documento),
    CONSTRAINT FK_Documentos_Supervisiones FOREIGN KEY (id_supervision) REFERENCES dbo.Supervisiones (id_supervision)
);


GO
-- Resultados_Supervision -----------------------------------------------------
CREATE TABLE dbo.Resultados_Supervision (
    id_resultado             INT            IDENTITY (1, 1) NOT NULL,
    id_supervision           INT            NOT NULL,
    nombre_registrador_civil NVARCHAR (150) NULL,
    fecha_presentacion       DATE           NULL,
    enlace_videoconferencia  NVARCHAR (400) NULL,
    fecha_limite_acciones    DATE           NULL,
    observaciones            NVARCHAR (MAX) NULL,
    estado_resultado         VARCHAR (30)   NULL,
    CONSTRAINT PK_Resultados_Supervision PRIMARY KEY (id_resultado),
    CONSTRAINT FK_Resultados_Supervisiones FOREIGN KEY (id_supervision) REFERENCES dbo.Supervisiones (id_supervision)
);


GO
-- Detalle_Resultados -----------------------------------------------------------
CREATE TABLE dbo.Detalle_Resultados (
    id_detalle_resultado    INT            IDENTITY (1, 1) NOT NULL,
    id_resultado            INT            NOT NULL,
    sigla                   VARCHAR (2)    NOT NULL,
    descripcion_resultado   NVARCHAR (MAX) NULL,
    politica_normativa      NVARCHAR (300) NULL,
    ruta_evidencia_objetiva NVARCHAR (400) NULL,
    accion_necesaria        VARCHAR (2)    NULL,
    CONSTRAINT PK_Detalle_Resultados PRIMARY KEY (id_detalle_resultado),
    CONSTRAINT FK_Detalle_Resultados_Resultados FOREIGN KEY (id_resultado) REFERENCES dbo.Resultados_Supervision (id_resultado),
    CONSTRAINT CK_Detalle_Resultados_sigla CHECK (sigla IN ('D', 'OM', 'F')),
    CONSTRAINT CK_Detalle_Resultados_accion_necesaria CHECK (accion_necesaria IN ('AC', 'AP'))
);


GO
-- Acciones_Preventivas_Correctivas -----------------------------------------
CREATE TABLE dbo.Acciones_Preventivas_Correctivas (
    id_accion                INT            IDENTITY (1, 1) NOT NULL,
    id_detalle_resultado     INT            NOT NULL,
    accion_inmediata         NVARCHAR (MAX) NULL,
    ruta_respaldo            NVARCHAR (400) NULL,
    acciones_por_efectuar    BIT            DEFAULT (0) NOT NULL,
    direccion_responsable    NVARCHAR (150) NULL,
    departamento_responsable NVARCHAR (150) NULL,
    estado_accion            VARCHAR (30)   NULL,
    CONSTRAINT PK_Acciones_Preventivas_Correctivas PRIMARY KEY (id_accion),
    CONSTRAINT FK_Acciones_Detalle_Resultados FOREIGN KEY (id_detalle_resultado) REFERENCES dbo.Detalle_Resultados (id_detalle_resultado)
);


GO
-- Analisis_Causa_Raiz ------------------------------------------------------
CREATE TABLE dbo.Analisis_Causa_Raiz (
    id_causa_raiz            INT            IDENTITY (1, 1) NOT NULL,
    id_accion                INT            NOT NULL,
    causa_raiz_principal     NVARCHAR (MAX) NULL,
    contramedidas_propuestas NVARCHAR (MAX) NULL,
    responsable_ejecucion    NVARCHAR (150) NULL,
    fecha_compromiso         DATE           NULL,
    CONSTRAINT PK_Analisis_Causa_Raiz PRIMARY KEY (id_causa_raiz),
    CONSTRAINT FK_Causa_Raiz_Acciones FOREIGN KEY (id_accion) REFERENCES dbo.Acciones_Preventivas_Correctivas (id_accion)
);


GO
-- Detalle_Cinco_Porque -------------------------------------------------------
CREATE TABLE dbo.Detalle_Cinco_Porque (
    id_porque          INT            IDENTITY (1, 1) NOT NULL,
    id_causa_raiz      INT            NOT NULL,
    numero_iteracion   INT            NOT NULL,
    pregunta_porque    NVARCHAR (300) NULL,
    causa_identificada NVARCHAR (MAX) NULL,
    CONSTRAINT PK_Detalle_Cinco_Porque PRIMARY KEY (id_porque),
    CONSTRAINT FK_Cinco_Porque_Causa_Raiz FOREIGN KEY (id_causa_raiz) REFERENCES dbo.Analisis_Causa_Raiz (id_causa_raiz),
    CONSTRAINT CK_Cinco_Porque_iteracion CHECK (numero_iteracion BETWEEN 1 AND 5)
);


GO
-- Informe_Supervision ----------------------------------------------------------
CREATE TABLE dbo.Informe_Supervision (
    id_informe            INT            IDENTITY (1, 1) NOT NULL,
    id_supervision        INT            NOT NULL,
    objetivos_especificos NVARCHAR (MAX) NULL,
    documentos_examinados NVARCHAR (MAX) NULL,
    observaciones         NVARCHAR (MAX) NULL,
    estado_informe        VARCHAR (30)   NULL,
    fecha_generacion      DATETIME       DEFAULT (GETDATE()) NOT NULL,
    CONSTRAINT PK_Informe_Supervision PRIMARY KEY (id_informe),
    CONSTRAINT FK_Informe_Supervisiones FOREIGN KEY (id_supervision) REFERENCES dbo.Supervisiones (id_supervision)
);


GO
-- ============================================================================
-- 4. DATOS SEMILLA
-- ============================================================================
-- 4.1 Usuarios: un registro por cada perfil ------------------------------------
INSERT  INTO dbo.Usuarios (
    cui,
    contrasena,
    nombre_completo,
    perfil,
    direccion_rrhh,
    unidad_rrhh,
    puesto_rrhh,
    codigo_empleado,
    correo_institucional
)
VALUES                    ('1000000010101', 'admin123', 'Ana Lucía Ramírez Solís', 'ADMINISTRADOR', 'Dirección de Tecnología', 'Unidad de Sistemas', 'Administrador de Sistema', 'EMP-0001', 'admin.sistema@supervision.gob.gt'),
                          ('1000000020202', 'director123', 'Carlos Alberto Méndez Girón', 'DIRECTOR_DCI', 'Dirección de Coordinación Institucional', 'Despacho de Dirección', 'Director DCI', 'EMP-0002', 'carlos.mendez@supervision.gob.gt'),
                          ('1000000030303', 'jefesupervision123', 'María Fernanda López Castro', 'JEFE_SUPERVISION', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'Jefe de Supervisión', 'EMP-0003', 'maria.lopez@supervision.gob.gt'),
                          ('1000000040404', 'supervisor123', 'José Manuel Hernández Us', 'SUPERVISOR_DCI', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'Supervisor DCI', 'EMP-0004', 'jose.hernandez@supervision.gob.gt'),
                          ('1000000050505', 'operador123', 'Lucía Marisol Pérez Tzoc', 'OPERADOR', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'Operador de Datos', 'EMP-0005', 'lucia.perez@supervision.gob.gt'),
                          ('1000000060606', 'jefeoficina123', 'Francisco Javier Ixcot Pu', 'JEFE_OFICINA', 'Oficinas Auxiliares', 'Oficina Auxiliatura Municipal', 'Jefe de Oficina', 'EMP-0006', 'francisco.ixcot@supervision.gob.gt');


GO
-- 4.2 Supervisión de prueba #1 (flujo completo hasta INFORME) -----------------
DECLARE @sup1 AS INT, @res1 AS INT, @det1 AS INT, @det2 AS INT, @acc1 AS INT, @causa1 AS INT;

INSERT  INTO dbo.Supervisiones (
    cui_supervisor,
    region,
    orden_asignacion,
    fecha_inicio_comision,
    fecha_fin_comision,
    fecha_alcance_inicial,
    fecha_alcance_final,
    tipo_nombramiento,
    direccion_administrativa,
    departamento_administrativo,
    departamento,
    municipio,
    oficina_auxiliatura,
    autorizacion_viaticos,
    vehiculo_propio,
    id_nombramiento_reprogramado,
    observaciones,
    estado
)
VALUES                         ('1000000040404', 'Región VI - Suroccidente', 'OA-2026-001', '2026-03-02', '2026-03-06', '2026-01-01', '2026-02-28', 'CAMPO', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'Quetzaltenango', 'Quetzaltenango', 'Oficina Auxiliatura Municipal Quetzaltenango', 1, 0, NULL, 'Supervisión ordinaria programada conforme a plan anual.', 'INFORME');

SET @sup1 = SCOPE_IDENTITY();

INSERT  INTO dbo.Historial_Supervision (
    id_supervision,
    estado_anterior,
    estado_nuevo,
    motivo_cambio,
    observaciones,
    responsable_cui
)
VALUES                                 (@sup1, NULL, 'PROGRAMADO', 'Asignación inicial de comisión', 'Registro inicial de la supervisión.', '1000000030303'),
                                       (@sup1, 'PROGRAMADO', 'APROBADO', 'Visto bueno de jefatura', 'Orden de comisión aprobada.', '1000000020202'),
                                       (@sup1, 'APROBADO', 'INICIADO', 'Inicio de comisión en campo', 'Supervisor se presenta en oficina auxiliatura.', '1000000040404'),
                                       (@sup1, 'INICIADO', 'EJECUTADO', 'Supervisión finalizada en sitio', 'Se concluyó el trabajo de campo.', '1000000040404'),
                                       (@sup1, 'EJECUTADO', 'INFORME', 'Generación de informe final', 'Informe de supervisión generado.', '1000000030303');

INSERT  INTO dbo.Documentos_Supervision (
    id_supervision,
    tipo_documento,
    ruta_archivo
)
VALUES                                  (@sup1, 'Orden de Comisión', '/documentos/supervision_1/orden_comision.pdf'),
                                        (@sup1, 'Acta de Supervisión', '/documentos/supervision_1/acta_supervision.pdf');

INSERT  INTO dbo.Resultados_Supervision (
    id_supervision,
    nombre_registrador_civil,
    fecha_presentacion,
    enlace_videoconferencia,
    fecha_limite_acciones,
    observaciones,
    estado_resultado
)
VALUES                                  (@sup1, 'Pedro Antonio Son Cuc', '2026-03-06', 'https://meet.supervision.gob.gt/sup-001', '2026-03-27', 'Presentación de resultados ante jefatura de oficina.', 'PRESENTADO');

SET @res1 = SCOPE_IDENTITY();

INSERT  INTO dbo.Detalle_Resultados (
    id_resultado,
    sigla,
    descripcion_resultado,
    politica_normativa,
    ruta_evidencia_objetiva,
    accion_necesaria
)
VALUES                              (@res1, 'D', 'Expedientes de inscripción incompletos.', 'Manual de Procedimientos Registrales, numeral 4.2', '/documentos/supervision_1/evidencia_deficiencia.pdf', 'AC');

SET @det1 = SCOPE_IDENTITY();

INSERT  INTO dbo.Detalle_Resultados (
    id_resultado,
    sigla,
    descripcion_resultado,
    politica_normativa,
    ruta_evidencia_objetiva,
    accion_necesaria
)
VALUES                              (@res1, 'OM', 'Oportunidad de digitalizar el archivo físico.', 'Plan Estratégico Institucional, eje 3', '/documentos/supervision_1/evidencia_mejora.pdf', 'AP');

SET @det2 = SCOPE_IDENTITY();

INSERT  INTO dbo.Acciones_Preventivas_Correctivas (
    id_detalle_resultado,
    accion_inmediata,
    ruta_respaldo,
    acciones_por_efectuar,
    direccion_responsable,
    departamento_responsable,
    estado_accion
)
VALUES                                            (@det1, 'Se instruyó al personal completar los expedientes pendientes en un plazo de 10 días.', '/documentos/supervision_1/respaldo_accion.pdf', 1, 'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'EN_PROCESO');

SET @acc1 = SCOPE_IDENTITY();

INSERT  INTO dbo.Analisis_Causa_Raiz (
    id_accion,
    causa_raiz_principal,
    contramedidas_propuestas,
    responsable_ejecucion,
    fecha_compromiso
)
VALUES                               (@acc1, 'Falta de capacitación del personal en el llenado de expedientes.', 'Capacitación mensual y lista de verificación obligatoria.', 'María Fernanda López Castro', '2026-04-15');

SET @causa1 = SCOPE_IDENTITY();

INSERT  INTO dbo.Detalle_Cinco_Porque (
    id_causa_raiz,
    numero_iteracion,
    pregunta_porque,
    causa_identificada
)
VALUES                                (@causa1, 1, '¿Por qué los expedientes están incompletos?', 'Porque falta documentación de respaldo al momento de la inscripción.'),
                                      (@causa1, 2, '¿Por qué falta documentación de respaldo?', 'Porque el personal no verifica la lista de requisitos.'),
                                      (@causa1, 3, '¿Por qué el personal no verifica la lista de requisitos?', 'Porque no existe una lista de verificación estandarizada.'),
                                      (@causa1, 4, '¿Por qué no existe una lista de verificación estandarizada?', 'Porque no se ha actualizado el manual de procedimientos.'),
                                      (@causa1, 5, '¿Por qué no se ha actualizado el manual de procedimientos?', 'Porque no hay un responsable asignado a su actualización periódica.');

INSERT  INTO dbo.Informe_Supervision (
    id_supervision,
    objetivos_especificos,
    documentos_examinados,
    observaciones,
    estado_informe
)
VALUES                               (@sup1, 'Verificar el cumplimiento de los procedimientos de inscripción registral.', 'Expedientes de inscripción, libros de registro, actas administrativas.', 'Se identificaron oportunidades de mejora en el proceso de archivo.', 'FINALIZADO');


GO
-- 4.3 Supervisión de prueba #2 (flujo en curso) --------------------------------
DECLARE @sup2 AS INT, @res2 AS INT, @det3 AS INT, @acc2 AS INT, @causa2 AS INT;

INSERT  INTO dbo.Supervisiones (
    cui_supervisor,
    region,
    orden_asignacion,
    fecha_inicio_comision,
    fecha_fin_comision,
    fecha_alcance_inicial,
    fecha_alcance_final,
    tipo_nombramiento,
    direccion_administrativa,
    departamento_administrativo,
    departamento,
    municipio,
    oficina_auxiliatura,
    autorizacion_viaticos,
    vehiculo_propio,
    id_nombramiento_reprogramado,
    observaciones,
    estado
)
VALUES                         ('1000000050505', 'Región VII - Norte', 'OA-2026-002', '2026-04-13', '2026-04-15', '2026-02-01', '2026-03-31', 'ESPECIFICA', 'Dirección de Coordinación Institucional', 'Departamento de Supervisión', 'Petén', 'Flores', 'Oficina Auxiliatura Municipal Flores', 1, 1, NULL, 'Supervisión específica derivada de denuncia ciudadana.', 'RESULTADOS');

SET @sup2 = SCOPE_IDENTITY();

INSERT  INTO dbo.Historial_Supervision (
    id_supervision,
    estado_anterior,
    estado_nuevo,
    motivo_cambio,
    observaciones,
    responsable_cui
)
VALUES                                 (@sup2, NULL, 'PROGRAMADO', 'Asignación por denuncia ciudadana', 'Registro inicial de la supervisión específica.', '1000000030303'),
                                       (@sup2, 'PROGRAMADO', 'APROBADO', 'Visto bueno de jefatura', 'Orden de comisión aprobada.', '1000000020202'),
                                       (@sup2, 'APROBADO', 'INICIADO', 'Inicio de comisión en campo', 'Operador inicia verificación en sitio.', '1000000050505'),
                                       (@sup2, 'INICIADO', 'EJECUTADO', 'Supervisión finalizada en sitio', 'Se concluyó la verificación.', '1000000050505'),
                                       (@sup2, 'EJECUTADO', 'RESULTADOS', 'Presentación de resultados', 'Resultados presentados a jefatura de oficina.', '1000000030303');

INSERT  INTO dbo.Documentos_Supervision (
    id_supervision,
    tipo_documento,
    ruta_archivo
)
VALUES                                  (@sup2, 'Orden de Comisión', '/documentos/supervision_2/orden_comision.pdf');

INSERT  INTO dbo.Resultados_Supervision (
    id_supervision,
    nombre_registrador_civil,
    fecha_presentacion,
    enlace_videoconferencia,
    fecha_limite_acciones,
    observaciones,
    estado_resultado
)
VALUES                                  (@sup2, 'Rosa Elvira Choc Tut', '2026-04-15', 'https://meet.supervision.gob.gt/sup-002', '2026-05-06', 'Pendiente de definir acciones correctivas.', 'PRESENTADO');

SET @res2 = SCOPE_IDENTITY();

INSERT  INTO dbo.Detalle_Resultados (
    id_resultado,
    sigla,
    descripcion_resultado,
    politica_normativa,
    ruta_evidencia_objetiva,
    accion_necesaria
)
VALUES                              (@res2, 'F', 'Cobro indebido de tarifas no autorizadas a usuarios.', 'Reglamento de Tarifas Institucional, art. 12', '/documentos/supervision_2/evidencia_falta.pdf', 'AC');

SET @det3 = SCOPE_IDENTITY();

INSERT  INTO dbo.Acciones_Preventivas_Correctivas (
    id_detalle_resultado,
    accion_inmediata,
    ruta_respaldo,
    acciones_por_efectuar,
    direccion_responsable,
    departamento_responsable,
    estado_accion
)
VALUES                                            (@det3, 'Suspensión inmediata del cobro indebido y notificación a usuarios afectados.', '/documentos/supervision_2/respaldo_accion.pdf', 1, 'Dirección de Coordinación Institucional', 'Oficina Auxiliatura Municipal Flores', 'PENDIENTE');

SET @acc2 = SCOPE_IDENTITY();

INSERT  INTO dbo.Analisis_Causa_Raiz (
    id_accion,
    causa_raiz_principal,
    contramedidas_propuestas,
    responsable_ejecucion,
    fecha_compromiso
)
VALUES                               (@acc2, 'Desconocimiento del tarifario vigente por parte del personal de ventanilla.', 'Divulgación del tarifario actualizado y auditorías mensuales de caja.', 'Francisco Javier Ixcot Pu', '2026-05-20');

SET @causa2 = SCOPE_IDENTITY();

INSERT  INTO dbo.Detalle_Cinco_Porque (
    id_causa_raiz,
    numero_iteracion,
    pregunta_porque,
    causa_identificada
)
VALUES                                (@causa2, 1, '¿Por qué se cobraron tarifas no autorizadas?', 'Porque el personal de ventanilla desconocía el tarifario vigente.'),
                                      (@causa2, 2, '¿Por qué el personal desconocía el tarifario vigente?', 'Porque no se le hizo llegar la actualización más reciente.'),
                                      (@causa2, 3, '¿Por qué no se le hizo llegar la actualización?', 'Porque no existe un procedimiento formal de divulgación de cambios.');