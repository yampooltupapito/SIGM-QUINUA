/**
 * SIGM-QUINUA - Editor Gráfico Interactivo de Planos de Mercados
 * SVG Modular con Selección, Arrastre, Redimensionamiento, Rotación, Cuadrícula y Publicación
 */

let modoHerramienta = 'SELECCIONAR'; // SELECCIONAR, AGREGAR_PUESTO, AGREGAR_SECTOR, AGREGAR_PABELLON, etc.
let elementosEditor = []; // Array de elementos en memoria
let elementoSeleccionadoId = null;
let elementoArrastrado = null;
let offsetArrastreX = 0;
let offsetArrastreY = 0;
let snapGrid = true;
let gridSize = 20;

let zoomEditor = 1.0;
let panEditorX = 0;
let panEditorY = 0;
let isPanningEditor = false;
let startPanX = 0;
let startPanY = 0;

let planoMercadoId = null;
let versionPlanoId = null;
let baseCanvasWidth = 1400;
let baseCanvasHeight = 900;
let versionControlLock = 0;

document.addEventListener('DOMContentLoaded', function () {
    inicializarEditor();
});

function inicializarEditor() {
    const configData = document.getElementById('editorConfigData');
    if (!configData) return;

    planoMercadoId = configData.getAttribute('data-mercado-id');
    versionPlanoId = configData.getAttribute('data-version-id');
    baseCanvasWidth = parseFloat(configData.getAttribute('data-ancho-base')) || 1400;
    baseCanvasHeight = parseFloat(configData.getAttribute('data-alto-base')) || 900;
    versionControlLock = parseInt(configData.getAttribute('data-version-lock')) || 0;

    // Cargar elementos iniciales inyectados en JSON
    try {
        const rawJson = document.getElementById('elementosInicialesJson').textContent;
        elementosEditor = JSON.parse(rawJson) || [];
    } catch (e) {
        console.warn('No se encontraron elementos iniciales pre-cargados', e);
        elementosEditor = [];
    }

    renderizarElementosEnLienzo();
    configurarEventosLienzo();
}

function configurarEventosLienzo() {
    const lienzo = document.getElementById('contenedorLienzoEditor');
    const svg = document.getElementById('svgEditor');
    const grupo = document.getElementById('grupoEditor');

    if (!lienzo || !svg) return;

    // Clic en el fondo del lienzo SVG para agregar elementos o deseleccionar
    svg.addEventListener('click', function (e) {
        if (e.target.closest('.elemento-grafico')) return;

        if (modoHerramienta.startsWith('AGREGAR_')) {
            const pt = obtenerCoordenadasSvg(e);
            agregarElementoNuevo(modoHerramienta, pt.x, pt.y);
            cambiarHerramienta('SELECCIONAR');
        } else {
            deseleccionarElemento();
        }
    });

    // Paneo con botón central o tecla espacio + arrastre
    lienzo.addEventListener('mousedown', function (e) {
        if (e.button === 1 || e.target === svg || e.target.id === 'fondoCuadricula') {
            isPanningEditor = true;
            startPanX = e.clientX - panEditorX;
            startPanY = e.clientY - panEditorY;
        }
    });

    window.addEventListener('mousemove', function (e) {
        if (isPanningEditor) {
            panEditorX = e.clientX - startPanX;
            panEditorY = e.clientY - startPanY;
            actualizarTransformacionEditor();
        } else if (elementoArrastrado) {
            const pt = obtenerCoordenadasSvg(e);
            let nuevoX = pt.x - offsetArrastreX;
            let nuevoY = pt.y - offsetArrastreY;

            if (snapGrid) {
                nuevoX = Math.round(nuevoX / gridSize) * gridSize;
                nuevoY = Math.round(nuevoY / gridSize) * gridSize;
            }

            // Limitar al lienzo base
            nuevoX = Math.max(0, Math.min(baseCanvasWidth - elementoArrastrado.ancho, nuevoX));
            nuevoY = Math.max(0, Math.min(baseCanvasHeight - elementoArrastrado.alto, nuevoY));

            elementoArrastrado.coordenadaX = nuevoX;
            elementoArrastrado.coordenadaY = nuevoY;

            actualizarElementoDom(elementoArrastrado);
            actualizarValoresPropiedades(elementoArrastrado);
        }
    });

    window.addEventListener('mouseup', function () {
        isPanningEditor = false;
        elementoArrastrado = null;
    });

    // Zoom con rueda
    lienzo.addEventListener('wheel', function (e) {
        e.preventDefault();
        const delta = e.deltaY < 0 ? 0.1 : -0.1;
        zoomEditor = Math.max(0.2, Math.min(4.0, zoomEditor + delta));
        actualizarTransformacionEditor();
    }, { passive: false });
}

