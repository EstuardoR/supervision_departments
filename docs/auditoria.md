# Auditoría — Sistema de Supervisión de Dependencias Administrativas

**Fecha de la auditoría:** viernes 6 de noviembre de 2026 (00:22 hrs — faltan ~23.5 horas para la entrega de las 23:59).
**Metodología:** lectura directa del código fuente (sin modificar nada), con 5 revisiones paralelas cubriendo: (1) planificación/asignación, (2) ejecución de campo, (3) resultados/acciones/informe/matriz + documentos, (4) aspectos transversales (SQL, POO, errores, autoría, estados), (5) reportes + entregables del proyecto. Todo hallazgo está citado con archivo:línea y, cuando aplica, el fragmento de código real.

**Resumen de una línea:** el flujo de estados **PROGRAMADO → SEGUIMIENTO se puede completar de principio a fin sin bloqueos indebidos** (ningún paso se salta ni se ejecuta desde un estado incorrecto), pero **ninguno de los 5 documentos requeridos (Anexos I–IV + Matriz) se genera realmente como archivo**, no existe botón "Visualizar documento" en ninguna pantalla, y 3 de los 5 pasos de correo del flujo (creación, aprobación, resultados, informe) no envían ningún correo o envían solo a una fracción de los destinatarios requeridos.

---

## 1. Tabla por ítem

### 1.1 Roles y control de acceso

| Ítem | Estado | Archivo y clase | Qué falta |
|---|---|---|---|
| Mapa de permisos por perfil (6 roles) | **Completo** | `util/ControlAcceso.java:28-44` | Coincide exactamente con la tabla de roles pedida. |
| Menú oculta lo no permitido | **Completo** | `view/frmPrincipal.java:34-51` (`aplicarControlAcceso`) | Oculta ítems y el menú padre si queda vacío. |
| Validación de rol **dentro** de frmAsignacionSupervision | **Falta** | `view/frmAsignacionSupervision.java`, `controller/AsignacionSupervisionController.java` | Ningún uso de `ControlAcceso`/`Sesion` para restringir perfil; cualquier sesión activa puede crear/editar asignaciones si logra abrir la pantalla. |
| Validación de rol dentro de frmBandejaPlanificacion ("solo las suyas") | **Completo** | `view/frmBandejaPlanificacion.java:136-141` | Filtra por `cui_supervisor` real desde `Sesion`, no solo visual. |
| Validación de rol dentro de frmAcciones ("solo su oficina") | **Completo** | `view/frmAcciones.java:166-172` | Filtra por `unidad_rrhh` real desde `Sesion`. |
| Botones de frmBandejaPlanificacion combinan estado+rol | **Parcial** | `view/frmBandejaPlanificacion.java:213-233` | Solo `btnRecepcion` valida estado+rol; `btnEditar`/`btnAprobar`/`btnReprogramar` solo validan estado, no el perfil del usuario en sesión. |

### 1.2 Menús de FrmPrincipal

| Ítem | Estado | Archivo y clase | Qué falta |
|---|---|---|---|
| Administración, Supervisión, Gestión, Reportes | **Completo** (estructura real documentada) | `view/frmPrincipal.java:106-189`, `.form` | La estructura real ya incluye "Acciones" en Administración y "Matriz de seguimiento" en Gestión (agregado en integración previa) — se reporta como hecho, no como desviación. |
| Barra con usuario y rol | **Completo** | `view/frmPrincipal.java:23-32` | — |

### 1.3 Flujo de estados (pasos 1–11)

