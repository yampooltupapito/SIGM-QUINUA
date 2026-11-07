var paginaActual = 0;

document.addEventListener('DOMContentLoaded', () => {
    cargarPagos(0);

    document.getElementById('formFiltrosPagos')?.addEventListener('submit', (e) => {
        e.preventDefault();
        cargarPagos(0);
    });

    document.getElementById('filtroQ')?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            cargarPagos(0);
        }
    });

    document.getElementById('filtroMedio')?.addEventListener('change', () => cargarPagos(0));
    document.getElementById('filtroEstado')?.addEventListener('change', () => cargarPagos(0));
});

async function cargarPagos(pagina = 0) {
    paginaActual = pagina;
    const q = document.getElementById('filtroQ')?.value || '';
    const medioPago = document.getElementById('filtroMedio')?.value || '';
    const estado = document.getElementById('filtroEstado')?.value || '';

    const params = new URLSearchParams({
        page: pagina,
        size: 10
    });
    if (q) params.append('q', q);
    if (medioPago) params.append('medioPago', medioPago);
    if (estado) params.append('estado', estado);

    const tbody = document.getElementById('tbodyPagos');
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="9" class="px-5 py-8 text-center text-slate-400">Cargando pagos...</td></tr>`;

    try {
        const resp = await fetch(`/api/mercados/pagos?${params.toString()}`);
        if (!resp.ok) throw new Error('Error al cargar pagos');
        const data = await resp.json();

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="px-5 py-8 text-center text-slate-400">No se encontraron pagos registrados.</td></tr>`;
            document.getElementById('paginacionPagos').innerHTML = '';
            return;
        }

        tbody.innerHTML = data.content.map(p => {
            const fechaStr = p.fechaPago ? new Date(p.fechaPago).toLocaleString('es-PE') : '-';
            const estadoBadge = getEstadoPagoBadge(p.estado);

            return `
                <tr class="hover:bg-[#202c3d]/50 transition-colors">
                    <td class="px-5 py-4 font-mono font-bold text-emerald-400">
                        <a href="/dashboard/mercados/pagos/${p.id}" class="hover:underline">${p.codigoPago}</a>
                    </td>
                    <td class="px-5 py-4 text-slate-300">
                        ${fechaStr}
                    </td>
                    <td class="px-5 py-4">
                        <span class="font-bold text-white block">${escapeHtml(p.comercianteNombre || '-')}</span>
                        <span class="text-[11px] text-slate-400">DNI/RUC: ${p.comercianteDocumento || '-'}</span>
                    </td>
                    <td class="px-5 py-4">
                        <span class="px-2 py-0.5 rounded-lg bg-slate-800 text-slate-300 border border-[#334155] text-[11px]">
                            ${escapeHtml(p.medioPagoDescripcion || p.medioPago)}
                        </span>
                        ${p.referenciaOperacion ? `<span class="block text-[10px] text-slate-500 font-mono mt-0.5">Ref: ${escapeHtml(p.referenciaOperacion)}</span>` : ''}
                    </td>
                    <td class="px-5 py-4 text-right font-mono font-bold text-white">
                        S/ ${Number(p.montoRecibido).toFixed(2)}
                    </td>
                    <td class="px-5 py-4 text-right font-mono font-bold text-emerald-400">
                        S/ ${Number(p.montoAplicado).toFixed(2)}
                    </td>
                    <td class="px-5 py-4 text-slate-300">
                        <span class="block font-semibold text-slate-200">${p.cajaCodigo || 'OFICINA'}</span>
                        <span class="text-[10px] text-slate-500 font-mono">${p.cajeroUsername || '-'}</span>
                    </td>
                    <td class="px-5 py-4 text-center">
                        ${estadoBadge}
                    </td>
                    <td class="px-5 py-4 text-right space-x-2">
                        <a href="/dashboard/mercados/pagos/${p.id}"
                           class="inline-flex items-center px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-[#334155] transition-colors">
                            Detalle
                        </a>
                        <a href="/api/mercados/pagos/${p.id}/constancia" target="_blank"
                           class="inline-flex items-center px-2.5 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow transition-colors">
                            PDF
                        </a>
                    </td>
                </tr>
            `;
        }).join('');

        renderPaginacionPagos(data);
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="9" class="px-5 py-8 text-center text-rose-400 font-semibold">Error al conectar con el servidor.</td></tr>`;
    }
}

function getEstadoPagoBadge(estado) {
    switch (estado) {
        case 'CONFIRMADO':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">CONFIRMADO</span>`;
        case 'CONCILIADO':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20">CONCILIADO</span>`;
        case 'REVERSADO':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">REVERSADO</span>`;
        case 'ANULADO':
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-slate-500/10 text-slate-400 border border-slate-500/20">ANULADO</span>`;
        default:
            return `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-slate-800 text-slate-300 border border-[#334155]">${estado || '-'}</span>`;
    }
}

function renderPaginacionPagos(pageData) {
    const cont = document.getElementById('paginacionPagos');
    if (!cont) return;

    const totalPages = pageData.totalPages;
    const current = pageData.number;

    cont.innerHTML = `
        <div>
            Mostrando página <span class="font-bold text-white">${current + 1}</span> de <span class="font-bold text-white">${totalPages || 1}</span> (${pageData.totalElements} recibos en total)
        </div>
        <div class="flex items-center gap-2">
            <button onclick="cargarPagos(${current - 1})" ${current === 0 ? 'disabled' : ''}
                    class="px-3 py-1.5 rounded-xl border border-[#334155] bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:opacity-40 disabled:cursor-not-allowed text-xs font-semibold">
                Anterior
            </button>
            <button onclick="cargarPagos(${current + 1})" ${current >= totalPages - 1 ? 'disabled' : ''}
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

window.cargarPagos = cargarPagos;

