/**
 * SIGM-QUINUA - Visor Interactivo de Planos de Mercados
 * SVG Interactivo con Pan/Zoom, Selección, Búsqueda, Filtros y Accesibilidad
 */

let zoomLevel = 1.0;
let panX = 0;
let panY = 0;
let isPanning = false;
let startX = 0;
let startY = 0;
let baseWidth = 1400;
let baseHeight = 900;
let elementoSeleccionado = null;
let filtroEstadoActual = 'TODOS';
let filtroSectorActual = '';
let filtroPabellonActual = '';

document.addEventListener('DOMContentLoaded', function () {
    inicializarVisor();
});

function inicializarVisor() {
    const contenedor = document.getElementById('contenedorLienzoSvg');
    const svg = document.getElementById('svgPlano');
    const grupoEscala = document.getElementById('grupoTransformacionPlano');

    if (!contenedor || !svg || !grupoEscala) return;

    // Leer dimensiones del plano
    baseWidth = parseFloat(svg.getAttribute('viewBox').split(' ')[2]) || 1400;
    baseHeight = parseFloat(svg.getAttribute('viewBox').split(' ')[3]) || 900;

    // Mouse Drag para desplazamiento (Pan)
    contenedor.addEventListener('mousedown', function (e) {
        if (e.target.closest('.elemento-interactivo')) return;
        isPanning = true;
        startX = e.clientX - panX;
        startY = e.clientY - panY;
        contenedor.style.cursor = 'grabbing';
    });

    window.addEventListener('mousemove', function (e) {
        if (!isPanning) return;
        panX = e.clientX - startX;
        panY = e.clientY - startY;
        actualizarTransformacion();
    });

    window.addEventListener('mouseup', function () {
        isPanning = false;
        contenedor.style.cursor = 'grab';
    });

    // Touch support para móviles / tablets
    let initialTouchDist = 0;
    contenedor.addEventListener('touchstart', function (e) {
        if (e.touches.length === 1) {
            isPanning = true;
            startX = e.touches[0].clientX - panX;
            startY = e.touches[0].clientY - panY;
        } else if (e.touches.length === 2) {
            initialTouchDist = Math.hypot(
                e.touches[0].clientX - e.touches[1].clientX,
                e.touches[0].clientY - e.touches[1].clientY
            );
        }
    }, { passive: true });

    contenedor.addEventListener('touchmove', function (e) {
        if (e.touches.length === 1 && isPanning) {
            panX = e.touches[0].clientX - startX;
            panY = e.touches[0].clientY - startY;
            actualizarTransformacion();
        } else if (e.touches.length === 2) {
            const dist = Math.hypot(
                e.touches[0].clientX - e.touches[1].clientX,
                e.touches[0].clientY - e.touches[1].clientY
            );
            if (initialTouchDist > 0) {
                const diff = (dist - initialTouchDist) * 0.005;
                zoomLevel = Math.max(0.2, Math.min(5.0, zoomLevel + diff));
                actualizarTransformacion();
            }
        }
    }, { passive: true });

    contenedor.addEventListener('touchend', function () {
        isPanning = false;
        initialTouchDist = 0;
    });

    // Rueda del ratón para Zoom centrado
    contenedor.addEventListener('wheel', function (e) {
        e.preventDefault();
        const delta = e.deltaY < 0 ? 0.1 : -0.1;
        const nuevoZoom = Math.max(0.2, Math.min(5.0, zoomLevel + delta));

        // Ajustar zoom manteniendo punto del cursor
        const rect = contenedor.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        panX -= (mouseX - panX) * (nuevoZoom / zoomLevel - 1);
        panY -= (mouseY - panY) * (nuevoZoom / zoomLevel - 1);
        zoomLevel = nuevoZoom;

        actualizarTransformacion();
    }, { passive: false });

    // Ajustar vista inicial
    fitToScreen();

    // Eventos de teclado globales
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            cerrarPanelPuesto();
        }
    });

    // Configurar buscador con debounce
    const buscador = document.getElementById('buscadorPuesto');
    if (buscador) {
        buscador.addEventListener('input', debounce(ejecutarBusquedaPuesto, 250));
    }

    // Event listeners explícitos para cerrar el panel lateral de puesto
    const btnCerrarHeader = document.getElementById('btnCerrarPanelPuestoX');
    if (btnCerrarHeader) btnCerrarHeader.addEventListener('click', cerrarPanelPuesto);
    const btnCerrarFooter = document.getElementById('btnCerrarPanelPuesto');
    if (btnCerrarFooter) btnCerrarFooter.addEventListener('click', cerrarPanelPuesto);
    const backdropEl = document.getElementById('puestoModalBackdrop');
    if (backdropEl) backdropEl.addEventListener('click', cerrarPanelPuesto);
}