| Paso | Ítem | Estado | Archivo y clase | Qué falta |
|---|---|---|---|---|
| 1 | Crear asignación → PROGRAMADO | **Completo** | `dao/SupervisionDAO.java:58` | — |
| 1 | Correo de creación de planificación | **Falta** | `controller/AsignacionSupervisionController.java` (sin import de `NotificacionService`) | No se envía ningún correo al crear. |
| 2 | Editar solo en PROGRAMADO | **Parcial** | `dao/SupervisionDAO.java:353-392` (`actualizar`) | El `UPDATE` no incluye `AND estado='PROGRAMADO'`; el guard solo vive en la UI (`frmBandejaPlanificacion.java:227`, botón deshabilitado). |
| 3 | Aprobar con firma: PROGRAMADO→APROBADO | **Completo** | `controller/BandejaPlanificacionController.java:49-55` | — |
| 3 | Generar documento de Nombramiento (Anexo I) | **Falta** | — (no existe en ningún archivo del proyecto) | `aprobar()` no genera ningún archivo; `abrirDocumento()` solo busca una ruta que debería existir ya. |
| 3 | Correo en este paso | **Falta** | `controller/BandejaPlanificacionController.java` | Sin llamada a `NotificacionService`. |
| 4 | Reprogramar/Anular con motivo, observaciones, adjunto opcional y correo | **Completo** | `view/dlgReprogramarAnular.java:50-53`, `controller/ReprogramarAnularController.java:74-91` | Lista de motivos idéntica a la pedida; correo real a JEFE_SUPERVISION+DIRECTOR_DCI+supervisor. |
| 5 | Recepción: historial + correo, sin cambiar estado | **Parcial** | `dao/SupervisionDAO.java:398-423` (`registrarRecepcion`) | No usa el helper `cambiarEstadoDelSupervisorAsignado` ni ata `responsable_cui` al `cui_supervisor` real de la fila (mitigado por el filtro de UI, no por el DAO). Correo real va a JEFE_SUPERVISION/DIRECTOR_DCI/supervisor — "Profesional DCI" del requisito no tiene perfil equivalente en el esquema. |
| 6 | Iniciar: APROBADO→INICIADO, solo supervisor asignado | **Completo** | `view/frmBandejaEjecucion.java:343-349`, `dao/SupervisionDAO.java:183-197` | Doble control (filtro de bandeja + `AND cui_supervisor=?` en el UPDATE). |
| 7 | Finalizar visita: INICIADO→EJECUTADO, 4 PDF obligatorios | **Parcial** | `view/frmFinalizarVisita.java:139-206` | Bloqueo real de los 4 PDF y transacción única correctos; pero copia a `documentos/supervision_{id}/` (ruta propia) en vez de `ArchivoService`/`expedientes/{id}/`, y no revalida supervisor asignado al finalizar. |
| 8 | Resultados: Guardar→CREADO, Generar documento (Anexo II)→RESULTADOS, resultado→ENVIADO | **Parcial** | `view/frmResultadosSupervision.java:700-765`, `dao/clsResultados.java` | Cambio de estado y `estado_resultado='ENVIADO'` sí ocurren; **no se genera ningún archivo** y **no se envía ningún correo** (0 de 5 destinatarios). |
| 9 | Acciones: Guardar→CREADO, Generar documento (Anexo III)→ACCIONES | **Parcial** | `view/frmAcciones.java:309-345` | Cambio de estado correcto; **no se genera archivo**; correo solo llega al Supervisor (1 de 5 destinatarios requeridos). |
| 10 | Informe: Guardar→CREADO, Generar documento (Anexo IV)→INFORME | **Parcial** | `view/frmInformeFinal.java:582-633` | Cambio de estado y `estado_informe='ENVIADO'` correctos; **no se genera archivo** y **no se envía ningún correo** (0 de 5). |
| 11 | Matriz: generar documento→SEGUIMIENTO | **Parcial** | `view/frmMatrizSeguimiento.java:125-153` | Cambio de estado correcto; **no se genera ningún documento de matriz** (solo exportación CSV manual aparte, bajo demanda); correctamente sin correo (no se requiere). |
| — | No se puede saltar un paso / ejecutar desde estado incorrecto | **Completo** | `dao/SupervisionDAO.java:183-197` (lock optimista `WHERE estado=?` en cada transición) | Confirmado para los 8 call sites de cambio de estado del proyecto. |

### 1.4 Funcionalidad por pantalla

