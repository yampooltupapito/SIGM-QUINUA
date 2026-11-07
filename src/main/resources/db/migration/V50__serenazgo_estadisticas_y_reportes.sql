-- ============================================================================
-- SIGM-QUINUA: Migración V50 — Estadísticas, Reportes y Catálogo de Indicadores
-- Módulo 3: Seguridad Ciudadana y Serenazgo (Prompt 49 de 150)
-- ============================================================================

-- 1. Secuencia para correlativo de reportes estadísticos
CREATE SEQUENCE IF NOT EXISTS seq_serenazgo_reportes_estadisticos
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 2. Tabla de Catálogo Técnico de Indicadores de Seguridad Ciudadana
CREATE TABLE IF NOT EXISTS serenazgo_catalogo_indicadores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    modulo_origen VARCHAR(50) NOT NULL,
    definicion TEXT NOT NULL,
    fuente_datos VARCHAR(100) NOT NULL,
    formula_calculo TEXT NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL,
    frecuencia_calculo VARCHAR(50) NOT NULL DEFAULT 'CONSULTA_PERIODICA',
    limitaciones_interpretacion TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_cat_ind_mod ON serenazgo_catalogo_indicadores(modulo_origen);
CREATE INDEX IF NOT EXISTS idx_serenazgo_cat_ind_cod ON serenazgo_catalogo_indicadores(codigo);

-- 3. Tabla de Historial de Reportes Estadísticos Generados
CREATE TABLE IF NOT EXISTS serenazgo_reportes_estadisticos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    tipo_reporte VARCHAR(60) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    formato VARCHAR(20) NOT NULL,
    parametros_json TEXT,
    periodo_tipo VARCHAR(40) NOT NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE,
    fecha_fin TIMESTAMP WITH TIME ZONE,
    sector_id UUID REFERENCES serenazgo_sectores(id) ON DELETE SET NULL,
    zona_id UUID REFERENCES serenazgo_zonas(id) ON DELETE SET NULL,
    usuario_generador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    username_generador VARCHAR(100) NOT NULL,
    oficina_id BIGINT REFERENCES oficinas(id) ON DELETE SET NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'COMPLETADO',
    nombre_archivo VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    tamanio_bytes BIGINT NOT NULL DEFAULT 0,
    ruta_archivo VARCHAR(500),
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    error_detalle TEXT,
    fecha_generacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_serenazgo_rep_est_usr ON serenazgo_reportes_estadisticos(usuario_generador_id);
CREATE INDEX IF NOT EXISTS idx_serenazgo_rep_est_fecha ON serenazgo_reportes_estadisticos(fecha_generacion DESC);
CREATE INDEX IF NOT EXISTS idx_serenazgo_rep_est_tipo ON serenazgo_reportes_estadisticos(tipo_reporte);
CREATE INDEX IF NOT EXISTS idx_serenazgo_rep_est_sec ON serenazgo_reportes_estadisticos(sector_id);