function actualizarTransformacion() {
    const grupo = document.getElementById('grupoTransformacionPlano');
    if (grupo) {
        grupo.setAttribute('transform', `translate(${panX}, ${panY}) scale(${zoomLevel})`);
    }
}

function zoomIn() {
    zoomLevel = Math.min(5.0, zoomLevel + 0.2);
    actualizarTransformacion();
}

function zoomOut() {
    zoomLevel = Math.max(0.2, zoomLevel - 0.2);
    actualizarTransformacion();
}

function resetZoom() {
    zoomLevel = 1.0;
    panX = 0;
    panY = 0;
    actualizarTransformacion();
}

function fitToScreen() {
    const contenedor = document.getElementById('contenedorLienzoSvg');
    if (!contenedor) return;

    const contWidth = contenedor.clientWidth || 1000;
    const contHeight = contenedor.clientHeight || 650;

    const scaleX = (contWidth - 40) / baseWidth;
    const scaleY = (contHeight - 40) / baseHeight;
    zoomLevel = Math.min(scaleX, scaleY, 1.2);

    panX = (contWidth - baseWidth * zoomLevel) / 2;
    panY = (contHeight - baseHeight * zoomLevel) / 2;

    actualizarTransformacion();
}

// Clic o pulsación de teclado en un puesto comercial
function seleccionarPuesto(elementoSvg) {
    if (!elementoSvg) return;

    const puestoId = elementoSvg.getAttribute('data-puesto-id');
    if (!puestoId) return;

    // Deseleccionar elemento previo
    if (elementoSeleccionado) {
        elementoSeleccionado.classList.remove('puesto-seleccionado');
    }

    // Resaltar elemento actual
    elementoSeleccionado = elementoSvg;
    elementoSvg.classList.add('puesto-seleccionado');

    // Cargar datos reales vía API
    abrirPanelPuesto(puestoId);
}