function obtenerCoordenadasSvg(evt) {
    const svg = document.getElementById('svgEditor');
    const pt = svg.createSVGPoint();
    pt.x = evt.clientX;
    pt.y = evt.clientY;
    const grupo = document.getElementById('grupoEditor');
    const matrizGlobal = grupo.getScreenCTM().inverse();
    return pt.matrixTransform(matrizGlobal);
}

function actualizarTransformacionEditor() {
    const grupo = document.getElementById('grupoEditor');
    if (grupo) {
        grupo.setAttribute('transform', `translate(${panEditorX}, ${panEditorY}) scale(${zoomEditor})`);
    }
}

function cambiarHerramienta(herramienta) {
    modoHerramienta = herramienta;

    document.querySelectorAll('.btn-herramienta').forEach(btn => {
        if (btn.getAttribute('data-tool') === herramienta) {
            btn.classList.add('bg-blue-600', 'text-white');
            btn.classList.remove('bg-[#202c3d]', 'text-slate-300');
        } else {
            btn.classList.remove('bg-blue-600', 'text-white');
            btn.classList.add('bg-[#202c3d]', 'text-slate-300');
        }
    });

    const lienzo = document.getElementById('contenedorLienzoEditor');
    if (lienzo) {
        lienzo.style.cursor = herramienta === 'SELECCIONAR' ? 'default' : 'crosshair';
    }
}

function agregarElementoNuevo(tipoHerramienta, x, y) {
    let tipo = 'PUESTO';
    let ancho = 60;
    let alto = 60;
    let etiqueta = 'Puesto';
    let codigo = 'P-' + (elementosEditor.filter(e => e.tipoElemento === 'PUESTO').length + 1);

    if (tipoHerramienta === 'AGREGAR_SECTOR') {
        tipo = 'SECTOR';
        ancho = 240;
        alto = 160;
        etiqueta = 'Sector Nuevo';
        codigo = 'SEC-' + (elementosEditor.filter(e => e.tipoElemento === 'SECTOR').length + 1);
    } else if (tipoHerramienta === 'AGREGAR_PABELLON') {
        tipo = 'PABELLON';
        ancho = 200;
        alto = 120;
        etiqueta = 'Pabellón';
        codigo = 'PAB-' + (elementosEditor.filter(e => e.tipoElemento === 'PABELLON').length + 1);
    } else if (tipoHerramienta === 'AGREGAR_PASILLO') {
        tipo = 'PASILLO';
        ancho = 180;
        alto = 40;
        etiqueta = 'Pasillo';
        codigo = 'PAS';
    } else if (tipoHerramienta === 'AGREGAR_ACCESO') {
        tipo = 'ACCESO';
        ancho = 80;
        alto = 30;
        etiqueta = 'Acceso Principal';
        codigo = 'ACC';
    } else if (tipoHerramienta === 'AGREGAR_SALIDA') {
        tipo = 'SALIDA';
        ancho = 80;
        alto = 30;
        etiqueta = 'Salida Emergencia';
        codigo = 'SAL';
    } else if (tipoHerramienta === 'AGREGAR_ETIQUETA') {
        tipo = 'ETIQUETA';
        ancho = 100;
        alto = 30;
        etiqueta = 'Texto Informativo';
        codigo = 'TXT';
    }

    if (snapGrid) {
        x = Math.round(x / gridSize) * gridSize;
        y = Math.round(y / gridSize) * gridSize;
    }

    const nuevoElem = {
        id: 'tmp_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
        tipoElemento: tipo,
        referenciaEntidadId: null,
        codigoVisual: codigo,
        etiqueta: etiqueta,
        coordenadaX: Math.max(0, x),
        coordenadaY: Math.max(0, y),
        ancho: ancho,
        alto: alto,
        rotacion: 0,
        ordenCapa: tipo === 'PUESTO' ? 10 : (tipo === 'SECTOR' || tipo === 'PABELLON' ? 1 : 5),
        colorHex: tipo === 'PUESTO' ? '#10b981' : '#3b82f6',
        estadoVisual: 'DISPONIBLE'
    };

    elementosEditor.push(nuevoElem);
    renderizarElementosEnLienzo();
    seleccionarElementoParaEdicion(nuevoElem.id);
}

