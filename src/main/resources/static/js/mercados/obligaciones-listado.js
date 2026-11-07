var paginaActual = 0;

document.addEventListener('DOMContentLoaded', () => {
    cargarObligaciones(0);

    document.getElementById('formFiltros')?.addEventListener('submit', (e) => {
        e.preventDefault();
        cargarObligaciones(0);
    });

    document.getElementById('filtroQ')?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            cargarObligaciones(0);
        }
    });

    document.getElementById('filtroMercado')?.addEventListener('change', () => cargarObligaciones(0));
    document.getElementById('filtroConcepto')?.addEventListener('change', () => cargarObligaciones(0));
    document.getElementById('filtroEstado')?.addEventListener('change', () => cargarObligaciones(0));
});

async function cargarObligaciones(pagina = 0) {
    paginaActual = pagina;
    const q = document.getElementById('filtroQ')?.value || '';
    const mercadoId = document.getElementById('filtroMercado')?.value || '';
    const conceptoId = document.getElementById('filtroConcepto')?.value || '';
    const estado = document.getElementById('filtroEstado')?.value || '';

    const params = new URLSearchParams({
        page: pagina,
        size: 10
    });
    if (q) params.append('q', q);
    if (mercadoId) params.append('mercadoId', mercadoId);
    if (conceptoId) params.append('conceptoId', conceptoId);
    if (estado) params.append('estado', estado);

    const tbody = document.getElementById('tbodyObligaciones');
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="9" class="px-5 py-8 text-center text-slate-400">Cargando obligaciones...</td></tr>`;

    try {
        const resp = await fetch(`/api/mercados/obligaciones?${params.toString()}`);
        if (!resp.ok) throw new Error('Error al cargar obligaciones');
        const data = await resp.json();

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="px-5 py-8 text-center text-slate-400">No se encontraron obligaciones registradas.</td></tr>`;
            document.getElementById('paginacionObligaciones').innerHTML = '';
            return;
        }

        tbody.innerHTML = data.content.map(o => {
            const estadoBadge = getEstadoBadge(o.estado);
            const vencimientoStr = o.fechaVencimiento || '-';
            const saldoColor = o.saldoPendiente > 0 ? 'text-amber-400' : 'text-slate-400';

            return `
                <tr class="hover:bg-[#202c3d]/50 transition-colors">
                    <td class="px-5 py-4 font-mono font-bold text-blue-400">
                        <a href="/dashboard/mercados/obligaciones/${o.id}" class="hover:underline">${o.codigoObligacion}</a>
                    </td>
                    <td class="px-5 py-4">
                        <span class="font-bold text-white block">${escapeHtml(o.comercianteNombre || '-')}</span>
                        <span class="text-[11px] text-slate-400">DNI/RUC: ${o.comercianteDocumento || '-'}</span>
                    </td>
                    <td class="px-5 py-4">
                        <span class="text-slate-200 block font-semibold">Puesto ${o.puestoNumero || '-'}</span>
                        <span class="text-[11px] text-slate-400">${escapeHtml(o.mercadoNombre || '-')}</span>
                    </td>
                    <td class="px-5 py-4">
                        <span class="text-slate-300 block font-medium">${escapeHtml(o.conceptoNombre || '-')}</span>
                        <span class="text-[10px] font-mono text-slate-500">${o.conceptoCodigo || ''}</span>
                    </td>
                    <td class="px-5 py-4 text-right font-mono font-bold text-slate-200">
                        S/ ${Number(o.montoOriginal).toFixed(2)}
                    </td>
                    <td class="px-5 py-4 text-right font-mono font-bold ${saldoColor}">
                        S/ ${Number(o.saldoPendiente).toFixed(2)}
                    </td>
                    <td class="px-5 py-4 text-slate-300">
                        ${vencimientoStr}
                    </td>
                    <td class="px-5 py-4 text-center">
                        ${estadoBadge}
                    </td>
                    <td class="px-5 py-4 text-right space-x-2">
                        <a href="/dashboard/mercados/obligaciones/${o.id}"
                           class="inline-flex items-center px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-[#334155] transition-colors">
                            Detalle
                        </a>
                        ${o.saldoPendiente > 0 ? `
                        <a href="/dashboard/mercados/pagos/nuevo?comercianteId=${o.comercianteId}"
                           class="inline-flex items-center px-2.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold shadow transition-colors">
                            Cobrar
                        </a>` : ''}
                    </td>
                </tr>
            `;
        }).join('');

        renderPaginacion(data);
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="9" class="px-5 py-8 text-center text-rose-400 font-semibold">Error al conectar con el servidor.</td></tr>`;
    }
}

function getEstadoBadge(estado) {
    switch (estado) {
        case 'PAGADA':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">PAGADA</span>`;
        case 'PARCIALMENTE_PAGADA':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20">PAGO PARCIAL</span>`;
        case 'PENDIENTE':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">PENDIENTE</span>`;
        case 'VENCIDA':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">VENCIDA</span>`;
        case 'ANULADA':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-slate-500/10 text-slate-400 border border-slate-500/20">ANULADA</span>`;
        default:
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-slate-800 text-slate-300 border border-[#334155]">${estado || '-'}</span>`;
    }
}

function renderPaginacion(pageData) {
    const cont = document.getElementById('paginacionObligaciones');
    if (!cont) return;

    const totalPages = pageData.totalPages;
    const current = pageData.number;

    cont.innerHTML = `
        <div>
            Mostrando página <span class="font-bold text-white">${current + 1}</span> de <span class="font-bold text-white">${totalPages || 1}</span> (${pageData.totalElements} registros en total)
        </div>
        <div class="flex items-center gap-2">
            <button onclick="cargarObligaciones(${current - 1})" ${current === 0 ? 'disabled' : ''}
                    class="px-3 py-1.5 rounded-xl border border-[#334155] bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-semibold">
                Anterior
            </button>
            <button onclick="cargarObligaciones(${current + 1})" ${current >= totalPages - 1 ? 'disabled' : ''}
                    class="px-3 py-1.5 rounded-xl border border-[#334155] bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-semibold">
                Siguiente
            </button>
        </div>
    `;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, m => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[m]);
}

window.cargarObligaciones = cargarObligaciones;