function abrirPanelPuesto(puestoId) {
    const modal = document.getElementById('panelPuesto');
    const backdrop = document.getElementById('puestoModalBackdrop');
    if (!modal) return;

    // Abrir visualmente
    if (backdrop) backdrop.classList.remove('hidden');
    modal.classList.remove('hidden');
    requestAnimationFrame(() => {
        modal.classList.remove('translate-x-full');
        modal.style.transform = 'translateX(0)';
    });

    // Estado cargando
    document.getElementById('modalPuestoTitulo').textContent = 'Cargando datos...';
    document.getElementById('modalPuestoCodigo').textContent = '...';

    fetch(`/api/mercados/puestos/${puestoId}/informacion-plano`)
        .then(res => {
            if (!res.ok) throw new Error('Error al consultar datos del puesto.');
            return res.json();
        })
        .then(data => {
            document.getElementById('modalPuestoTitulo').textContent = `Puesto ${data.numero || data.codigo}`;
            document.getElementById('modalPuestoSubtitulo').textContent = data.nombreReferencial || (data.sectorNombre ? `Sector: ${data.sectorNombre}` : 'Mercado Municipal');
            document.getElementById('modalPuestoCodigo').textContent = data.codigo || '-';
            document.getElementById('modalEstadoVisual').textContent = data.estadoVisualLabel || 'DISPONIBLE';

            // Color badge
            const badgeColor = document.getElementById('modalEstadoBadgeColor');
            if (badgeColor) {
                badgeColor.style.backgroundColor = data.colorHex || '#10b981';
            }

            // Datos Físicos
            document.getElementById('modalSectorNombre').textContent = data.sectorNombre || 'No asignado';
            document.getElementById('modalPabellonNombre').textContent = data.pabellonNombre || 'No asignado';
            document.getElementById('modalArea').textContent = data.areaMetrosCuadrados ? Number(data.areaMetrosCuadrados).toFixed(2) : '-';
            document.getElementById('modalEstadoFisico').textContent = data.estadoFisico || 'OPERATIVO';

            // Titular
            const sinTitular = document.getElementById('modalSinTitular');
            const conTitular = document.getElementById('modalConTitular');
            if (data.ocupado && data.comercianteNombre) {
                if (sinTitular) sinTitular.classList.add('hidden');
                if (conTitular) conTitular.classList.remove('hidden');
                document.getElementById('modalComercianteNombre').textContent = data.comercianteNombre;
                document.getElementById('modalComercianteDoc').textContent = data.comercianteDocumento || '-';
                document.getElementById('modalComercianteTel').textContent = data.comercianteTelefono || 'No registrado';
            } else {
                if (sinTitular) sinTitular.classList.remove('hidden');
                if (conTitular) conTitular.classList.add('hidden');
            }

            // Títulos de Ocupación
            const seccionOcupacion = document.getElementById('modalSeccionOcupacion');
            if (data.asignacionCodigo || data.contratoCodigo || data.autorizacionCodigo) {
                if (seccionOcupacion) seccionOcupacion.classList.remove('hidden');
                document.getElementById('modalAsignacionCodigo').textContent = data.asignacionCodigo || 'Ninguna';
                document.getElementById('modalContratoCodigo').textContent = data.contratoCodigo || 'Ninguno';
                document.getElementById('modalAutorizacionCodigo').textContent = data.autorizacionCodigo || 'Ninguna';
            } else {
                if (seccionOcupacion) seccionOcupacion.classList.add('hidden');
            }

            // Incidencias e Inspecciones
            document.getElementById('modalIncidenciasCount').textContent = data.incidenciasPendientes != null ? data.incidenciasPendientes : '0';
            document.getElementById('modalInspeccionesCount').textContent = data.inspeccionesRealizadas != null ? data.inspeccionesRealizadas : '0';

            // Finanzas
            const seccionFinanzas = document.getElementById('modalSeccionFinanzas');
            if (data.finanzasVisibles && data.totalDeudaPendiente != null) {
                if (seccionFinanzas) seccionFinanzas.classList.remove('hidden');
                const deudaEl = document.getElementById('modalDeudaTotal');
                deudaEl.textContent = `S/ ${Number(data.totalDeudaPendiente).toFixed(2)}`;
                if (data.tieneDeudaVencida) {
                    deudaEl.className = 'font-mono font-bold text-rose-400';
                } else {
                    deudaEl.className = 'font-mono font-bold text-emerald-400';
                }
            } else {
                if (seccionFinanzas) seccionFinanzas.classList.add('hidden');
            }

            // Enlaces directos
            const btnFicha = document.getElementById('btnVerFichaPuesto');
            if (btnFicha && data.puestoId) {
                btnFicha.href = `/dashboard/mercados/puestos/${data.puestoId}`;
            }
            const btnHistorial = document.getElementById('btnHistorialTitularidad');
            if (btnHistorial && data.puestoId) {
                btnHistorial.href = `/dashboard/mercados/gestiones-ocupacion/historial?puestoId=${data.puestoId}`;
            }
        })
        .catch(err => {
            console.error(err);
            document.getElementById('modalPuestoTitulo').textContent = 'Error al cargar';
            document.getElementById('modalPuestoSubtitulo').textContent = err.message;
        });
}

function cerrarPanelPuesto() {
    const modal = document.getElementById('panelPuesto');
    const backdrop = document.getElementById('puestoModalBackdrop');
    if (modal) {
        modal.classList.add('translate-x-full');
        modal.style.transform = 'translateX(100%)';
        setTimeout(() => {
            modal.classList.add('hidden');
        }, 250);
    }
    if (backdrop) backdrop.classList.add('hidden');

    if (elementoSeleccionado) {
        elementoSeleccionado.classList.remove('puesto-seleccionado');
        elementoSeleccionado = null;
    }
}

// Filtros por Estado Visual
function filtrarPorEstado(estado) {
    filtroEstadoActual = estado;

    // Actualizar botones de filtro
    document.querySelectorAll('.filtro-btn').forEach(btn => {
        if (btn.getAttribute('data-filtro-estado') === estado) {
            btn.classList.add('active-filter');
            btn.classList.add('bg-slate-700');
        } else {
            btn.classList.remove('active-filter');
            btn.classList.remove('bg-slate-700');
        }
    });

    aplicarTodosLosFiltros();
}

function aplicarFiltrosEstructura() {
    const selSector = document.getElementById('filtroSector');
    const selPab = document.getElementById('filtroPabellon');

    filtroSectorActual = selSector ? selSector.value : '';
    filtroPabellonActual = selPab ? selPab.value : '';

    aplicarTodosLosFiltros();
}

function aplicarTodosLosFiltros() {
    const puestos = document.querySelectorAll('.elemento-puesto');

    puestos.forEach(p => {
        const est = p.getAttribute('data-estado-visual');
        const sec = p.getAttribute('data-sector-id');
        const pab = p.getAttribute('data-pabellon-id');

        const coincideEstado = (filtroEstadoActual === 'TODOS' || est === filtroEstadoActual);
        const coincideSector = (!filtroSectorActual || sec === filtroSectorActual);
        const coincidePabellon = (!filtroPabellonActual || pab === filtroPabellonActual);

        if (coincideEstado && coincideSector && coincidePabellon) {
            p.style.opacity = '1';
            p.style.pointerEvents = 'auto';
        } else {
            p.style.opacity = '0.12';
            p.style.pointerEvents = 'none';
        }
    });
}