function renderizarElementosEnLienzo() {
    const contenedor = document.getElementById('capaElementosGraficos');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    // Ordenar por orden de capa
    const ordenados = [...elementosEditor].sort((a, b) => a.ordenCapa - b.ordenCapa);

    ordenados.forEach(elem => {
        const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        g.setAttribute('class', 'elemento-grafico cursor-move select-none');
        g.setAttribute('id', 'elemSvg_' + elem.id);
        g.setAttribute('data-elem-id', elem.id);
        g.setAttribute('style', `transform-origin: ${elem.coordenadaX + elem.ancho / 2}px ${elem.coordenadaY + elem.alto / 2}px; transform: rotate(${elem.rotacion}deg);`);

        if (elem.tipoElemento === 'PUESTO') {
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('x', elem.coordenadaX);
            rect.setAttribute('y', elem.coordenadaY);
            rect.setAttribute('width', elem.ancho);
            rect.setAttribute('height', elem.alto);
            rect.setAttribute('rx', 6);
            rect.setAttribute('ry', 6);
            rect.setAttribute('fill', elem.colorHex || '#10b981');
            rect.setAttribute('fill-opacity', '0.85');
            rect.setAttribute('stroke', elem.id === elementoSeleccionadoId ? '#38bdf8' : '#ffffff');
            rect.setAttribute('stroke-width', elem.id === elementoSeleccionadoId ? '3' : '1.5');
            rect.setAttribute('stroke-opacity', elem.id === elementoSeleccionadoId ? '1' : '0.4');
            g.appendChild(rect);

            const txt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            txt.setAttribute('x', elem.coordenadaX + elem.ancho / 2);
            txt.setAttribute('y', elem.coordenadaY + elem.alto / 2 + 4);
            txt.setAttribute('text-anchor', 'middle');
            txt.setAttribute('fill', '#ffffff');
            txt.setAttribute('font-size', '11');
            txt.setAttribute('font-weight', 'bold');
            txt.setAttribute('font-family', 'monospace');
            txt.setAttribute('pointer-events', 'none');
            txt.textContent = elem.etiqueta || elem.codigoVisual;
            g.appendChild(txt);
        } else {
            // Elementos de contorno (Sector, Pasillo, etc.)
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('x', elem.coordenadaX);
            rect.setAttribute('y', elem.coordenadaY);
            rect.setAttribute('width', elem.ancho);
            rect.setAttribute('height', elem.alto);
            rect.setAttribute('rx', 6);
            rect.setAttribute('ry', 6);
            rect.setAttribute('fill', elem.tipoElemento === 'SALIDA' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(30, 41, 59, 0.5)');
            rect.setAttribute('stroke', elem.id === elementoSeleccionadoId ? '#38bdf8' : (elem.tipoElemento === 'SALIDA' ? '#ef4444' : '#475569'));
            rect.setAttribute('stroke-width', elem.id === elementoSeleccionadoId ? '3' : '1.5');
            if (elem.tipoElemento === 'SECTOR' || elem.tipoElemento === 'PABELLON') {
                rect.setAttribute('stroke-dasharray', '6,4');
            }
            g.appendChild(rect);

            const txt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            txt.setAttribute('x', elem.coordenadaX + 10);
            txt.setAttribute('y', elem.coordenadaY + 18);
            txt.setAttribute('fill', '#94a3b8');
            txt.setAttribute('font-size', '11');
            txt.setAttribute('font-weight', 'bold');
            txt.setAttribute('pointer-events', 'none');
            txt.textContent = elem.etiqueta || elem.codigoVisual;
            g.appendChild(txt);
        }

        // Eventos mousedown sobre el elemento para arrastre y selección
        g.addEventListener('mousedown', function (e) {
            e.stopPropagation();
            seleccionarElementoParaEdicion(elem.id);

            elementoArrastrado = elem;
            const pt = obtenerCoordenadasSvg(e);
            offsetArrastreX = pt.x - elem.coordenadaX;
            offsetArrastreY = pt.y - elem.coordenadaY;
        });

        contenedor.appendChild(g);
    });

    actualizarContadorElementos();
}

function actualizarElementoDom(elem) {
    const g = document.getElementById('elemSvg_' + elem.id);
    if (!g) return;

    g.setAttribute('style', `transform-origin: ${elem.coordenadaX + elem.ancho / 2}px ${elem.coordenadaY + elem.alto / 2}px; transform: rotate(${elem.rotacion}deg);`);

    const rect = g.querySelector('rect');
    if (rect) {
        rect.setAttribute('x', elem.coordenadaX);
        rect.setAttribute('y', elem.coordenadaY);
        rect.setAttribute('width', elem.ancho);
        rect.setAttribute('height', elem.alto);
    }

    const txt = g.querySelector('text');
    if (txt) {
        if (elem.tipoElemento === 'PUESTO') {
            txt.setAttribute('x', elem.coordenadaX + elem.ancho / 2);
            txt.setAttribute('y', elem.coordenadaY + elem.alto / 2 + 4);
        } else {
            txt.setAttribute('x', elem.coordenadaX + 10);
            txt.setAttribute('y', elem.coordenadaY + 18);
        }
    }
}

function seleccionarElementoParaEdicion(id) {
    elementoSeleccionadoId = id;
    const elem = elementosEditor.find(e => e.id === id);
    if (!elem) return;

    renderizarElementosEnLienzo();

    // Mostrar panel de propiedades
    const panel = document.getElementById('panelPropiedadesElemento');
    const sinSeleccion = document.getElementById('panelSinSeleccion');
    if (panel) panel.classList.remove('hidden');
    if (sinSeleccion) sinSeleccion.classList.add('hidden');

    actualizarValoresPropiedades(elem);
}

function deseleccionarElemento() {
    elementoSeleccionadoId = null;
    const panel = document.getElementById('panelPropiedadesElemento');
    const sinSeleccion = document.getElementById('panelSinSeleccion');
    if (panel) panel.classList.add('hidden');
    if (sinSeleccion) sinSeleccion.classList.remove('hidden');

    renderizarElementosEnLienzo();
}

function actualizarValoresPropiedades(elem) {
    const inputX = document.getElementById('propCoordX');
    const inputY = document.getElementById('propCoordY');
    const inputW = document.getElementById('propAncho');
    const inputH = document.getElementById('propAlto');
    const inputRot = document.getElementById('propRotacion');
    const inputCod = document.getElementById('propCodigo');
    const inputEti = document.getElementById('propEtiqueta');
    const selPuesto = document.getElementById('propVinculoPuesto');
    const contenedorVinculo = document.getElementById('contenedorVinculoPuesto');

    if (inputX) inputX.value = elem.coordenadaX;
    if (inputY) inputY.value = elem.coordenadaY;
    if (inputW) inputW.value = elem.ancho;
    if (inputH) inputH.value = elem.alto;
    if (inputRot) inputRot.value = elem.rotacion || 0;
    if (inputCod) inputCod.value = elem.codigoVisual || '';
    if (inputEti) inputEti.value = elem.etiqueta || '';

    if (contenedorVinculo) {
        if (elem.tipoElemento === 'PUESTO') {
            contenedorVinculo.classList.remove('hidden');
            if (selPuesto) selPuesto.value = elem.referenciaEntidadId || '';
        } else {
            contenedorVinculo.classList.add('hidden');
        }
    }
}

function aplicarCambioPropiedades() {
    if (!elementoSeleccionadoId) return;
    const elem = elementosEditor.find(e => e.id === elementoSeleccionadoId);
    if (!elem) return;

    elem.coordenadaX = parseFloat(document.getElementById('propCoordX').value) || 0;
    elem.coordenadaY = parseFloat(document.getElementById('propCoordY').value) || 0;
    elem.ancho = Math.max(10, parseFloat(document.getElementById('propAncho').value) || 60);
    elem.alto = Math.max(10, parseFloat(document.getElementById('propAlto').value) || 60);
    elem.rotacion = parseFloat(document.getElementById('propRotacion').value) || 0;
    elem.codigoVisual = document.getElementById('propCodigo').value.trim();
    elem.etiqueta = document.getElementById('propEtiqueta').value.trim();

    const selPuesto = document.getElementById('propVinculoPuesto');
    if (selPuesto && elem.tipoElemento === 'PUESTO') {
        const puestoId = selPuesto.value;
        elem.referenciaEntidadId = puestoId || null;

        // Si se seleccionó un puesto, actualizar etiqueta con su código
        if (puestoId && selPuesto.selectedOptions.length > 0) {
            const texto = selPuesto.selectedOptions[0].text;
            elem.codigoVisual = texto.split(' - ')[0] || elem.codigoVisual;
        }
    }

    renderizarElementosEnLienzo();
}

function eliminarElementoSeleccionado() {
    if (!elementoSeleccionadoId) return;
    if (!confirm('¿Estás seguro de eliminar este elemento gráfico del borrador?')) return;

    elementosEditor = elementosEditor.filter(e => e.id !== elementoSeleccionadoId);
    deseleccionarElemento();
}

function actualizarContadorElementos() {
    const totalEl = document.getElementById('totalElementosContador');
    const puestosEl = document.getElementById('totalPuestosContador');
    if (totalEl) totalEl.textContent = elementosEditor.length;
    if (puestosEl) puestosEl.textContent = elementosEditor.filter(e => e.tipoElemento === 'PUESTO').length;
}

// Herramientas de alineación
function alinearSeleccion(modo) {
    if (!elementoSeleccionadoId) return;
    const elem = elementosEditor.find(e => e.id === elementoSeleccionadoId);
    if (!elem) return;

    if (modo === 'CENTRO_H') {
        elem.coordenadaX = Math.round((baseCanvasWidth - elem.ancho) / 2);
    } else if (modo === 'CENTRO_V') {
        elem.coordenadaY = Math.round((baseCanvasHeight - elem.alto) / 2);
    } else if (modo === 'IZQUIERDA') {
        elem.coordenadaX = 20;
    } else if (modo === 'ARRIBA') {
        elem.coordenadaY = 20;
    }

    renderizarElementosEnLienzo();
    actualizarValoresPropiedades(elem);
}

// Guardar borrador en el backend
function guardarBorradorPlano() {
    const btn = document.getElementById('btnGuardarBorrador');
    if (btn) btn.disabled = true;

    const payload = {
        versionId: versionPlanoId,
        descripcionCambios: document.getElementById('descripcionCambiosInput')?.value || 'Edición gráfica en lienzo interactivo',
        version: versionControlLock,
        elementos: elementosEditor.map(e => ({
            id: (e.id && !e.id.startsWith('tmp_')) ? e.id : null,
            tipoElemento: e.tipoElemento,
            referenciaEntidadId: e.referenciaEntidadId || null,
            codigoVisual: e.codigoVisual,
            etiqueta: e.etiqueta,
            coordenadaX: e.coordenadaX,
            coordenadaY: e.coordenadaY,
            ancho: e.ancho,
            alto: e.alto,
            rotacion: e.rotacion,
            ordenCapa: e.ordenCapa,
            estiloConfigurado: e.estiloConfigurado || null
        }))
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = {
        'Content-Type': 'application/json'
    };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch(`/api/mercados/${planoMercadoId}/plano/borradores/${versionPlanoId}`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: 'Error al guardar distribución.' }));
            throw new Error(err.message || 'Error al guardar borrador.');
        }
        return res.json();
    })
    .then(data => {
        mostrarNotificacionToast('Borrador guardado exitosamente con ' + data.totalElementos + ' elementos.', 'exito');
        versionControlLock = data.version;
    })
    .catch(err => {
        console.error(err);
        mostrarNotificacionToast(err.message, 'error');
    })
    .finally(() => {
        if (btn) btn.disabled = false;
    });
}

