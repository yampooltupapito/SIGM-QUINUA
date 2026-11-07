-- ==============================================================================
-- SIGM-QUINUA: SCRIPT MAESTRO DE ENRIQUECIMIENTO Y DIVERSIFICACIÓN DE BASE DE DATOS
-- Municipalidad Distrital de Quinua, Huamanga, Ayacucho, Perú
-- ==============================================================================

BEGIN;

-- ------------------------------------------------------------------------------
-- 1. OFICINAS MUNICIPALES INSTITUCIONALES (Completar organigrama oficial)
-- ------------------------------------------------------------------------------
INSERT INTO oficinas (municipalidad_id, codigo, nombre, sigla, descripcion, nivel_jerarquico, estado, created_at)
VALUES
    (1212, 'OFI-ALC', 'Despacho de Alcaldía', 'ALC', 'Despacho de la máxima autoridad ejecutiva distrital de Quinua', 1, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '120 days'),
    (1212, 'OFI-GM', 'Gerencia Municipal', 'GM', 'Dirección general y coordinación administrativa institucional', 2, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '120 days'),
    (1212, 'OFI-GAT', 'Gerencia de Administración Tributaria y Rentas', 'GAT', 'Fiscalización, acotación y cobranza tributaria distrital', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '100 days'),
    (1212, 'OFI-GSC', 'Subgerencia de Seguridad Ciudadana y Serenazgo', 'GSC', 'Patrullaje preventivo y orden vecinal en Quinua', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '100 days'),
    (1212, 'OFI-MDE', 'Subgerencia de Desarrollo Económico y Mercados', 'MDE', 'Comercio, abastos, ferias artesanales y turismo', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    (1212, 'OFI-OTI', 'Oficina de Tecnologías de la Información y Sistemas', 'OTI', 'Infraestructura tecnológica, redes y gobierno digital', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    (1212, 'OFI-OAJ', 'Oficina de Asesoría Jurídica', 'OAJ', 'Opinión legal, resoluciones de alcaldía y convenios', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    (1212, 'OFI-RRHH', 'Unidad de Gestión de Recursos Humanos', 'RRHH', 'Control de planillas, legajos y régimen laboral', 4, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days')
ON CONFLICT (codigo) DO UPDATE 
SET estado = 'ACTIVO', updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------------------------
-- 2. CARGOS FUNCIONALES INSTITUCIONALES
-- ------------------------------------------------------------------------------
INSERT INTO cargos (codigo, nombre, descripcion, nivel_jerarquico, estado, created_at)
VALUES
    ('CARG-001', 'Alcalde Distrital', 'Titular del pliego municipal de Quinua', 1, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '120 days'),
    ('CARG-002', 'Gerente Municipal', 'Responsable de la dirección administrativa general', 2, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '120 days'),
    ('CARG-003', 'Subgerente de Obras Públicas', 'Supervisión de proyectos de infraestructura', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '100 days'),
    ('CARG-004', 'Registrador de Mesa de Partes', 'Recepción y foliación de expedientes virtuales y físicos', 4, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '100 days'),
    ('CARG-005', 'Gerente de Administración Tributaria', 'Responsable de la recaudación y resoluciones tributarias', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '100 days'),
    ('CARG-006', 'Cajero Recaudador Municipal', 'Operación de ventanilla de cobranza de tasas y tributos', 4, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    ('CARG-007', 'Jefe de Serenazgo', 'Comandancia del centro de control y patrullaje', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    ('CARG-008', 'Administrador de Mercados', 'Gestión de puestos, contratos e inspecciones de abastos', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    ('CARG-009', 'Inspector de Obras y Urbanismo', 'Fiscalización de edificaciones y licencias de construcción', 4, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    ('CARG-010', 'Operador de Centro de Control y Cámaras', 'Monitoreo de cámaras de seguridad y despacho de alertas', 4, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    ('CARG-011', 'Notificador de Deudas y Resoluciones', 'Entrega de notificaciones tributarias y administrativas en campo', 4, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days'),
    ('CARG-012', 'Asesor Jurídico Municipal', 'Emisión de dictámenes e informes técnicos legales', 3, 'ACTIVO', CURRENT_TIMESTAMP - INTERVAL '90 days')
ON CONFLICT (codigo) DO UPDATE 
SET estado = 'ACTIVO', updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------------------------
-- 3. REDISTRIBUCIÓN EQUITATIVA DE PERSONAL POR OFICINA (Dashboard Chart 2)
-- ------------------------------------------------------------------------------
WITH oficinas_activas AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) - 1 as ofi_idx, COUNT(*) OVER() as total_ofi
    FROM oficinas WHERE estado = 'ACTIVO'
),
cargos_activos AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) - 1 as carg_idx, COUNT(*) OVER() as total_carg
    FROM cargos WHERE estado = 'ACTIVO'
)
UPDATE persona_oficinas po
SET 
    oficina_id = (SELECT oa.id FROM oficinas_activas oa WHERE oa.ofi_idx = (po.id % oa.total_ofi)),
    cargo_id   = (SELECT ca.id FROM cargos_activos ca WHERE ca.carg_idx = (po.id % ca.total_carg)),
    updated_at = CURRENT_TIMESTAMP
WHERE po.id IS NOT NULL;

-- ------------------------------------------------------------------------------
-- 4. DIVERSIFICACIÓN DE ESTADOS DE USUARIOS (Dashboard Chart 1)
-- ------------------------------------------------------------------------------
UPDATE usuarios 
SET enabled = false, updated_at = CURRENT_TIMESTAMP
WHERE id IN (
    SELECT id FROM usuarios 
    WHERE username NOT IN ('admin', 'admin.quinua', 'alonso.gutierrez', 'superadmin', 'demo.quinua', 'jack', 'operador.mpv', 'jefe.obras', 'especialista', 'ciudadano')
    ORDER BY id ASC LIMIT 10
);

UPDATE usuarios 
SET account_locked = true, updated_at = CURRENT_TIMESTAMP
WHERE id IN (
    SELECT id FROM usuarios 
    WHERE enabled = true 
      AND username NOT IN ('admin', 'admin.quinua', 'alonso.gutierrez', 'superadmin', 'demo.quinua', 'jack', 'operador.mpv', 'jefe.obras', 'especialista', 'ciudadano')
    ORDER BY id DESC LIMIT 5
);

-- ------------------------------------------------------------------------------
-- 5. MESA DE PARTES VIRTUAL: DIVERSIFICACIÓN DE EXPEDIENTES (MPV)
-- ------------------------------------------------------------------------------
WITH oficinas_activas AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) - 1 as ofi_idx, COUNT(*) OVER() as total_ofi
    FROM oficinas WHERE estado = 'ACTIVO'
)
UPDATE mpv_expedientes
SET 
    estado_actual = CASE (id % 6)
        WHEN 0 THEN 'PRESENTADO'
        WHEN 1 THEN 'RECIBIDO'
        WHEN 2 THEN 'EN_REVISION'
        WHEN 3 THEN 'OBSERVADO'
        WHEN 4 THEN 'EN_SUBSANACION'
        ELSE 'RESUELTO'
    END,
    prioridad = CASE (id % 3)
        WHEN 0 THEN 'URGENTE'
        WHEN 1 THEN 'ALTA'
        ELSE 'NORMAL'
    END,
    oficina_responsable_id = (SELECT oa.id FROM oficinas_activas oa WHERE oa.ofi_idx = (mpv_expedientes.id % oa.total_ofi)),
    updated_at = CURRENT_TIMESTAMP
WHERE id IS NOT NULL;

-- ------------------------------------------------------------------------------
-- 6. MERCADOS MUNICIPALES: DIVERSIFICACIÓN Y SECTORES
-- ------------------------------------------------------------------------------
INSERT INTO mercados_mercados (codigo, nombre, direccion, referencia_ubicacion, capacidad_referencial, estado, oficina_administradora_id, version)
SELECT 
    'MERC-QUINUA-002',
    'Mercado Artesanal y Ferial Pampa de Quinua',
    'Carretera Histórica Ayacucho - Pampa de Quinua Km 36',
    'Frente al Obelisco de Quinua',
    250,
    'ACTIVO',
    (SELECT id FROM oficinas WHERE codigo = 'OFI-MDE' LIMIT 1),
    0
WHERE NOT EXISTS (SELECT 1 FROM mercados_mercados WHERE codigo = 'MERC-QUINUA-002');

INSERT INTO mercados_sectores (mercado_id, codigo, nombre, tipo_sector, estado, categoria_comercial, orden_visual, version)
SELECT 
    (SELECT id FROM mercados_mercados WHERE codigo = 'MERC-QUINUA-001' LIMIT 1),
    s.cod, s.nom, 'GENERAL', 'ACTIVO', s.cat, s.ord, 0
FROM (VALUES 
    ('SEC-CARNES', 'Sector 01 - Carnes y Derivados', 'CARNES', 1),
    ('SEC-VERDURAS', 'Sector 02 - Frutas, Hortalizas y Verduras', 'VERDURAS', 2),
    ('SEC-ABARROTES', 'Sector 03 - Abarrotes y Cereales Andinos', 'ABARROTES', 3),
    ('SEC-ARTESANIA', 'Sector 04 - Cerámica y Artesanías de Quinua', 'ARTESANIAS', 4),
    ('SEC-COMIDAS', 'Sector 05 - Gastronomía Tradicional y Viandas', 'COMIDAS', 5)
) AS s(cod, nom, cat, ord)
WHERE NOT EXISTS (
    SELECT 1 FROM mercados_sectores 
    WHERE mercado_id = (SELECT id FROM mercados_mercados WHERE codigo = 'MERC-QUINUA-001' LIMIT 1) 
      AND codigo = s.cod
);

WITH sectores_list AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) - 1 as sec_idx, COUNT(*) OVER() as total_sec
    FROM mercados_sectores
)
UPDATE mercados_puestos p
SET 
    sector_id = (SELECT sl.id FROM sectores_list sl WHERE sl.sec_idx = (ABS(HASHTEXT(p.id::text)) % sl.total_sec)),
    estado = CASE (ABS(HASHTEXT(p.id::text)) % 5)
        WHEN 0 THEN 'DISPONIBLE'
        WHEN 1 THEN 'EN_MANTENIMIENTO'
        WHEN 2 THEN 'RESERVADO'
        WHEN 3 THEN 'INHABILITADO'
        ELSE 'OCUPADO'
    END,
    estado_fisico = CASE 
        WHEN (ABS(HASHTEXT(p.id::text)) % 5) = 1 THEN 'EN_MANTENIMIENTO'
        WHEN (ABS(HASHTEXT(p.id::text)) % 5) = 3 THEN 'INHABILITADO'
        ELSE 'OPERATIVO'
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE p.id IS NOT NULL;

INSERT INTO mercados_pagos (
    codigo_pago, comerciante_id, fecha_pago, monto_recibido, monto_aplicado, moneda,
    medio_pago, referencia_operacion, estado, usuario_registro, observaciones, created_at, version
)
SELECT 
    'PAG-MERC-' || LPAD(sub.n::text, 6, '0'),
    c.id,
    CURRENT_TIMESTAMP - (sub.n * INTERVAL '4 hours'),
    ROUND((80.00 + (sub.n % 120))::numeric, 2),
    ROUND((80.00 + (sub.n % 120))::numeric, 2),
    'PEN',
    CASE (sub.n % 4)
        WHEN 0 THEN 'EFECTIVO'
        WHEN 1 THEN 'TRANSFERENCIA'
        WHEN 2 THEN 'YAPE'
        ELSE 'PLIN'
    END,
    'OP-MERC-2026-' || (1000 + sub.n),
    'CONFIRMADO',
    'cajero.mercado',
    'Cobro mensual de merced conductiva de puesto municipal',
    CURRENT_TIMESTAMP - (sub.n * INTERVAL '4 hours'),
    0
FROM generate_series(1, 300) AS sub(n)
JOIN LATERAL (
    SELECT id FROM mercados_comerciantes ORDER BY id OFFSET (sub.n % 500) LIMIT 1
) c ON true
ON CONFLICT (codigo_pago) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 7. SEGURIDAD CIUDADANA Y SERENAZGO: DIVERSIFICACIÓN
-- ------------------------------------------------------------------------------
INSERT INTO serenazgo_sectores (codigo, nombre, descripcion, color_hex, estado, oficina_responsable_id, version)
VALUES 
    ('SEC-04', 'Sector 04 - Mirador y Obelisco de Quinua', 'Área de afluencia turística e histórica', '#2563EB', 'ACTIVO', (SELECT id FROM oficinas WHERE codigo = 'OFI-GSC' LIMIT 1), 0),
    ('SEC-05', 'Sector 05 - Anexo San Pedro de Cachi', 'Control de acceso vial rural y comunidades', '#16A34A', 'ACTIVO', (SELECT id FROM oficinas WHERE codigo = 'OFI-GSC' LIMIT 1), 0)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO serenazgo_zonas (sector_id, codigo, nombre, descripcion, estado, prioridad_operativa, version)
SELECT 
    s.id, z.cod, z.nom, z.des, 'ACTIVO', z.prio, 0
FROM (VALUES 
    ('SEC-04', 'ZONA-SER-002', 'Zona Histórica Pampa de Quinua', 'Área protegida del Santuario Cívico e Histórico', 'ALTA'),
    ('SEC-05', 'ZONA-SER-003', 'Zona Rural y Anexos Comunales', 'Comunidades campesinas aledañas al valle de Quinua', 'MEDIA')
) AS z(sec_cod, cod, nom, des, prio)
JOIN serenazgo_sectores s ON s.codigo = z.sec_cod
ON CONFLICT DO NOTHING;

UPDATE serenazgo_incidencias
SET 
    estado = CASE (ABS(HASHTEXT(id::text)) % 6)
        WHEN 0 THEN 'REGISTRADA'
        WHEN 1 THEN 'EN_EVALUACION'
        WHEN 2 THEN 'ASIGNADA'
        WHEN 3 THEN 'EN_ATENCION'
        WHEN 4 THEN 'RESUELTA'
        ELSE 'CERRADA'
    END,
    prioridad = CASE (ABS(HASHTEXT(id::text)) % 4)
        WHEN 0 THEN 'BAJA'
        WHEN 1 THEN 'MEDIA'
        WHEN 2 THEN 'ALTA'
        ELSE 'CRITICA'
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE id IS NOT NULL;

-- ------------------------------------------------------------------------------
-- 8. RECAUDACIÓN TRIBUTARIA: CAJAS Y PAGOS REALES
-- ------------------------------------------------------------------------------
INSERT INTO tributaria_cajas_municipales (
    codigo, nombre, ubicacion, estado, oficina_id, observaciones, version
)
VALUES
    ('CAJA-02', 'Caja de Rentas y Arbitrios', 'Palacio Municipal Ventanilla 2', 'ACTIVA', (SELECT id FROM oficinas WHERE codigo = 'OFI-GAT' LIMIT 1), 'Ventanilla 2 para cobranza tributaria', 0),
    ('CAJA-VIRTUAL', 'Ventanilla Digital y Pasarela', 'Plataforma Virtual Web', 'ACTIVA', (SELECT id FROM oficinas WHERE codigo = 'OFI-GAT' LIMIT 1), 'Módulo de recaudación virtual', 0)
ON CONFLICT (codigo) DO NOTHING;

UPDATE tributaria_obligaciones
SET 
    estado = CASE (ABS(HASHTEXT(id::text)) % 3)
        WHEN 0 THEN 'CANCELADA'
        WHEN 1 THEN 'VIGENTE'
        ELSE 'EMITIDA'
    END,
    importe_pagado = CASE 
        WHEN (ABS(HASHTEXT(id::text)) % 3) = 0 THEN importe_determinado
        WHEN (ABS(HASHTEXT(id::text)) % 3) = 1 THEN ROUND(importe_determinado * 0.50, 2)
        ELSE 0.00
    END,
    saldo = CASE 
        WHEN (ABS(HASHTEXT(id::text)) % 3) = 0 THEN 0.00
        WHEN (ABS(HASHTEXT(id::text)) % 3) = 1 THEN ROUND(importe_determinado * 0.50, 2)
        ELSE importe_determinado
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE id IS NOT NULL;

INSERT INTO tributaria_pagos (
    codigo_pago, contribuyente_id, fecha_hora_pago, importe, moneda,
    medio_pago, referencia_externa, estado, oficina_id, observaciones, created_at, version
)
SELECT 
    'PAG-TRIB-2026-' || LPAD(sub.n::text, 6, '0'),
    c.id,
    CURRENT_TIMESTAMP - (sub.n * INTERVAL '3 hours'),
    ROUND((45.00 + ((sub.n * 13) % 400))::numeric, 2),
    'PEN',
    CASE (sub.n % 4)
        WHEN 0 THEN 'EFECTIVO'
        WHEN 1 THEN 'TRANSFERENCIA'
        WHEN 2 THEN 'DEPOSITO'
        ELSE 'PAGO_MOVIL'
    END,
    'REC-QUINUA-' || (2026000 + sub.n),
    'REGISTRADO',
    (SELECT id FROM oficinas WHERE codigo = 'OFI-GAT' LIMIT 1),
    'Pago de Impuesto Predial y Arbitrios Municipales Ejercicio 2026',
    CURRENT_TIMESTAMP - (sub.n * INTERVAL '3 hours'),
    0
FROM generate_series(1, 400) AS sub(n)
JOIN LATERAL (
    SELECT id FROM tributaria_contribuyentes ORDER BY id OFFSET (sub.n % 600) LIMIT 1
) c ON true
ON CONFLICT (codigo_pago) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 9. AUDITORÍA ADMINISTRATIVA: FRECUENCIA Y ACCIONES VARIADAS (Dashboard Chart 4)
-- ------------------------------------------------------------------------------
INSERT INTO auditoria_administrativa (
    usuario_responsable, accion, entidad_afectada, entidad_id,
    resultado, detalle, ip_origen, user_agent, trace_id, created_at
)
SELECT 
    u.username,
    sub.acc,
    sub.ent,
    (1000 + (s.i % 500)),
    CASE 
        WHEN (s.i % 30 = 0) THEN 'FALLO' 
        WHEN (s.i % 15 = 0) THEN 'ALERTA' 
        ELSE 'EXITO' 
    END,
    'Operación administrativa institucional registrada: ' || sub.acc || ' sobre la entidad ' || sub.ent,
    '192.168.1.' || (10 + (s.i % 40)),
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) SIGM-Quinua/2026',
    'TRC-' || LPAD(s.i::text, 8, '0'),
    CURRENT_TIMESTAMP - (s.i * INTERVAL '15 minutes')
FROM generate_series(1, 1500) AS s(i)
JOIN LATERAL (
    SELECT username FROM usuarios ORDER BY id OFFSET (s.i % 20) LIMIT 1
) u ON true
CROSS JOIN LATERAL (
    VALUES 
        ('RECEPCIONAR_EXPEDIENTE', 'Expediente'),
        ('DERIVAR_TRAMITE', 'Derivacion'),
        ('CONFIRMAR_PAGO', 'PagoMercado'),
        ('REGISTRAR_INSPECCION', 'ActaInspeccion'),
        ('REGISTRAR_INCIDENCIA', 'Incidencia'),
        ('DESPACHAR_PATRULLA', 'Patrullaje'),
        ('EMITIR_RECIBO_PAGO', 'PagoTributario'),
        ('INICIAR_SESION', 'Usuario')
) AS sub(acc, ent)
WHERE s.i % 8 = (CASE sub.acc 
    WHEN 'RECEPCIONAR_EXPEDIENTE' THEN 0 
    WHEN 'DERIVAR_TRAMITE' THEN 1 
    WHEN 'CONFIRMAR_PAGO' THEN 2 
    WHEN 'REGISTRAR_INSPECCION' THEN 3 
    WHEN 'REGISTRAR_INCIDENCIA' THEN 4 
    WHEN 'DESPACHAR_PATRULLA' THEN 5 
    WHEN 'EMITIR_RECIBO_PAGO' THEN 6 
    ELSE 7 END);

-- ------------------------------------------------------------------------------
-- 10. SINCRONIZACIÓN DE SECUENCIAS
-- ------------------------------------------------------------------------------
SELECT setval('oficinas_id_seq', COALESCE((SELECT MAX(id) FROM oficinas), 1));
SELECT setval('cargos_id_seq', COALESCE((SELECT MAX(id) FROM cargos), 1));
SELECT setval('auditoria_administrativa_id_seq', COALESCE((SELECT MAX(id) FROM auditoria_administrativa), 1));

COMMIT;