| Pantalla | Estado | Archivo y clase | Qué falta |
|---|---|---|---|
| FrmLogin | **Completo** | `view/frmLogin.java`, `controller/LoginController.java:32-37` | Contraseña en texto plano (limitación conocida y aceptada). |
| FrmPrincipal | **Completo** | `view/frmPrincipal.java` | — |
| FrmAsignacionSupervision — combo supervisor, autocompletado, región 1-30 | **Completo** | `view/frmAsignacionSupervision.java:106-178`, `controller/AsignacionSupervisionController.java:28-34` | — |
| FrmAsignacionSupervision — listas ordenadas A-Z | **Parcial** | `util/Catalogos.java:39-45` | Combo de supervisor sí ordenado (`ORDER BY`); catálogos de región/departamento/municipio/oficina están hardcodeados sin orden alfabético. |
| FrmAsignacionSupervision — 4 fechas con calendario | **Falta** | `view/frmAsignacionSupervision.java:92-102` | Usa `JSpinner`/`SpinnerDateModel` (texto editable), no `JDateChooser` — que el propio proyecto ya usa en `frmResultadosSupervision.java:448-449`. |
| FrmAsignacionSupervision — tipo CAMPO/ESPECÍFICA habilita combos | **Completo** | `view/frmAsignacionSupervision.java:185-202` | — |
| FrmAsignacionSupervision — viáticos/vehículo SI/NO | **Completo** | `view/frmAsignacionSupervision.java:118-121` | — |
| FrmAsignacionSupervision — búsqueda/vínculo de nombramiento REPROGRAMADO | **Falta** | — (0 coincidencias en todo el archivo) | No existe esta función en absoluto. |
| FrmAsignacionSupervision — observaciones, 5 días hábiles, guarda PROGRAMADO | **Completo** | `view/frmAsignacionSupervision.java:307-309`, `controller/AsignacionSupervisionController.java:91-129` | Cuenta días hábiles reales (lunes-viernes), correcto. |
| FrmBandejaPlanificacion — 8 filtros, columnas, "solo las suyas" | **Completo** | `view/frmBandejaPlanificacion.java:373-376` | — |
| FrmBandejaPlanificacion — botones según estado y rol | **Parcial** | `view/frmBandejaPlanificacion.java:213-233` | Ver tabla 1.1. |
| DlgReprogramarAnular | **Completo** | `view/dlgReprogramarAnular.java`, `controller/ReprogramarAnularController.java` | — |
| FrmBandejaEjecucion | **Completo** (con matiz) | `view/frmBandejaEjecucion.java:144-153,343-349` | Sin filtros manipulables por el usuario (filtro fijo por CUI+estado). |
| DlgFinalizarVisita (frmFinalizarVisita) — 4 PDF, registro en Documentos_Supervision | **Parcial** | `view/frmFinalizarVisita.java:139-206` | Ver 1.3 paso 7 — ruta propia en vez de `ArchivoService`. |
| FrmResultadosSupervision — encabezado, fechas, enlace, hallazgos, Guardar/Editar | **Parcial** | `view/frmResultadosSupervision.java:97-151,448-449` | Autocompleta supervisor/lugar; faltan departamento/municipio/nombre del Registrador Civil como campos separados; "Generar enlace" solo abre Google Meet (no genera uno real). |
| DlgHallazgo (frmHallazgo) — sigla D/OM/F, política, evidencia | **Completo** | `view/frmHallazgo.java:27-47` | Mapeo D→AC, OM→AP, F→vacío exacto. |
| FrmAcciones — encabezado autocompletado | **Falta** | `view/frmAcciones.java:76-126` | Solo un `JLabel` de título fijo; no hay datos autocompletados de oficina/supervisor aparte de las tablas. |
| FrmAcciones — acciones preventivas/correctivas, Guardar/Editar/Generar | **Completo** | `view/frmAcciones.java:289-345` | Lógica de SOLVENTADA/ATENDIDA y bloqueo sin causa-raíz correctos. |
| DlgCausaRaiz — deficiencia automática, causa-raíz, contramedidas, responsable | **Completo** | `view/dlgCausaRaiz.java` | — |
| DlgCausaRaiz — 5 filas ¿Por qué? (mínimo 5) | **Parcial** | `view/dlgCausaRaiz.java:38-39` | Son exactamente 5 filas fijas, sin posibilidad de ampliar (la especificación dice "mínimo 5"). |
| DlgCausaRaiz — fecha compromiso con calendario | **Falta** | `view/dlgCausaRaiz.java:172-181` | `JTextField` con parseo manual `dd/MM/yyyy`, no un widget de calendario. |
| FrmInformeFinal — supervisor/lugar, alcance, objetivos, documentos examinados | **Parcial** | `view/frmInformeFinal.java:50-128` | Autocompleta supervisor y lugar; no autocompleta "nombre del supervisado" como campo propio. |
| FrmInformeFinal — tabla de hallazgos con checkbox | **Parcial** | `view/frmInformeFinal.java:130-158` | Checkbox real de inclusión sí existe, pero la tabla solo tiene 3 columnas (Incluir/Sigla/Descripción); faltan política y acciones efectuadas. |
| FrmMatrizSeguimiento — columnas de la tabla de supervisiones | **Falta** (incompletas) | `view/frmMatrizSeguimiento.java:40` | Solo `{"ID","Oficina","Fecha informe","Estado"}`; faltan región, departamento, municipio, direcciones y supervisor como columnas explícitas. |
| FrmReportes — filtros, 10 indicadores, CSV, imprimir | **Completo** | `view/frmReportes.java`, `dao/ReporteDAO.java:29-90` | Los 10 indicadores tienen consulta SQL real; CSV e impresión (`JTable.print()`) funcionan de verdad. |

### 1.5 Documentos generados