// Modal de publicación
function abrirModalPublicar() {
    const modal = document.getElementById('modalPublicarPlano');
    if (modal) modal.classList.remove('hidden');
}

function cerrarModalPublicar() {
    const modal = document.getElementById('modalPublicarPlano');
    if (modal) modal.classList.add('hidden');
}

function ejecutarPublicacionPlano() {
    const observaciones = document.getElementById('observacionesPublicacion')?.value || '';
    const btn = document.getElementById('btnConfirmarPublicar');
    if (btn) btn.disabled = true;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    fetch(`/api/mercados/${planoMercadoId}/plano/versiones/${versionPlanoId}/publicar`, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ observaciones: observaciones })
    })
    .then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: 'Error al publicar plano.' }));
            throw new Error(err.message || 'Error al publicar plano.');
        }
        return res.json();
    })
    .then(data => {
        alert('¡Plano publicado exitosamente como versión oficial vigente!');
        window.location.href = `/dashboard/mercados/planos/${planoMercadoId}`;
    })
    .catch(err => {
        alert('Error al publicar: ' + err.message);
        if (btn) btn.disabled = false;
    });
}

function mostrarNotificacionToast(mensaje, tipo) {
    const toast = document.getElementById('toastNotificacion');
    if (!toast) return;

    toast.textContent = mensaje;
    if (tipo === 'error') {
        toast.className = 'fixed bottom-5 right-5 px-4 py-3 bg-rose-900 border border-rose-700 text-white rounded-xl shadow-2xl text-xs z-50';
    } else {
        toast.className = 'fixed bottom-5 right-5 px-4 py-3 bg-emerald-900 border border-emerald-700 text-white rounded-xl shadow-2xl text-xs z-50';
    }

    toast.classList.remove('hidden');
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 4000);
}