// Búsqueda de puestos
function ejecutarBusquedaPuesto() {
    const query = document.getElementById('buscadorPuesto').value.trim().toLowerCase();
    const drop = document.getElementById('resultadosBusqueda');
    if (!drop) return;

    if (!query) {
        drop.classList.add('hidden');
        drop.innerHTML = '';
        return;
    }

    const puestos = document.querySelectorAll('.elemento-puesto');
    const coincidencias = [];

    puestos.forEach(p => {
        const cod = (p.getAttribute('data-codigo') || '').toLowerCase();
        const num = (p.getAttribute('data-numero') || '').toLowerCase();
        const sec = (p.getAttribute('data-sector-nombre') || '').toLowerCase();

        if (cod.includes(query) || num.includes(query) || sec.includes(query)) {
            coincidencias.push({
                element: p,
                id: p.getAttribute('data-puesto-id'),
                codigo: p.getAttribute('data-codigo'),
                numero: p.getAttribute('data-numero'),
                sector: p.getAttribute('data-sector-nombre') || 'Sector General',
                estado: p.getAttribute('data-estado-visual'),
                color: p.getAttribute('data-color-hex') || '#10b981'
            });
        }
    });

    if (coincidencias.length === 0) {
        drop.innerHTML = '<div class="p-3 text-slate-400 text-center text-xs">No se encontraron puestos que coincidan</div>';
        drop.classList.remove('hidden');
        return;
    }

    let html = '';
    coincidencias.slice(0, 10).forEach(c => {
        html += `
            <div onclick="centrarEnPuesto('${c.id}')"
                 class="p-2.5 hover:bg-slate-800 cursor-pointer border-b border-slate-700/50 flex items-center justify-between text-xs transition-colors">
                <div class="flex items-center gap-2">
                    <span class="w-2.5 h-2.5 rounded-full" style="background-color: ${c.color}"></span>
                    <span class="font-bold text-white">${c.codigo}</span>
                    <span class="text-slate-400">Puesto ${c.numero}</span>
                </div>
                <span class="text-[10px] text-slate-400">${c.sector}</span>
            </div>
        `;
    });

    drop.innerHTML = html;
    drop.classList.remove('hidden');
}

function limpiarBusqueda() {
    const inp = document.getElementById('buscadorPuesto');
    if (inp) inp.value = '';
    const drop = document.getElementById('resultadosBusqueda');
    if (drop) {
        drop.classList.add('hidden');
        drop.innerHTML = '';
    }
    // Restaurar opacidades
    aplicarTodosLosFiltros();
}

function centrarEnPuesto(puestoId) {
    const drop = document.getElementById('resultadosBusqueda');
    if (drop) drop.classList.add('hidden');

    const elemento = document.querySelector(`.elemento-puesto[data-puesto-id="${puestoId}"]`);
    if (!elemento) return;

    const x = parseFloat(elemento.getAttribute('data-x')) || 0;
    const y = parseFloat(elemento.getAttribute('data-y')) || 0;
    const w = parseFloat(elemento.getAttribute('data-ancho')) || 60;
    const h = parseFloat(elemento.getAttribute('data-alto')) || 60;

    const contenedor = document.getElementById('contenedorLienzoSvg');
    const contWidth = contenedor ? contenedor.clientWidth : 1000;
    const contHeight = contenedor ? contenedor.clientHeight : 650;

    // Zoom específico de enfoque
    zoomLevel = 1.8;
    panX = (contWidth / 2) - (x + w / 2) * zoomLevel;
    panY = (contHeight / 2) - (y + h / 2) * zoomLevel;

    actualizarTransformacion();
    seleccionarPuesto(elemento);
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

// Exposición en el objeto global window
window.cerrarPanelPuesto = cerrarPanelPuesto;
window.abrirPanelPuesto = abrirPanelPuesto;
window.seleccionarPuesto = seleccionarPuesto;
window.centrarEnPuesto = centrarEnPuesto;
window.filtrarPorEstado = filtrarPorEstado;
window.aplicarFiltrosEstructura = aplicarFiltrosEstructura;
window.limpiarBusqueda = limpiarBusqueda;
window.zoomIn = zoomIn;
window.zoomOut = zoomOut;
window.resetZoom = resetZoom;
window.fitToScreen = fitToScreen;