| Documento | Estado | Archivo y clase | Qué falta |
|---|---|---|---|
| Anexo I (Nombramiento) | **Falta** | `controller/BandejaPlanificacionController.java` | No se genera ningún archivo al aprobar. |
| Anexo II (Resultados) | **Falta** | `view/frmResultadosSupervision.java:700-765` | "Generar documento" solo firma (password) + cambia estado; no escribe archivo. |
| Anexo III (Acción preventiva/correctiva) | **Falta** | `view/frmAcciones.java:309-345` | Igual que Anexo II. |
| Anexo IV (Informe de supervisión) | **Falta** | `view/frmInformeFinal.java:582-633` | Igual que Anexo II. |
| Matriz de seguimiento (documento) | **Falta** | `view/frmMatrizSeguimiento.java:125-153` | Solo cambia estado y llena una tabla en memoria; el CSV es una exportación manual aparte, no el "documento de matriz" en sí. |
| Botón "Visualizar documento" (cualquiera de los 5) | **Falta** | — (sin coincidencias de "Visualizar" en ningún `.java`/`.form` de `view/`) | No existe en ninguna de las 4 pantallas de generación (Resultados, Acciones, Informe, Matriz). |

### 1.6 Reglas de negocio

| Regla | Estado | Archivo y clase | Qué falta |
|---|---|---|---|
| Máximo 5 días hábiles (L-V) | **Completo** | `controller/AsignacionSupervisionController.java:117-129` | — |
| Editar/Reprogramar/Anular solo en PROGRAMADO | **Parcial** | `dao/SupervisionDAO.java:353-392` vs. `:431-470` | Reprogramar/Anular sí lo valida en el DAO (`actualizarEstadoYHistorial` con lock optimista); Editar NO lo valida en el DAO, solo en la UI. |
| Resultados/Acciones/Informe editables solo en CREADO | **Completo** | `view/frmResultadosSupervision.java:395-405`, lógica análoga en Acciones/Informe | — |
| Finalizar visita exige 4 PDF | **Completo** | `view/frmFinalizarVisita.java:139-147` | Bloqueo real, no solo visual. |
| D→AC, OM→AP, F sin acción | **Completo** | `view/frmHallazgo.java:27-47` | — |
| "Por efectuar" NO→solventada, SI→exige causa-raíz | **Completo** | `view/frmAcciones.java:289-300` | — |
| tipo_nombramiento 'CAMPO'/'ESPECIFICA' sin tilde | **Completo** | `query/supervisor_dependenciasDB.sql:115`, `view/frmAsignacionSupervision.java:215` | La UI normaliza la tilde de la etiqueta antes de persistir. |
| estado_resultado/estado_informe solo CREADO/ENVIADO | **Completo** | `dao/clsResultados.java`, `dao/clsInformeFinal.java` | Sin CHECK constraint en el esquema (solo disciplina de código) — ver sección 4. |

### 1.7 Transversales

| Ítem | Estado | Archivo y clase | Qué falta |
|---|---|---|---|
| Correos (fallo no deshace el cambio de estado) | **Completo** (en los correos que sí existen) | `controller/ReprogramarAnularController.java:65` (retorna booleano aparte del guardado ya confirmado) | Diseño correcto donde se implementó; pero varios correos del flujo ni siquiera están implementados (ver 1.3). |
| Cambios de estado: una sola función de DAO, en transacción, con historial | **Parcial** | `dao/SupervisionDAO.java` vs. `dao/clsSupervision.java:28-33` | `clsSupervision.java` duplica un `UPDATE Supervisiones SET estado` fuera de `SupervisionDAO` — código huérfano (sin callers), pero viola la regla. |
| Todas las consultas con PreparedStatement | **Completo** | todo `src/` | Sin ningún `Statement`/concatenación insegura encontrada. |
| Encabezado de autor en cada .java | **Parcial — 49/52** | ver sección final | Faltan en `dao/clsSupervision.java`, `view/frmLogin.java`, `view/frmPrincipal.java`. |
| Cada método comentado | **Parcial** | `dao/`,`servicios/`,`util/` bien; `view/` generado por NetBeans, mal | Ver sección 4 del bloque transversal. |
| POO: herencia de dominio | **Falta** | todo `src/model,dao,controller,servicios` | Solo heredan de `Exception` (no cuenta) y de clases Swing (no cuenta). |
| POO: clases abstractas/interfaces propias | **Falta** | todo `src/` | Ninguna encontrada. |
| POO: composición/agregación | **Completo** (parcial en algunos modelos) | `model/HallazgoAccion.java:25` (`CausaRaiz`), `util/Sesion.java:17` (`Usuario`) | `Supervision`/`SupervisionResumen` usan CUI/nombre como `String` plano en vez de componer un objeto `Usuario`. |
| POO: colecciones | **Completo** | `util/ControlAcceso.java:25`, `dao/SupervisionDAO.java:27,275` | — |
| Manejo de errores: sin pantallas que se cierren por excepción no controlada | **Parcial** | — | No existe `Thread.setDefaultUncaughtExceptionHandler`; riesgo latente, no confirmado como falla real observada. |
| Manejo de errores: sin mensajes con trazas técnicas | **Parcial** | múltiples `view/*.java` (ver sección 4) | Varios `catch` muestran `e.getMessage()`/`e.getCause().getMessage()` crudo en `JOptionPane`. |