-- 4. Inserción de Catálogo Técnico Oficial de Indicadores
INSERT INTO serenazgo_catalogo_indicadores (codigo, nombre, modulo_origen, definicion, fuente_datos, formula_calculo, unidad_medida, limitaciones_interpretacion)
VALUES
    ('IND_INC_TOTAL', 'Incidencias Registradas en Período', 'INCIDENCIAS',
     'Total de eventos o novedades ingresadas al registro municipal de incidencias.',
     'serenazgo_incidencias', 'COUNT(DISTINCT id)', 'Incidencias',
     'Un registro no equivale a delito comprobado judicialmente ni a tasa delictiva oficial.'),

    ('IND_INC_PEND_REV', 'Incidencias Pendientes de Revisión o Despacho', 'INCIDENCIAS',
     'Incidencias registradas que se encuentran en estado inicial sin asignación ni despacho concluido.',
     'serenazgo_incidencias', 'COUNT(id) WHERE estado IN (''REGISTRADA'', ''ASIGNADA'')', 'Incidencias',
     'No implica desatención o negligencia; depende de la disponibilidad del turno de servicio.'),

    ('IND_INC_CERRADAS', 'Incidencias Cerradas con Resultado', 'INCIDENCIAS',
     'Incidencias concluidas con acta, informe o verificación de atención finalizada.',
     'serenazgo_incidencias', 'COUNT(id) WHERE estado IN (''CERRADA'', ''RESUELTA'')', 'Incidencias',
     'Refleja el cierre formal administrativo de la bitácora municipal.'),

    ('IND_INC_DESCARTADAS', 'Incidencias Descartadas Fundamentadas', 'INCIDENCIAS',
     'Alertas o reportes que no ameritaron actuación municipal tras verificación.',
     'serenazgo_incidencias', 'COUNT(id) WHERE estado = ''DESCARTADA''', 'Incidencias',
     'Se presentan por separado para evitar distorsiones estadísticas.'),

    ('IND_ALE_TOTAL', 'Alertas Ciudadanas Recibidas', 'ALERTAS',
     'Comunicaciones preventivas ingresadas por vecinos a través de canales institucionales.',
     'serenazgo_alertas_ciudadanas', 'COUNT(DISTINCT id)', 'Alertas',
     'Los reportes vecinales iniciales no constituyen prueba fehaciente.'),

    ('IND_ALE_PEND', 'Alertas Ciudadanas Pendientes de Evaluación', 'ALERTAS',
     'Alertas vecinales recibidas que aún no cuentan con revisión preliminar.',
     'serenazgo_alertas_ciudadanas', 'COUNT(id) WHERE estado IN (''PENDIENTE'', ''EN_REVISION'')', 'Alertas',
     'Sujeto a la operatividad de la central de comunicaciones.'),

    ('IND_ALE_CONV_INC', 'Alertas Convertidas a Incidencias Operativas', 'ALERTAS',
     'Alertas verificadas positivamente que generaron una incidencia de Serenazgo.',
     'serenazgo_alertas_ciudadanas', 'COUNT(id) WHERE incidencia_id IS NOT NULL', 'Alertas',
     'Mide la efectividad del filtrado y confirmación ciudadana.'),

    ('IND_ATE_T_ASIG', 'Tiempo Medio de Registro a Asignación', 'ATENCIONES',
     'Tiempo promedio transcurrido desde el registro de la incidencia hasta la asignación de recursos.',
     'serenazgo_atenciones', 'AVG(fecha_asignacion - fecha_reporte)', 'Minutos',
     'Calculado exclusivamente sobre atenciones con ambas marcas temporales registradas.'),

    ('IND_ATE_T_DESP', 'Tiempo Medio de Asignación a Despacho', 'ATENCIONES',
     'Tiempo promedio transcurrido entre la asignación y la salida de la unidad o serenos.',
     'serenazgo_atenciones', 'AVG(fecha_despacho - fecha_asignacion)', 'Minutos',
     'Excluye registros con fechas faltantes.'),

    ('IND_ATE_T_ACEP', 'Tiempo Medio de Despacho a Aceptación', 'ATENCIONES',
     'Tiempo promedio transcurrido entre el despacho y la confirmación de recepción en el sector.',
     'serenazgo_atenciones', 'AVG(fecha_aceptacion - fecha_despacho)', 'Minutos',
     'No representa velocidad de traslado ni tiempo de llegada al lugar de los hechos.'),

    ('IND_ATE_DURACION', 'Duración Media de Atención Registrada', 'ATENCIONES',
     'Tiempo promedio transcurrido entre el inicio de la atención y su finalización administrativa.',
     'serenazgo_atenciones', 'AVG(fecha_fin - fecha_inicio)', 'Minutos',
     'Depende de la complejidad de la labor comunitaria y de apoyo al orden público.'),

    ('IND_INT_TOTAL', 'Intervenciones Operativas Registradas', 'INTERVENCIONES',
     'Total de actuaciones preventivas o de auxilio documentadas por el personal de Serenazgo.',
     'serenazgo_intervenciones', 'COUNT(DISTINCT id)', 'Intervenciones',
     'No constituyen intervenciones policiales ni detenciones judiciales.'),

    ('IND_INT_VAL', 'Intervenciones Validadas Administrativamente', 'INTERVENCIONES',
     'Intervenciones cuyo informe u hoja de actuación fue aprobada por el supervisor.',
     'serenazgo_intervenciones', 'COUNT(id) WHERE estado = ''VALIDADA''', 'Intervenciones',
     'Garantiza el control de calidad de las actuaciones del personal.'),

    ('IND_PAT_PROG', 'Patrullajes Programados en el Período', 'PATRULLAJES',
     'Patrullajes planificados en el rol de servicios en sectores y zonas del distrito.',
     'serenazgo_patrullajes', 'COUNT(id)', 'Patrullajes',
     'Corresponde a la programación oficial de los turnos.'),

    ('IND_PAT_EJEC', 'Patrullajes Efectivamente Finalizados', 'PATRULLAJES',
     'Patrullajes completados con informe de fin de recorrido y cierre registrado.',
     'serenazgo_patrullajes', 'COUNT(id) WHERE estado = ''FINALIZADO''', 'Patrullajes',
     'Base para evaluar la ejecución de la presencia territorial preventiva.'),

    ('IND_PAT_CUMP', 'Tasa de Cumplimiento de Patrullaje', 'PATRULLAJES',
     'Porcentaje de patrullajes programados que culminaron su servicio satisfactoriamente.',
     'serenazgo_patrullajes', '(Patrullajes Finalizados / Patrullajes Programados) * 100', 'Porcentaje',
     'Indefinido si no hubo patrullajes programados en el período consultado.'),

    ('IND_PAT_PTS_CTRL', 'Cumplimiento de Puntos de Control Visitados', 'PATRULLAJES',
     'Porcentaje de puntos de control planificados que fueron efectivamente verificados.',
     'serenazgo_patrullajes_puntos_control', '(Puntos Visitados / Puntos Planificados) * 100', 'Porcentaje',
     'No se consideran visitas simuladas ni puntos sin registro de paso.'),

    ('IND_PER_HABIL', 'Personal de Serenazgo Administrativamente Habilitado', 'PERSONAL',
     'Serenos en planilla o régimen activo que cuentan con legajo y aptitud vigente.',
     'serenazgo_personal', 'COUNT(id) WHERE estado = ''ACTIVO''', 'Personas',
     'No equivale a presencia física simultánea en el terreno.'),

    ('IND_UNI_OPER', 'Unidades Móviles en Estado Operativo', 'UNIDADES',
     'Vehículos (camionetas, motocicletas, bicicletas) aptos para servicio municipal.',
     'serenazgo_unidades_moviles', 'COUNT(id) WHERE estado = ''OPERATIVA''', 'Vehículos',
     'Sujeto a disponibilidad mecánica y asignación de turno.'),

    ('IND_DOC_INTEG_FALLA', 'Documentos con Integridad No Coincidente', 'DOCUMENTOS',
     'Archivos digitales cuyo hash SHA-256 no coincide con el digest registrado.',
     'serenazgo_documentos', 'COUNT(id) WHERE estado_integridad = ''NO_COINCIDENTE''', 'Archivos',
     'Genera advertencia técnica de auditoría; requiere verificación pericial interna.'),

    ('IND_SUP_SEGUIM', 'Expedientes de Supervisión Abiertos', 'SUPERVISION',
     'Casos en seguimiento por la coordinación de supervisión y monitoreo.',
     'serenazgo_supervision_seguimientos', 'COUNT(id) WHERE estado NOT IN (''CERRADO'', ''CANCELADO'')', 'Expedientes',
     'Mide la carga de trabajo de control interno municipal.')
