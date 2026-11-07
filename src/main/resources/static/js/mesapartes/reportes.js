/**
 * SIGM-QUINUA - Mesa de Partes Virtual: Orquestador Principal de Reportes y Estadísticas
 */
document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    let paginaActual = 0;
    const tamanioPagina = 15;

    // 1. Inicializar Filtros y Tema de Gráficos
    if (typeof ReportesFiltros !== 'undefined') {
        ReportesFiltros.inicializar();
    }
    if (typeof ReportesGraficos !== 'undefined') {
        ReportesGraficos.configurarTemaOscuro();
    }

    // 2. Escuchar evento de filtros aplicados
    document.addEventListener('reportes:filtros-aplicados', function () {
        paginaActual = 0;
        cargarTodo();
    });

    // 3. Vincular botones de exportación
    const btnExcel = document.getElementById('btnExportarExcel');
    if (btnExcel) {
        btnExcel.addEventListener('click', function () {
            const query = ReportesFiltros.construirQueryString();
            window.location.href = `/api/mesa-partes/reportes/exportar/excel?${query}`;
        });
    }

    const btnPdf = document.getElementById('btnExportarPdf');
    if (btnPdf) {
        btnPdf.addEventListener('click', function () {
            const query = ReportesFiltros.construirQueryString();
            window.open(`/api/mesa-partes/reportes/exportar/pdf?${query}`, '_blank');
        });
    }

    const btnRecargar = document.getElementById('btnRecargarReportes');
    if (btnRecargar) {
        btnRecargar.addEventListener('click', function () {
            cargarTodo();
        });
    }

    // 4. Funciones de Carga Asíncrona
    function cargarTodo() {
        mostrarIndicadorCarga(true);
        Promise.all([
            cargarResumen(),
            cargarEficiencia(),
            cargarPlazos(),
            cargarGraficoEstado(),
            cargarGraficoEvolucion(),
            cargarGraficoOficina(),
            cargarGraficoTramite(),
            cargarComparativa(),
            cargarSupervision(),
            cargarTablaExpedientes(paginaActual)
        ]).finally(() => {
            mostrarIndicadorCarga(false);
            if (window.lucide) {
                lucide.createIcons();
            }
        });
    }

    function mostrarIndicadorCarga(mostrar) {
        const spinner = document.getElementById('indicadorCargaReportes');
        if (spinner) {
            spinner.classList.toggle('hidden', !mostrar);
        }
    }

    async function cargarResumen() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/resumen?${query}`);
            const data = await res.json();
            if (data && data.data) {
                const r = data.data;
                actualizarTexto('kpiTotalPresentados', r.totalPresentados);
                actualizarTexto('kpiTotalRecibidos', r.totalRecibidos);
                actualizarTexto('kpiTotalEnRevision', r.totalEnRevision);
                actualizarTexto('kpiTotalObservados', r.totalObservados);
                actualizarTexto('kpiTotalEnAtencion', r.totalEnAtencion);
                actualizarTexto('kpiTotalResueltos', r.totalResueltos);
                actualizarTexto('kpiTotalCerrados', r.totalCerrados);
                actualizarTexto('kpiTotalArchivados', r.totalArchivados);
                actualizarTexto('kpiTotalPendientes', r.totalPendientes);
            }
        } catch (e) {
            console.error('Error cargando resumen estadístico:', e);
        }
    }

    async function cargarEficiencia() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/eficiencia?${query}`);
            const data = await res.json();
            if (data && data.data) {
                const ef = data.data;
                const txtProm = ef.tiempoPromedioAtencionDias != null ? `${ef.tiempoPromedioAtencionDias} d` : 'Sin datos';
                const txtMed = ef.tiempoMedianoAtencionDias != null ? `${ef.tiempoMedianoAtencionDias} d` : 'Sin datos';
                const txtRec = ef.tiempoPromedioRecepcionHoras != null ? `${ef.tiempoPromedioRecepcionHoras} h` : 'Sin datos';
                const txtDer = ef.tiempoPromedioDerivacionHoras != null ? `${ef.tiempoPromedioDerivacionHoras} h` : 'Sin datos';

                actualizarTexto('kpiPromedioAtencion', txtProm);
                actualizarTexto('kpiMedianaAtencion', txtMed);
                actualizarTexto('kpiPromedioRecepcion', txtRec);
                actualizarTexto('kpiPromedioDerivacion', txtDer);
                actualizarTexto('kpiTasaResolucion', `${ef.porcentajeResueltos}%`);
                actualizarTexto('kpiTasaObservacion', `${ef.porcentajeObservados}%`);
            }
        } catch (e) {
            console.error('Error cargando indicadores de eficiencia:', e);
        }
    }

    async function cargarPlazos() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/plazos?${query}`);
            const data = await res.json();
            if (data && data.data) {
                const p = data.data;
                actualizarTexto('plazoEnPlazo', p.enPlazo);
                actualizarTexto('plazoProximosVencer', p.proximosVencer);
                actualizarTexto('plazoVencidos', p.vencidos);
                actualizarTexto('plazoSuspendidos', p.suspendidos);
                actualizarTexto('plazoSinPlazo', p.sinPlazoConfigurado);
            }
        } catch (e) {
            console.error('Error cargando indicadores de plazos:', e);
        }
    }

    async function cargarGraficoEstado() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/por-estado?${query}`);
            const data = await res.json();
            if (data && data.data && typeof ReportesGraficos !== 'undefined') {
                ReportesGraficos.renderizarGraficoEstado(data.data);
            }
        } catch (e) {
            console.error('Error cargando gráfico por estado:', e);
        }
    }

    async function cargarGraficoEvolucion() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/evolucion?${query}`);
            const data = await res.json();
            if (data && data.data && typeof ReportesGraficos !== 'undefined') {
                ReportesGraficos.renderizarGraficoEvolucion(data.data);
            }
        } catch (e) {
            console.error('Error cargando gráfico de evolución:', e);
        }
    }

    async function cargarGraficoOficina() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/por-oficina?${query}`);
            const data = await res.json();
            if (data && data.data && typeof ReportesGraficos !== 'undefined') {
                ReportesGraficos.renderizarGraficoOficinas(data.data);
            }
        } catch (e) {
            console.error('Error cargando gráfico por oficina:', e);
        }
    }

    async function cargarGraficoTramite() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/por-tramite?${query}`);
            const data = await res.json();
            if (data && data.data && typeof ReportesGraficos !== 'undefined') {
                ReportesGraficos.renderizarGraficoTramites(data.data);
            }
        } catch (e) {
            console.error('Error cargando gráfico por trámite:', e);
        }
    }

    async function cargarComparativa() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const res = await fetch(`/api/mesa-partes/reportes/comparacion?${query}`);
            const data = await res.json();
            if (data && data.data) {
                const c = data.data;
                actualizarTexto('compLabelActual', c.periodoActualLabel);
                actualizarTexto('compLabelAnterior', c.periodoAnteriorLabel);

                actualizarTexto('compPresActual', c.presentadosActual);
                actualizarTexto('compPresAnterior', c.presentadosAnterior);
                actualizarTexto('compPresVariacion', c.variacionPctPresentados);

                actualizarTexto('compResActual', c.resueltosActual);
                actualizarTexto('compResAnterior', c.resueltosAnterior);
                actualizarTexto('compResVariacion', c.variacionPctResueltos);

                actualizarTexto('compObsActual', c.observadosActual);
                actualizarTexto('compObsAnterior', c.observadosAnterior);
                actualizarTexto('compObsVariacion', c.variacionPctObservados);
            }
        } catch (e) {
            console.error('Error cargando comparativa de períodos:', e);
        }
    }

    async function cargarSupervision() {
        try {
            const query = ReportesFiltros.construirQueryString();
            const [resDeriv, resObs, resProd] = await Promise.all([
                fetch(`/api/mesa-partes/reportes/derivaciones?${query}`).then(r => r.json()),
                fetch(`/api/mesa-partes/reportes/observaciones?${query}`).then(r => r.json()),
                fetch(`/api/mesa-partes/reportes/productividad?${query}`).then(r => r.json())
            ]);

            if (resDeriv && resDeriv.data) {
                const d = resDeriv.data;
                actualizarTexto('supDerivEnviadas', d.enviadas);
                actualizarTexto('supDerivRecibidas', d.recibidas);
                actualizarTexto('supDerivPendientes', d.pendientes);
                actualizarTexto('supDerivRechazadas', d.rechazadas);
                actualizarTexto('supDerivDevueltas', d.devueltas);
                actualizarTexto('supDerivPromedio', d.tiempoPromedioRecepcionHoras != null ? `${d.tiempoPromedioRecepcionHoras} h` : '-');
            }

            if (resObs && resObs.data) {
                const o = resObs.data;
                actualizarTexto('supObsExps', o.expedientesObservados);
                actualizarTexto('supObsTotal', o.observacionesTotales);
                actualizarTexto('supSubsPresentadas', o.subsanacionesPresentadas);
                actualizarTexto('supSubsPendientes', o.subsanacionesPendientes);
            }

            if (resProd && resProd.data) {
                const p = resProd.data;
                actualizarTexto('supProdAsignados', p.expedientesAsignados);
                actualizarTexto('supProdEnAtencion', p.expedientesEnAtencion);
                actualizarTexto('supProdActuaciones', p.actuacionesRegistradas);
                actualizarTexto('supProdFinalizados', p.expedientesFinalizados);
            }
        } catch (e) {
            console.error('Error cargando indicadores de supervisión:', e);
        }
    }

    async function cargarTablaExpedientes(pagina) {
        const tbody = document.getElementById('tbodyExpedientesReporte');
        if (!tbody) return;

        try {
            const query = ReportesFiltros.construirQueryString({ page: pagina, size: tamanioPagina });
            const res = await fetch(`/api/mesa-partes/reportes/expedientes?${query}`);
            const data = await res.json();

            if (!data || !data.data || !data.data.content || data.data.content.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center py-8 text-slate-400">
                            <i data-lucide="inbox" class="w-8 h-8 mx-auto mb-2 opacity-50"></i>
                            <p class="text-xs">No se encontraron expedientes con los filtros seleccionados.</p>
                        </td>
                    </tr>
                `;
                actualizarPaginacion(0, 0, 0);
                return;
            }

            const pageData = data.data;
            let html = '';

            pageData.content.forEach((item, index) => {
                const nro = (pagina * tamanioPagina) + index + 1;
                const badgeEstado = obtenerBadgeEstado(item.estado);
                const badgePrioridad = obtenerBadgePrioridad(item.prioridad);
                const fecha = item.fechaPresentacion ? item.fechaPresentacion.substring(0, 10) : '-';

                html += `
                    <tr class="border-b border-slate-700/50 hover:bg-slate-800/40 transition-colors text-xs">
                        <td class="py-2.5 px-3 text-center text-slate-400">${nro}</td>
                        <td class="py-2.5 px-3 font-semibold text-blue-400">
                            <a href="/dashboard/mesa-partes/expedientes/${item.id}" class="hover:underline">
                                ${escapeHtml(item.numeroExpediente || '-')}
                            </a>
                        </td>
                        <td class="py-2.5 px-3 text-slate-300">${fecha}</td>
                        <td class="py-2.5 px-3 text-slate-200">${escapeHtml(item.tipoTramite || '-')}</td>
                        <td class="py-2.5 px-3 text-slate-300">
                            <div class="font-medium">${escapeHtml(item.solicitante || '-')}</div>
                            <div class="text-[10px] text-slate-400">${escapeHtml(item.solicitanteDoc || '')}</div>
                        </td>
                        <td class="py-2.5 px-3 text-slate-300">${escapeHtml(item.oficina || '-')}</td>
                        <td class="py-2.5 px-3 text-center">${badgeEstado}</td>
                        <td class="py-2.5 px-3 text-center font-mono font-bold ${item.diasTranscurridos > 30 ? 'text-rose-400' : 'text-slate-300'}">
                            ${item.diasTranscurridos != null ? item.diasTranscurridos + ' d' : '-'}
                        </td>
                    </tr>
                `;
            });

            tbody.innerHTML = html;
            actualizarPaginacion(pageData.number, pageData.totalPages, pageData.totalElements);
        } catch (e) {
            console.error('Error cargando tabla de expedientes:', e);
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-6 text-rose-400 text-xs">
                        Error al cargar los datos de expedientes. Intente nuevamente.
                    </td>
                </tr>
            `;
        }
    }

    function actualizarPaginacion(current, totalPages, totalElements) {
        paginaActual = current;
        actualizarTexto('paginacionTotalRegistros', totalElements);
        actualizarTexto('paginacionPaginaActual', totalPages > 0 ? current + 1 : 0);
        actualizarTexto('paginacionTotalPaginas', totalPages);

        const btnPrev = document.getElementById('btnPaginaAnterior');
        const btnNext = document.getElementById('btnPaginaSiguiente');

        if (btnPrev) {
            btnPrev.disabled = current <= 0;
            btnPrev.onclick = () => {
                if (paginaActual > 0) {
                    cargarTablaExpedientes(paginaActual - 1);
                }
            };
        }

        if (btnNext) {
            btnNext.disabled = current >= totalPages - 1;
            btnNext.onclick = () => {
                if (paginaActual < totalPages - 1) {
                    cargarTablaExpedientes(paginaActual + 1);
                }
            };
        }
    }

    function obtenerBadgeEstado(estado) {
        switch (estado) {
            case 'RESUELTO':
            case 'CERRADO':
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">RESUELTO</span>';
            case 'RECIBIDO':
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">RECIBIDO</span>';
            case 'EN_ATENCION':
            case 'EN_CALIFICACION':
            case 'EN_TRAMITE':
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-purple-500/20 text-purple-400 border border-purple-500/30">EN ATENCIÓN</span>';
            case 'OBSERVADO':
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-rose-500/20 text-rose-400 border border-rose-500/30">OBSERVADO</span>';
            case 'EN_REVISION':
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-amber-500/20 text-amber-400 border border-amber-500/30">EN REVISIÓN</span>';
            case 'ARCHIVADO':
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-500/20 text-slate-400 border border-slate-500/30">ARCHIVADO</span>';
            case 'PRESENTADO':
            default:
                return '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-blue-500/20 text-blue-400 border border-blue-500/30">PRESENTADO</span>';
        }
    }

    function obtenerBadgePrioridad(prioridad) {
        switch (prioridad) {
            case 'URGENTE':
                return '<span class="text-rose-400 font-bold">URGENTE</span>';
            case 'ALTA':
                return '<span class="text-amber-400 font-semibold">ALTA</span>';
            default:
                return '<span class="text-slate-400">NORMAL</span>';
        }
    }

    function actualizarTexto(elementId, valor) {
        const el = document.getElementById(elementId);
        if (el) {
            el.textContent = valor != null ? valor : '-';
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Ejecución inicial
    cargarTodo();
});