### 1.8 Entregables del proyecto

| Entregable | Estado | Ruta | Qué falta |
|---|---|---|---|
| Manual técnico | **Falta** | — | No encontrado en ninguna parte del repositorio. |
| Manual de usuario | **Falta** | — | No encontrado. |
| Diagrama de clases + diccionario de clases | **Falta** | — | No encontrado (ni imagen ni documento). |
| Código fuente | **Completo** | `src/`, proyecto NetBeans completo | — |
| Script de base de datos | **Completo** | `src/query/supervisor_dependenciasDB.sql` | Script único; la migración de `id_informe` ya quedó integrada como ALTER TABLE al final de la sección de creación de tablas. |
| Modelo entidad-relación | **Falta** | — | No encontrado (solo existen los `CREATE TABLE`/FK dentro del script SQL, no un diagrama ER). |
| Certificado de licenciamiento | **Falta** | — | No encontrado. |

---

## 2. Lista priorizada de lo que falta

### A. ¿Algo rompe el flujo mecánico PROGRAMADO→SEGUIMIENTO?
**No.** Los 8 cambios de estado del flujo usan lock optimista (`WHERE estado = ?`) y ninguno se puede ejecutar desde un estado incorrecto ni saltarse un paso. Una supervisión puede recorrer todo el ciclo de principio a fin sin quedar atascada. Esto es una buena noticia para la defensa: el núcleo del sistema (máquina de estados) funciona correctamente.

### B. Requisitos explícitos de la especificación, incompletos o faltantes (orden de impacto)

1. **Ningún "Generar documento" produce un archivo real** (Anexo I, II, III, IV, Matriz) — es el gap más visible y más fácil de detectar en una revisión o defensa oral.
2. **No existe botón "Visualizar documento"** en ninguna de las 4 pantallas que lo requieren.
3. **Correos faltantes del flujo**: creación (paso 1), aprobación (paso 3), resultados (paso 8, 0/5), informe (paso 10, 0/5); acciones (paso 9) solo a 1 de 5 destinatarios.
4. **frmAsignacionSupervision sin validación de rol interna** — cualquier sesión activa puede crear/editar una asignación si abre la pantalla, sin verificar perfil.
5. **Fechas sin calendario real** en frmAsignacionSupervision (usa `JSpinner`) y dlgCausaRaiz (usa `JTextField` con parseo manual), pese a que la librería `JDateChooser` ya está en el proyecto y se usa en otra pantalla.
6. **Falta búsqueda/vinculación de nombramiento REPROGRAMADO** en frmAsignacionSupervision — no existe en absoluto.
7. **Editar y Recepción no reafirman sus reglas a nivel de datos** (`SupervisionDAO.actualizar()` no exige `estado='PROGRAMADO'`; `registrarRecepcion()` no ata el responsable al supervisor asignado) — dependen solo de la UI.
8. **frmAcciones sin encabezado autocompletado** (oficina/supervisor).
9. **frmResultadosSupervision**: faltan campos de departamento/municipio/Registrador Civil; "Generar enlace" no genera uno automático, solo abre Google Meet.
10. **frmInformeFinal**: tabla de hallazgos con checkbox solo tiene 3 de las 4 columnas pedidas (falta política y acciones efectuadas); falta "nombre del supervisado" autocompletado.
11. **frmMatrizSeguimiento**: tabla de supervisiones con columnas incompletas (falta región/departamento/municipio/direcciones/supervisor).
12. **dlgCausaRaiz**: 5 filas fijas sin poder ampliar (la especificación dice "mínimo 5").
13. **Catálogos de lugar sin orden alfabético** (región/departamento/municipio/oficina).
14. **`clsSupervision.java`**: código huérfano que viola la regla de función única de cambio de estado (sin invocadores actuales, pero riesgo si se reactiva).
15. **3 archivos sin encabezado de autor** (`clsSupervision.java`, `frmLogin.java`, `frmPrincipal.java`) — incluye la clase de arranque del sistema.
16. **Sin manejador global de excepciones no capturadas** — riesgo latente de cierre abrupto de pantalla.
17. **Mensajes de error con texto técnico crudo** expuesto al usuario en varias pantallas.
18. **5 de 7 entregables del proyecto no existen**: manual técnico, manual de usuario, diagrama de clases + diccionario, modelo entidad-relación, certificado de licenciamiento.
19. **Sin herencia/clases abstractas/interfaces de dominio** — débil para el rubro de evaluación de POO (ver sección 5).