ON CONFLICT (codigo) DO NOTHING;

-- 5. Sembrado de Permisos RBAC para Estadísticas y Reportes
INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER', 'Acceder al dashboard de estadísticas y reportes', 'VIEW',
       'Permite consultar la consola analítica, indicadores y tableros estadísticos de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_INCIDENCIAS', 'Consultar estadísticas detalladas de incidencias', 'VIEW',
       'Permite analizar series temporales, tipologías y prioridades de incidencias', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_ALERTAS', 'Consultar estadísticas de alertas ciudadanas', 'VIEW',
       'Permite analizar el volumen y atención de reportes preventivos ciudadanos', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_ATENCIONES', 'Consultar estadísticas y tiempos de atención', 'VIEW',
       'Permite evaluar tiempos de asignación, despacho, aceptación y duración de atenciones', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_INTERVENCIONES', 'Consultar estadísticas de intervenciones', 'VIEW',
       'Permite analizar tipos de intervención y resultados administrativos de serenazgo', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_PATRULLAJES', 'Consultar estadísticas de patrullajes y recorridos', 'VIEW',
       'Permite evaluar cumplimiento de programación y cobertura de puntos de control', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_RECURSOS', 'Consultar estadísticas de personal y unidades móviles', 'VIEW',
       'Permite analizar disponibilidad operativa, turnos y uso de recursos móviles', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_ESTADISTICAS_VER_MAPAS', 'Consultar análisis y mapas territoriales estadísticos', 'VIEW',
       'Permite visualizar distribución geográfica agregada y mapas de calor autorizados', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_REPORTES_GENERAR', 'Generar reportes estadísticos de serenazgo', 'CREATE',
       'Permite solicitar la generación de reportes analíticos e institucionales', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_REPORTES_DESCARGAR', 'Descargar reportes generados en PDF y Excel', 'EXPORT',
       'Permite descargar archivos consolidados generados en el módulo analítico', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO permisos (modulo_id, codigo, nombre, accion, descripcion, estado)
SELECT m.id, 'SERENAZGO_REPORTES_VER_HISTORIAL', 'Consultar historial de reportes estadísticos', 'VIEW',
       'Permite revisar la bitácora y auditoría de reportes generados en el sistema', 'ACTIVO'
FROM modulos_sistema m WHERE m.codigo = 'SEGURIDAD_CIUDADANA'
ON CONFLICT (codigo) DO NOTHING;

-- 6. Asignación de Permisos a Roles Institucionales
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.codigo IN ('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SUPERVISOR_SERENAZGO')
  AND (p.codigo LIKE 'SERENAZGO_ESTADISTICAS_%' OR p.codigo LIKE 'SERENAZGO_REPORTES_%')
ON CONFLICT DO NOTHING;