### C. Opcional (no reportar como falta)
- Exportación a PDF/Excel en Reportes — opcional por decisión del equipo; CSV e impresión ya funcionan.
- Formato de la Matriz de Seguimiento — libre por decisión del equipo; ya está implementado con columnas razonables.

---

## 3. Inconsistencias entre pantallas

1. **Dos implementaciones distintas de "firma simulada"** conviviendo en el proyecto: `servicios/FirmaService.java` pide reingresar la contraseña del usuario en sesión (usado en Resultados/Acciones/Informe), mientras que `frmBandejaPlanificacion.java:569-575` usa un simple `JOptionPane` Sí/No (usado en Aprobar). El comentario del propio código en `frmBandejaPlanificacion.java:569` dice *"No existe un FirmaService en el proyecto"*, lo cual ya no es cierto — es un comentario desactualizado que puede confundir en la defensa.
2. **`tipo_documento` sin catálogo compartido**: `frmFinalizarVisita.java` usa `"Financiera"`, `"Servicio"`, `"Oficina"`, `"Administrativa"` (capitalizado), mientras que `ReprogramarAnularController.java:50-51` usa `"OFICIO DE REPROGRAMACION"` / `"JUSTIFICACION DE ANULACION"` (mayúsculas, sin tildes). No hay constantes/enum compartido; una búsqueda exacta por tipo en una pantalla podría no encontrar documentos guardados con otra convención.
3. **Dos sistemas de almacenamiento de archivos conviviendo**: `frmFinalizarVisita.java` copia a su propia ruta `documentos/supervision_{id}/` con nombres UUID, mientras que `frmAcciones.java` y `ReprogramarAnularController.java` usan `util/ArchivoService.java` → `expedientes/{id}/`. Son dos mecanismos distintos para el mismo propósito (adjuntar evidencia a una supervisión).
4. **El requisito de correo de Recepción menciona "Profesional DCI"**, un perfil que no existe en el CHECK constraint de `Usuarios` (los 6 reales son ADMINISTRADOR, DIRECTOR_DCI, JEFE_SUPERVISION, SUPERVISOR_DCI, OPERADOR, JEFE_OFICINA). El código implementado lo aproximó a `JEFE_SUPERVISION` — razonable, pero es una discrepancia entre el texto del requisito y el modelo de datos real que conviene aclarar en la defensa.
5. **`clsSupervision.java` duplica, con un nombre casi idéntico (`cambiarEstado`), la función autorizada de `SupervisionDAO`** — aunque no tiene invocadores actuales, puede confundir a quien lea el código por primera vez (¿cuál es la función "oficial"?).
6. **FrmBandejaEjecucion no tiene filtros** manipulables por el usuario, mientras que la pantalla análoga FrmBandejaPlanificacion tiene 8 — inconsistencia de funcionalidad entre dos bandejas de listados similares.
7. **Dentro de FrmBandejaPlanificacion**, el botón Recepción combina estado+rol para habilitarse, pero Editar/Aprobar/Reprogramar solo consideran el estado — un criterio de habilitación inconsistente dentro de la misma pantalla.

---

## 4. Errores probables

1. **`dao/clsSupervision.java:28-33`** ejecuta `UPDATE dbo.Supervisiones SET estado = ...` por fuera de la función única autorizada (`SupervisionDAO`). No tiene invocadores actuales (confirmado por grep), pero si alguien lo conecta en el futuro, rompería la regla de "una sola función con historial en transacción" y probablemente dejaría de registrar el historial correctamente en algunos casos (duplica también el INSERT a `Historial_Supervision` de forma independiente).
2. **`estado_resultado` y `estado_informe` no tienen CHECK constraint** en `supervisor_dependenciasDB.sql` (a diferencia de `Supervisiones.estado`, que sí lo tiene). Actualmente el código solo usa `'CREADO'`/`'ENVIADO'` de forma consistente, pero no hay red de seguridad a nivel de base de datos si un futuro desarrollador escribe un valor distinto.
3. **`Documentos_Supervision.tipo_documento` es `NVARCHAR(100) NULL`** de texto libre sin catálogo — permite (y de hecho ya produjo) los valores inconsistentes entre pantallas descritos en la sección 3.
4. **`frmFinalizarVisita.java` no revalida que el usuario en sesión sea el supervisor asignado** al finalizar la visita (a diferencia de "Iniciar", que sí lo hace vía `cambiarEstadoDelSupervisorAsignado`). Mitigado en la práctica porque la bandeja ya filtra por CUI, pero el DAO/controlador no lo impone.
5. **`SupervisionDAO.actualizar()`** (editar asignación) no incluye `AND estado = 'PROGRAMADO'` en su `UPDATE` — un llamado directo al controlador (sin pasar por el botón deshabilitado de la UI) permitiría editar una asignación en cualquier estado.
6. **`SupervisionDAO.registrarRecepcion()`** no ata el `responsable_cui` insertado en el historial al `cui_supervisor` real de la fila — el `INSERT` es fijo y no verifica asignación a nivel de datos.
7. **`ArchivoService.copiarAExpediente()` usa `StandardCopyOption.REPLACE_EXISTING`** sin generar nombre único — dos adjuntos distintos con el mismo nombre de archivo (común, p. ej. "evidencia.pdf") se **sobrescriben silenciosamente** sin aviso al usuario, con riesgo real de pérdida de evidencia.
8. **`frmFinalizarVisita.java` guarda `destino.toAbsolutePath().toString()`** (ruta absoluta de la máquina) en `Documentos_Supervision.ruta_archivo` en vez de una ruta relativa — si el proyecto se ejecuta desde otra máquina o carpeta, esas rutas guardadas quedan rotas.
9. Varias pantallas muestran `e.getMessage()` o `e.getCause().getMessage()` directo en `JOptionPane` (ejemplos: `frmResultadosSupervision.java:758-762`, `frmMatrizSeguimiento.java:199-201`, `frmAcciones.java:354-357`, `frmBandejaEjecucion.java:358-362`) — puede exponer texto técnico de SQL Server al usuario final.

---

## 5. Riesgos para la calificación (15 minutos, preguntas sobre el código)

1. **"Muéstrame el documento PDF que generó el sistema al aprobar / al generar resultados."** No hay nada que mostrar — es probablemente la pregunta más comprometedora de toda la defensa, dado que es un requisito central y muy visible (5 documentos distintos, ninguno se genera).
2. **"¿Quién recibe el correo cuando se genera el Informe Final?"** La respuesta real es "nadie" — contradice directamente el flujo documentado en la especificación.
3. **Dos implementaciones de "firma simulada" distintas en el mismo sistema** (contraseña vs. Sí/No) — si preguntan "¿cómo funciona la firma?", la respuesta no es única, y el comentario desactualizado en el código ("no existe un FirmaService") puede delatar que no se revisó bien el proyecto antes de la entrega.
4. **`clsSupervision.java` como código muerto que duplica lógica central** — si el evaluador lo encuentra y pregunta para qué sirve, la respuesta honesta es "es código viejo que no se eliminó", lo cual resta puntos de limpieza y puede generar dudas sobre qué código es el "real".
5. **Sin herencia, clases abstractas ni interfaces de dominio** — si el rubro de POO pregunta específicamente por polimorfismo/abstracción, hay que tener lista una respuesta honesta y una propuesta concreta de mejora (ver más abajo), en vez de improvisar.
6. **`frmAsignacionSupervision` sin validación de rol** — si preguntan "¿qué pasa si un OPERADOR abre esta pantalla directamente?", la respuesta es que el sistema se lo permite; solo el menú lo oculta.
7. **Dos sistemas de archivos conviviendo** (`ArchivoService`/`expedientes/` vs. ruta propia de `frmFinalizarVisita`) — fácil de notar revisando el código, y genera la pregunta obvia de "¿por qué no es uno solo?"
8. **3 archivos sin encabezado de autor, incluyendo la clase de arranque (`frmLogin.java`) y `frmPrincipal.java`** — es de los hallazgos más fáciles de detectar con un vistazo superficial al código, y da la impresión de una revisión final incompleta.

**Sugerencia para el rubro de POO** (si preguntan "¿dónde encajaría herencia/abstracción?"):
- `model/Usuario.java` es hoy una única clase concreta con un campo `String perfil`. Se podría justificar una jerarquía `Usuario` (clase base) con subclases por perfil (`UsuarioSupervisor`, `UsuarioJefeOficina`, etc.) si cada perfil tuviera comportamiento distinto más allá de un filtro de datos — aunque dado que el comportamiento actual del sistema ya se resuelve bien con `ControlAcceso` + un campo `perfil`, la respuesta honesta en la defensa puede ser "se evaluó y se prefirió composición simple sobre herencia porque el comportamiento por rol es principalmente de visibilidad, no de lógica distinta" — es una decisión de diseño defendible, no necesariamente un error.
- Una interfaz común `GeneradorDeDocumento` (con un método `generar(idSupervision): String rutaArchivo`) implementada por una clase por cada Anexo sería el lugar natural para introducir polimorfismo **si** se llega a implementar la generación real de documentos — of hecho, sería la oportunidad perfecta para cerrar dos gaps a la vez (POO + generación de documentos) si el tiempo alcanza.

---

## 6. Estimación (quedan ~23.5 horas hasta las 23:59 de hoy)

### Cabe hoy (alto impacto, bajo-medio esfuerzo)
- **Agregar encabezado de autor a los 3 archivos restantes** (`clsSupervision.java`, `frmLogin.java`, `frmPrincipal.java`) — minutos.
- **Completar los correos faltantes** (creación, aprobación, resultados, informe, y completar acciones a los 5 destinatarios) — patrón ya existente y probado en `ReprogramarAnularController`/`BandejaPlanificacionController`, es trabajo mecánico de adaptar el mismo patrón 4 veces.
- **Eliminar o aislar `clsSupervision.java`** (sin invocadores, bajo riesgo) para cerrar la violación de la regla de función única.
- **Agregar encabezado autocompletado mínimo a `frmAcciones`** (un par de `JLabel` con oficina/supervisor) — bajo esfuerzo.
- **Cambiar `dlgCausaRaiz` y `frmAsignacionSupervision` a `JDateChooser`** en vez de `JTextField`/`JSpinner` — la librería ya está en el proyecto y usada en otra pantalla, es un cambio de componente, no de lógica.
- Si alcanza el tiempo: **generación mínima de documento como HTML simple** (no PDF con librería nueva, demasiado riesgo a esta hora) para los 4 Anexos + Matriz, usando `String.format` + `Files.write` con los datos ya disponibles en cada pantalla, más un botón "Visualizar" que abra el HTML con `Desktop.getDesktop().browse(...)`. Esto cerraría de un solo golpe los dos gaps más visibles de la auditoría (documentos + visualizar) sin necesitar una librería PDF nueva el mismo día de la entrega.

### Conviene dejar fuera de hoy (alto esfuerzo o alto riesgo si se apura)
- **Generación de PDF real con una librería tipo iText/PDFBox** — agregar una dependencia nueva y su lógica de maquetado el mismo día de la entrega es un riesgo de romper la compilación bajo presión de tiempo; el HTML simple sugerido arriba es una alternativa mucho más segura para hoy.
- **Manual técnico y manual de usuario completos** — son documentos de varias páginas cada uno; si el tiempo aprieta, priorizar una versión mínima de 1-2 páginas de cada uno antes que no entregar nada, pero no intentar hacerlos exhaustivos hoy.
- **Diagrama de clases con diccionario completo y modelo entidad-relación** — mismo criterio: una versión simple (incluso un diagrama a mano/herramienta rápida tipo draw.io con las clases principales) es mejor que nada, pero un diagrama exhaustivo de las ~52 clases no es realista hoy.
- **Certificado de licenciamiento** — depende de qué licencia haya usado el equipo (librerías jakarta.mail, jcalendar, mssql-jdbc); esto es más una tarea de redactar/recopilar que de programar, se puede hacer en paralelo sin tocar código.
- **Reestructurar `frmAsignacionSupervision` para validar rol internamente, o reforzar las reglas de Editar/Recepción a nivel de DAO** — son cambios correctos pero tocan rutas ya probadas del flujo central; si se hacen hoy, deben probarse con cuidado para no introducir una regresión en el flujo que sí funciona.
- **Introducir herencia/interfaces de dominio** (sugerencia de la sección 5) — es una refactorización de diseño, no un bug; mejor explicarlo como decisión consciente en la defensa que improvisarlo horas antes de entregar.

---

## Conteo final de archivos sin encabezado de autor

**3 de 52 archivos `.java`** no tienen el encabezado `Autor:`/`Carné:`:

1. `src/dao/clsSupervision.java` — sin ningún comentario al inicio.
2. `src/view/frmLogin.java` — tiene la plantilla por defecto de NetBeans (`@author by_estuardo`), no el formato del proyecto.
3. `src/view/frmPrincipal.java` — mismo caso que `frmLogin.java`.
