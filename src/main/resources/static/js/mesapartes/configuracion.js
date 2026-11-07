/**
 * SIGM-QUINUA — Configuración Administrativa de Mesa de Partes Virtual (Prompt 19)
 * Control de Catálogo de Trámites, Requisitos, Formularios Dinámicos, Plazos, Calendario y Plantillas.
 */

document.addEventListener('DOMContentLoaded', function () {
    inicializarIconos();
    inicializarCsrf();
    inicializarTramites();
    inicializarRequisitos();
    inicializarConstructorFormularios();
    inicializarPlazos();
    inicializarCalendario();
    inicializarPlantillas();
});

let csrfToken = '';
let csrfHeader = '';

function inicializarCsrf() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    if (metaToken && metaHeader) {
        csrfToken = metaToken.getAttribute('content');
        csrfHeader = metaHeader.getAttribute('content');
    }
}

function inicializarIconos() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

function fetchConCsrf(url, opciones = {}) {
    opciones.headers = opciones.headers || {};
    if (csrfToken && csrfHeader) {
        opciones.headers[csrfHeader] = csrfToken;
    }
    opciones.headers['Content-Type'] = opciones.headers['Content-Type'] || 'application/json';
    return fetch(url, opciones);
}

/* =========================================================================
 * 1. MÓDULO DE CATÁLOGO DE TRÁMITES
 * ========================================================================= */
function inicializarTramites() {
    const modalTramite = document.getElementById('modalTramite');
    const formTramite = document.getElementById('formTramite');
    const btnNuevo = document.getElementById('btnNuevoTramite');
    const btnCerrar = document.getElementById('btnCerrarModalTramite');
    const btnCancelar = document.getElementById('btnCancelarTramite');

    if (btnNuevo && modalTramite) {
        btnNuevo.addEventListener('click', () => {
            document.getElementById('modalTramiteTitulo').innerText = 'Nuevo Procedimiento / Trámite';
            formTramite.reset();
            document.getElementById('tramiteId').value = '';
            document.getElementById('tramiteCodigo').readOnly = false;
            modalTramite.classList.remove('hidden');
            inicializarIconos();
        });
    }

    if (btnCerrar && modalTramite) {
        btnCerrar.addEventListener('click', () => modalTramite.classList.add('hidden'));
    }
    if (btnCancelar && modalTramite) {
        btnCancelar.addEventListener('click', () => modalTramite.classList.add('hidden'));
    }

    // Editar trámite
    document.querySelectorAll('.btnEditarTramite').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            try {
                const res = await fetch(`/api/mesa-partes/configuracion/tramites/${id}`);
                const json = await res.json();
                if (json.success && json.data) {
                    const t = json.data;
                    document.getElementById('modalTramiteTitulo').innerText = 'Editar Procedimiento: ' + t.codigo;
                    document.getElementById('tramiteId').value = t.id;
                    document.getElementById('tramiteCodigo').value = t.codigo;
                    document.getElementById('tramiteCodigo').readOnly = true;
                    document.getElementById('tramiteCodigoTupa').value = t.codigoTupa || '';
                    document.getElementById('tramiteNombre').value = t.nombre || '';
                    document.getElementById('tramiteDenominacionOficial').value = t.denominacionOficial || '';
                    document.getElementById('tramiteCategoria').value = t.categoria || 'GENERAL';
                    document.getElementById('tramiteOficinaId').value = t.oficinaResponsableId || '';
                    document.getElementById('tramitePlazoDias').value = t.plazoDias || 30;
                    document.getElementById('tramiteUnidadPlazo').value = t.unidadPlazo || 'DIAS_HABILES';
                    document.getElementById('tramiteBaseNormativa').value = t.baseNormativa || '';
                    document.getElementById('tramiteRequiereDocumentos').checked = !!t.requiereDocumentos;
                    document.getElementById('tramitePermiteVirtual').checked = !!t.permitePresentacionVirtual;
                    document.getElementById('tramitePermitePresencial').checked = !!t.permitePresencial;
                    document.getElementById('tramiteRequiereTasa').checked = !!t.requiereTasa;
                    document.getElementById('tramiteMontoTasa').value = t.montoTasa || '0.00';
                    document.getElementById('tramiteCuentaTasa').value = t.cuentaTasa || '';

                    modalTramite.classList.remove('hidden');
                    inicializarIconos();
                }
            } catch (err) {
                alert('Error al consultar datos del trámite: ' + err.message);
            }
        });
    });

    if (formTramite) {
        formTramite.addEventListener('submit', async function (e) {
            e.preventDefault();
            const id = document.getElementById('tramiteId').value;
            const esEdicion = !!id;

            const payload = {
                codigo: document.getElementById('tramiteCodigo').value.trim().toUpperCase(),
                nombre: document.getElementById('tramiteNombre').value.trim(),
                codigoTupa: document.getElementById('tramiteCodigoTupa').value.trim() || null,
                denominacionOficial: document.getElementById('tramiteDenominacionOficial').value.trim() || null,
                categoria: document.getElementById('tramiteCategoria').value.trim() || 'GENERAL',
                oficinaResponsableId: parseInt(document.getElementById('tramiteOficinaId').value),
                plazoDias: parseInt(document.getElementById('tramitePlazoDias').value),
                unidadPlazo: document.getElementById('tramiteUnidadPlazo').value,
                baseNormativa: document.getElementById('tramiteBaseNormativa').value.trim() || null,
                requiereDocumentos: document.getElementById('tramiteRequiereDocumentos').checked,
                permitePresentacionVirtual: document.getElementById('tramitePermiteVirtual').checked,
                permitePresencial: document.getElementById('tramitePermitePresencial').checked,
                requiereTasa: document.getElementById('tramiteRequiereTasa').checked,
                montoTasa: parseFloat(document.getElementById('tramiteMontoTasa').value) || 0.00,
                cuentaTasa: document.getElementById('tramiteCuentaTasa').value.trim() || null
            };

            const url = esEdicion ? `/api/mesa-partes/configuracion/tramites/${id}` : '/api/mesa-partes/configuracion/tramites';
            const method = esEdicion ? 'PUT' : 'POST';

            try {
                const res = await fetchConCsrf(url, {
                    method: method,
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Procedimiento guardado satisfactoriamente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo guardar el procedimiento.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    // Modal de Transiciones de Trámite
    const modalTransicion = document.getElementById('modalTransicion');
    const formTransicion = document.getElementById('formTransicion');
    const btnCancTrans = document.getElementById('btnCancelarTransicion');

    if (btnCancTrans && modalTransicion) {
        btnCancTrans.addEventListener('click', () => modalTransicion.classList.add('hidden'));
    }

    document.querySelectorAll('.btnAccionTransicion').forEach(btn => {
        btn.addEventListener('click', function () {
            const id = this.getAttribute('data-id');
            const accion = this.getAttribute('data-accion');
            const titulo = this.getAttribute('data-titulo');

            document.getElementById('transicionTramiteId').value = id;
            document.getElementById('transicionAccion').value = accion;
            document.getElementById('modalTransicionTitulo').innerText = titulo || 'Confirmar Acción';
            document.getElementById('transicionMotivo').value = '';

            modalTransicion.classList.remove('hidden');
            inicializarIconos();
        });
    });

    if (formTransicion) {
        formTransicion.addEventListener('submit', async function (e) {
            e.preventDefault();
            const id = document.getElementById('transicionTramiteId').value;
            const accion = document.getElementById('transicionAccion').value;
            const motivo = document.getElementById('transicionMotivo').value.trim();

            try {
                const res = await fetchConCsrf(`/api/mesa-partes/configuracion/tramites/${id}/${accion}`, {
                    method: 'POST',
                    body: JSON.stringify({ motivo: motivo || 'Acción administrativa realizada desde panel' })
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Acción completada con éxito.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo procesar la acción.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    // Modal de Versiones Inmutables
    const modalVersiones = document.getElementById('modalVersiones');
    const btnCerrarVersiones = document.getElementById('btnCerrarModalVersiones');
    const contenedorVersiones = document.getElementById('contenedorListaVersiones');

    if (btnCerrarVersiones && modalVersiones) {
        btnCerrarVersiones.addEventListener('click', () => modalVersiones.classList.add('hidden'));
    }

    document.querySelectorAll('.btnVerVersiones').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            const codigo = this.getAttribute('data-codigo');
            document.getElementById('modalVersionesTitulo').innerText = 'Historial de Versiones — ' + codigo;
            contenedorVersiones.innerHTML = '<div class="text-center py-4 text-slate-400">Cargando versiones inmutables...</div>';
            modalVersiones.classList.remove('hidden');
            inicializarIconos();

            try {
                const res = await fetch(`/api/mesa-partes/configuracion/tramites/${id}/versiones`);
                const json = await res.json();
                if (json.success && json.data) {
                    if (json.data.length === 0) {
                        contenedorVersiones.innerHTML = '<div class="text-center py-4 text-slate-400">No hay versiones registradas aún.</div>';
                    } else {
                        let html = '<div class="space-y-2">';
                        json.data.forEach(v => {
                            html += `
                                <div class="p-3 bg-slate-900/80 rounded-xl border border-slate-700/60 flex items-start justify-between text-xs">
                                    <div class="space-y-1">
                                        <div class="flex items-center space-x-2">
                                            <span class="px-2 py-0.5 rounded font-mono font-bold bg-blue-500/20 text-blue-300">v${v.numeroVersion}</span>
                                            <span class="font-bold text-slate-100">${v.nombre}</span>
                                            <span class="text-[10px] text-emerald-400 font-semibold">• ${v.estado}</span>
                                        </div>
                                        <div class="text-[11px] text-slate-400">Oficina: ${v.oficinaResponsableNombre} | Plazo: ${v.plazoDias} ${v.unidadPlazo}</div>
                                        <div class="text-[11px] text-slate-400">Motivo: ${v.motivoCambio || 'Sin motivo'}</div>
                                    </div>
                                    <div class="text-right text-[10px] text-slate-500 font-mono">
                                        <div>Publicado por: ${v.publicadoPor || v.createdBy || 'Sistema'}</div>
                                        <div>Fecha: ${v.fechaPublicacion ? v.fechaPublicacion.substring(0, 10) : (v.createdAt ? v.createdAt.substring(0, 10) : '')}</div>
                                    </div>
                                </div>
                            `;
                        });
                        html += '</div>';
                        contenedorVersiones.innerHTML = html;
                    }
                }
            } catch (err) {
                contenedorVersiones.innerHTML = '<div class="text-rose-400 py-4">Error al cargar versiones: ' + err.message + '</div>';
            }
        });
    });
}

/* =========================================================================
 * 2. MÓDULO DE REQUISITOS DOCUMENTALES
 * ========================================================================= */
function inicializarRequisitos() {
    const modalRequisito = document.getElementById('modalRequisito');
    const formRequisito = document.getElementById('formRequisito');
    const btnNuevo = document.getElementById('btnNuevoRequisito');
    const btnCerrar = document.getElementById('btnCerrarModalRequisito');
    const btnCancelar = document.getElementById('btnCancelarRequisito');

    if (btnNuevo && modalRequisito) {
        btnNuevo.addEventListener('click', () => {
            document.getElementById('modalRequisitoTitulo').innerText = 'Nuevo Requisito Documental';
            formRequisito.reset();
            document.getElementById('requisitoId').value = '';
            document.getElementById('requisitoCodigo').readOnly = false;
            modalRequisito.classList.remove('hidden');
            inicializarIconos();
        });
    }

    if (btnCerrar && modalRequisito) {
        btnCerrar.addEventListener('click', () => modalRequisito.classList.add('hidden'));
    }
    if (btnCancelar && modalRequisito) {
        btnCancelar.addEventListener('click', () => modalRequisito.classList.add('hidden'));
    }

    document.querySelectorAll('.btnEditarRequisito').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            try {
                const res = await fetch(`/api/mesa-partes/configuracion/requisitos/${id}`);
                const json = await res.json();
                if (json.success && json.data) {
                    const r = json.data;
                    document.getElementById('modalRequisitoTitulo').innerText = 'Editar Requisito: ' + r.codigo;
                    document.getElementById('requisitoId').value = r.id;
                    document.getElementById('requisitoCodigo').value = r.codigo;
                    document.getElementById('requisitoNombre').value = r.nombre;
                    document.getElementById('requisitoDescripcion').value = r.descripcion || '';
                    document.getElementById('requisitoOrden').value = r.orden;
                    document.getElementById('requisitoTamano').value = r.tamanoMaximoMb;
                    document.getElementById('requisitoFormatos').value = r.formatosPermitidos;
                    document.getElementById('requisitoObligatorio').checked = !!r.obligatorio;

                    modalRequisito.classList.remove('hidden');
                    inicializarIconos();
                }
            } catch (err) {
                alert('Error al consultar requisito: ' + err.message);
            }
        });
    });

    if (formRequisito) {
        formRequisito.addEventListener('submit', async function (e) {
            e.preventDefault();
            const id = document.getElementById('requisitoId').value;
            const esEdicion = !!id;

            const payload = {
                tipoTramiteId: parseInt(document.getElementById('requisitoTramiteId').value),
                codigo: document.getElementById('requisitoCodigo').value.trim().toUpperCase(),
                nombre: document.getElementById('requisitoNombre').value.trim(),
                descripcion: document.getElementById('requisitoDescripcion').value.trim() || null,
                orden: parseInt(document.getElementById('requisitoOrden').value) || 1,
                tamanoMaximoMb: parseInt(document.getElementById('requisitoTamano').value) || 15,
                formatosPermitidos: document.getElementById('requisitoFormatos').value.trim(),
                obligatorio: document.getElementById('requisitoObligatorio').checked
            };

            const url = esEdicion ? `/api/mesa-partes/configuracion/requisitos/${id}` : '/api/mesa-partes/configuracion/requisitos';
            const method = esEdicion ? 'PUT' : 'POST';

            try {
                const res = await fetchConCsrf(url, {
                    method: method,
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Requisito guardado satisfactoriamente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo guardar el requisito.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    document.querySelectorAll('.btnToggleEstadoRequisito').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            const nuevoEstado = this.getAttribute('data-estado');
            if (!confirm(`¿Confirma cambiar el estado de este requisito a ${nuevoEstado}?`)) return;

            try {
                const res = await fetchConCsrf(`/api/mesa-partes/configuracion/requisitos/${id}/estado?estado=${nuevoEstado}`, {
                    method: 'PUT'
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo actualizar el estado.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    });
}

/* =========================================================================
 * 3. CONSTRUCTOR VISUAL DE FORMULARIOS DINÁMICOS
 * ========================================================================= */
let camposFormulario = [];

function inicializarConstructorFormularios() {
    const canvas = document.getElementById('canvasCampos');
    if (!canvas) return;

    // Extraer campos iniciales si existen en el DOM
    document.querySelectorAll('.itemCampoFormulario').forEach((item, idx) => {
        // Campos precargados por Thymeleaf
    });

    // Agregar nuevo campo desde la paleta
    document.querySelectorAll('.btnAgregarTipoCampo').forEach(btn => {
        btn.addEventListener('click', function () {
            const tipo = this.getAttribute('data-tipo');
            const nuevoIdx = camposFormulario.length + 1;
            const nuevoCampo = {
                nombreCampo: 'campo_' + nuevoIdx,
                etiqueta: 'Nuevo Campo ' + nuevoIdx,
                tipoCampo: tipo,
                obligatorio: false,
                orden: nuevoIdx,
                placeholder: '',
                textoAyuda: '',
                longitudMaxima: tipo === 'TEXTO_LARGO' ? 2000 : 250,
                expresionRegular: '',
                opciones: tipo === 'SELECCION' ? [
                    { valor: 'OPC1', etiqueta: 'Opción 1', orden: 1 },
                    { valor: 'OPC2', etiqueta: 'Opción 2', orden: 2 }
                ] : []
            };

            camposFormulario.push(nuevoCampo);
            renderizarCamposEnCanvas();
        });
    });

    // Guardar borrador del formulario
    const btnGuardar = document.getElementById('btnGuardarFormulario');
    if (btnGuardar) {
        btnGuardar.addEventListener('click', async function () {
            const tramiteId = parseInt(document.getElementById('selectTramiteForm').value);
            if (!tramiteId) return alert('Seleccione un trámite.');

            if (camposFormulario.length === 0) {
                // Verificar si hay campos en el DOM inicial
                const domItems = document.querySelectorAll('.itemCampoFormulario');
                if (domItems.length === 0) {
                    return alert('Agregue al menos un campo al formulario.');
                }
            }

            const payload = {
                tipoTramiteId: tramiteId,
                codigo: 'FORM-TR-' + tramiteId,
                nombre: 'Formulario Dinámico de Trámite',
                descripcion: 'Configuración de datos interactivos para el administrado',
                campos: camposFormulario
            };

            try {
                const res = await fetchConCsrf('/api/mesa-partes/configuracion/formularios', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Formulario guardado satisfactoriamente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo guardar el formulario.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    // Publicar versión inmutable
    const btnPublicar = document.getElementById('btnPublicarFormulario');
    if (btnPublicar) {
        btnPublicar.addEventListener('click', async function () {
            const tramiteId = parseInt(document.getElementById('selectTramiteForm').value);
            if (!confirm('¿Confirma publicar oficialmente una nueva versión inmutable de este formulario?')) return;

            try {
                // Buscar el ID del formulario
                const resF = await fetch(`/api/mesa-partes/configuracion/formularios/tramite/${tramiteId}`);
                const jsonF = await resF.json();
                if (!jsonF.success || !jsonF.data) {
                    return alert('Debe guardar el formulario en borrador antes de publicarlo.');
                }

                const resP = await fetchConCsrf(`/api/mesa-partes/configuracion/formularios/${jsonF.data.id}/publicar`, {
                    method: 'POST'
                });
                const jsonP = await resP.json();
                if (resP.ok && jsonP.success) {
                    alert('Versión publicada oficialmente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (jsonP.message || 'No se pudo publicar la versión.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    // Previsualización Ciudadana
    const btnPreview = document.getElementById('btnPreviewFormulario');
    const modalPreview = document.getElementById('modalPrevisualizacion');
    const btnCerrarPreview = document.getElementById('btnCerrarModalPreview');

    if (btnCerrarPreview && modalPreview) {
        btnCerrarPreview.addEventListener('click', () => modalPreview.classList.add('hidden'));
    }

    if (btnPreview && modalPreview) {
        btnPreview.addEventListener('click', async function () {
            const tramiteId = parseInt(document.getElementById('selectTramiteForm').value);
            try {
                const resF = await fetch(`/api/mesa-partes/configuracion/formularios/tramite/${tramiteId}`);
                const jsonF = await resF.json();
                if (!jsonF.success || !jsonF.data) {
                    return alert('Guarde el formulario en borrador para previsualizarlo.');
                }

                const resPrev = await fetch(`/api/mesa-partes/configuracion/formularios/${jsonF.data.id}/preview`);
                const jsonPrev = await resPrev.json();
                if (jsonPrev.success && jsonPrev.data) {
                    renderizarPrevisualizacion(jsonPrev.data);
                    modalPreview.classList.remove('hidden');
                    inicializarIconos();
                }
            } catch (err) {
                alert('Error al generar previsualización: ' + err.message);
            }
        });
    }
}

function renderizarCamposEnCanvas() {
    const canvas = document.getElementById('canvasCampos');
    if (!canvas) return;

    if (camposFormulario.length === 0) {
        canvas.innerHTML = `
            <div id="canvasVacio" class="flex flex-col items-center justify-center p-12 border-2 border-dashed border-slate-700/80 rounded-2xl text-slate-400 text-center">
                <i data-lucide="mouse-pointer-click" class="w-10 h-10 mb-2 text-slate-500"></i>
                <p class="text-sm font-semibold">El formulario aún no contiene campos configurados.</p>
                <p class="text-xs text-slate-500 max-w-sm mt-1">Haga clic en cualquiera de los tipos de campo de la paleta izquierda.</p>
            </div>
        `;
        inicializarIconos();
        return;
    }

    let html = '';
    camposFormulario.forEach((c, idx) => {
        html += `
            <div class="itemCampoFormulario p-4 rounded-xl bg-slate-900/80 border border-slate-700/80 flex items-start justify-between gap-4" data-idx="${idx}">
                <div class="flex-1 space-y-1">
                    <div class="flex items-center space-x-2">
                        <span class="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-blue-500/10 text-blue-400 border border-blue-500/30">${c.tipoCampo}</span>
                        <span class="text-xs font-bold text-slate-100">${c.etiqueta}</span>
                        ${c.obligatorio ? '<span class="text-rose-400 font-bold text-[10px]">* Requerido</span>' : ''}
                    </div>
                    <div class="text-[11px] font-mono text-slate-400">Clave: ${c.nombreCampo}</div>
                    ${c.textoAyuda ? `<div class="text-[11px] text-slate-500">${c.textoAyuda}</div>` : ''}
                </div>
                <div class="flex items-center space-x-1">
                    <button type="button" class="btnMoverArriba p-1 text-slate-400 hover:text-white" onclick="moverCampoArriba(${idx})" title="Mover arriba">
                        <i data-lucide="arrow-up" class="w-4 h-4"></i>
                    </button>
                    <button type="button" class="btnMoverAbajo p-1 text-slate-400 hover:text-white" onclick="moverCampoAbajo(${idx})" title="Mover abajo">
                        <i data-lucide="arrow-down" class="w-4 h-4"></i>
                    </button>
                    <button type="button" class="btnEditarProp p-1.5 text-blue-400 hover:bg-blue-500/20 rounded" onclick="abrirModalPropiedades(${idx})" title="Propiedades">
                        <i data-lucide="sliders" class="w-4 h-4"></i>
                    </button>
                    <button type="button" class="btnEliminarCampo p-1.5 text-rose-400 hover:bg-rose-500/20 rounded" onclick="eliminarCampo(${idx})" title="Eliminar">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </div>
            </div>
        `;
    });

    canvas.innerHTML = html;
    inicializarIconos();
}

window.moverCampoArriba = function (idx) {
    if (idx > 0) {
        const aux = camposFormulario[idx];
        camposFormulario[idx] = camposFormulario[idx - 1];
        camposFormulario[idx - 1] = aux;
        camposFormulario[idx].orden = idx + 1;
        camposFormulario[idx - 1].orden = idx;
        renderizarCamposEnCanvas();
    }
};

window.moverCampoAbajo = function (idx) {
    if (idx < camposFormulario.length - 1) {
        const aux = camposFormulario[idx];
        camposFormulario[idx] = camposFormulario[idx + 1];
        camposFormulario[idx + 1] = aux;
        camposFormulario[idx].orden = idx + 1;
        camposFormulario[idx + 1].orden = idx + 2;
        renderizarCamposEnCanvas();
    }
};

window.eliminarCampo = function (idx) {
    camposFormulario.splice(idx, 1);
    camposFormulario.forEach((c, i) => c.orden = i + 1);
    renderizarCamposEnCanvas();
};

window.abrirModalPropiedades = function (idx) {
    const c = camposFormulario[idx];
    if (!c) return;

    document.getElementById('propCampoIdx').value = idx;
    document.getElementById('propCampoTipo').value = c.tipoCampo;
    document.getElementById('propCampoNombre').value = c.nombreCampo;
    document.getElementById('propCampoEtiqueta').value = c.etiqueta;
    document.getElementById('propCampoAyuda').value = c.textoAyuda || '';
    document.getElementById('propCampoPlaceholder').value = c.placeholder || '';
    document.getElementById('propCampoLongitudMax').value = c.longitudMaxima || 250;
    document.getElementById('propCampoRegex').value = c.expresionRegular || '';
    document.getElementById('propCampoObligatorio').checked = !!c.obligatorio;

    const seccionOpciones = document.getElementById('seccionOpcionesSeleccion');
    if (c.tipoCampo === 'SELECCION') {
        seccionOpciones.classList.remove('hidden');
        if (c.opciones && c.opciones.length > 0) {
            document.getElementById('propCampoOpcionesText').value = c.opciones.map(o => `${o.valor}: ${o.etiqueta}`).join('\n');
        } else {
            document.getElementById('propCampoOpcionesText').value = 'OPC1: Opción 1\nOPC2: Opción 2';
        }
    } else {
        seccionOpciones.classList.add('hidden');
    }

    document.getElementById('modalPropiedadesCampo').classList.remove('hidden');
    inicializarIconos();
};

// Cerrar y guardar propiedades
const btnCerrarProp = document.getElementById('btnCerrarModalPropiedades');
const btnCancProp = document.getElementById('btnCancelarPropiedades');
const formProp = document.getElementById('formPropiedadesCampo');

if (btnCerrarProp) btnCerrarProp.addEventListener('click', () => document.getElementById('modalPropiedadesCampo').classList.add('hidden'));
if (btnCancProp) btnCancProp.addEventListener('click', () => document.getElementById('modalPropiedadesCampo').classList.add('hidden'));

if (formProp) {
    formProp.addEventListener('submit', function (e) {
        e.preventDefault();
        const idx = parseInt(document.getElementById('propCampoIdx').value);
        const c = camposFormulario[idx];
        if (!c) return;

        c.nombreCampo = document.getElementById('propCampoNombre').value.trim().toLowerCase();
        c.etiqueta = document.getElementById('propCampoEtiqueta').value.trim();
        c.textoAyuda = document.getElementById('propCampoAyuda').value.trim() || null;
        c.placeholder = document.getElementById('propCampoPlaceholder').value.trim() || null;
        c.longitudMaxima = parseInt(document.getElementById('propCampoLongitudMax').value) || null;
        c.expresionRegular = document.getElementById('propCampoRegex').value.trim() || null;
        c.obligatorio = document.getElementById('propCampoObligatorio').checked;

        if (c.tipoCampo === 'SELECCION') {
            const rawOpc = document.getElementById('propCampoOpcionesText').value.trim().split('\n');
            c.opciones = [];
            rawOpc.forEach((linea, ord) => {
                if (linea.includes(':')) {
                    const [val, lbl] = linea.split(':');
                    c.opciones.push({ valor: val.trim(), etiqueta: lbl.trim(), orden: ord + 1 });
                } else if (linea.trim()) {
                    c.opciones.push({ valor: linea.trim(), etiqueta: linea.trim(), orden: ord + 1 });
                }
            });
        }

        document.getElementById('modalPropiedadesCampo').classList.add('hidden');
        renderizarCamposEnCanvas();
    });
}

function renderizarPrevisualizacion(esquema) {
    const contenedor = document.getElementById('contenedorPreviewCampos');
    if (!contenedor || !esquema) return;

    document.getElementById('previewTitulo').innerText = esquema.nombre || 'Vista Previa';

    let html = '';
    if (esquema.formulario && esquema.formulario.campos) {
        esquema.formulario.campos.forEach(c => {
            html += `<div class="space-y-1.5">`;
            html += `<label class="block font-semibold text-slate-200 text-xs">${c.etiqueta} ${c.obligatorio ? '<span class="text-rose-400">*</span>' : ''}</label>`;

            if (c.tipoCampo === 'TEXTO' || c.tipoCampo === 'CORREO' || c.tipoCampo === 'NUMERO' || c.tipoCampo === 'FECHA') {
                const tipoInput = c.tipoCampo === 'CORREO' ? 'email' : (c.tipoCampo === 'NUMERO' ? 'number' : (c.tipoCampo === 'FECHA' ? 'date' : 'text'));
                html += `<input type="${tipoInput}" placeholder="${c.placeholder || ''}" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-300 text-xs focus:outline-none">`;
            } else if (c.tipoCampo === 'TEXTO_LARGO') {
                html += `<textarea rows="3" placeholder="${c.placeholder || ''}" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-300 text-xs focus:outline-none"></textarea>`;
            } else if (c.tipoCampo === 'SELECCION') {
                html += `<select class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-300 text-xs focus:outline-none">`;
                html += `<option value="">Seleccione una opción...</option>`;
                if (c.opciones) {
                    c.opciones.forEach(o => {
                        html += `<option value="${o.valor}">${o.etiqueta}</option>`;
                    });
                }
                html += `</select>`;
            } else if (c.tipoCampo === 'CHECKBOX') {
                html += `
                    <label class="flex items-center space-x-2 text-slate-300 text-xs">
                        <input type="checkbox" class="rounded border-slate-700 text-blue-600">
                        <span>${c.placeholder || 'Confirmo y acepto'}</span>
                    </label>
                `;
            } else if (c.tipoCampo === 'ARCHIVO') {
                html += `<input type="file" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-slate-300 text-xs">`;
            }

            if (c.mensajeAyuda) {
                html += `<div class="text-[10px] text-slate-400">${c.mensajeAyuda}</div>`;
            }
            html += `</div>`;
        });
    } else {
        html = '<div class="text-slate-400 py-4 text-center">No hay campos en el formulario dinámico.</div>';
    }

    contenedor.innerHTML = html;
}

/* =========================================================================
 * 4. MÓDULO DE PLAZOS Y CALENDARIO
 * ========================================================================= */
function inicializarPlazos() {
    const modalPlazo = document.getElementById('modalAjustarPlazo');
    const formPlazo = document.getElementById('formAjustarPlazo');
    const btnCerrar = document.getElementById('btnCerrarModalPlazo');
    const btnCancelar = document.getElementById('btnCancelarPlazo');

    if (btnCerrar && modalPlazo) btnCerrar.addEventListener('click', () => modalPlazo.classList.add('hidden'));
    if (btnCancelar && modalPlazo) btnCancelar.addEventListener('click', () => modalPlazo.classList.add('hidden'));

    document.querySelectorAll('.btnEditarPlazo').forEach(btn => {
        btn.addEventListener('click', function () {
            const id = this.getAttribute('data-id');
            const codigo = this.getAttribute('data-codigo');
            const nombre = this.getAttribute('data-nombre');
            const plazo = this.getAttribute('data-plazo');
            const unidad = this.getAttribute('data-unidad');
            const norma = this.getAttribute('data-norma');

            document.getElementById('plazoTramiteId').value = id;
            document.getElementById('plazoTramiteNombre').innerText = `${codigo} — ${nombre}`;
            document.getElementById('inputPlazoDias').value = plazo;
            document.getElementById('selectPlazoUnidad').value = unidad;
            document.getElementById('inputPlazoNorma').value = norma || '';
            document.getElementById('inputPlazoMotivo').value = '';

            modalPlazo.classList.remove('hidden');
            inicializarIconos();
        });
    });

    if (formPlazo) {
        formPlazo.addEventListener('submit', async function (e) {
            e.preventDefault();
            const id = document.getElementById('plazoTramiteId').value;
            const dias = parseInt(document.getElementById('inputPlazoDias').value);
            const unidad = document.getElementById('selectPlazoUnidad').value;
            const norma = document.getElementById('inputPlazoNorma').value.trim();
            const motivo = document.getElementById('inputPlazoMotivo').value.trim();

            try {
                const url = `/api/mesa-partes/configuracion/plazos/${id}?dias=${dias}&unidad=${unidad}&baseNormativa=${encodeURIComponent(norma)}&motivo=${encodeURIComponent(motivo)}`;
                const res = await fetchConCsrf(url, { method: 'PUT' });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Plazo legal actualizado correctamente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo actualizar el plazo.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }
}

function inicializarCalendario() {
    const modalFeriado = document.getElementById('modalFeriado');
    const formFeriado = document.getElementById('formFeriado');
    const btnNuevo = document.getElementById('btnNuevoFeriado');
    const btnCerrar = document.getElementById('btnCerrarModalFeriado');
    const btnCancelar = document.getElementById('btnCancelarFeriado');

    if (btnNuevo && modalFeriado) {
        btnNuevo.addEventListener('click', () => {
            formFeriado.reset();
            modalFeriado.classList.remove('hidden');
            inicializarIconos();
        });
    }

    if (btnCerrar && modalFeriado) btnCerrar.addEventListener('click', () => modalFeriado.classList.add('hidden'));
    if (btnCancelar && modalFeriado) btnCancelar.addEventListener('click', () => modalFeriado.classList.add('hidden'));

    if (formFeriado) {
        formFeriado.addEventListener('submit', async function (e) {
            e.preventDefault();
            const payload = {
                fecha: document.getElementById('inputFeriadoFecha').value,
                nombre: document.getElementById('inputFeriadoNombre').value.trim(),
                tipo: document.getElementById('selectFeriadoTipo').value,
                baseNormativa: document.getElementById('inputFeriadoNorma').value.trim() || null,
                esLaborable: document.getElementById('checkEsLaborable').checked,
                aplicaTodosTramites: true,
                activo: true
            };

            try {
                const res = await fetchConCsrf('/api/mesa-partes/configuracion/calendario', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Fecha guardada correctamente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo registrar la fecha.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    document.querySelectorAll('.btnToggleCalendario').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            const activo = this.getAttribute('data-activo') === 'true';
            if (!confirm(`¿Confirma ${activo ? 'activar' : 'desactivar'} esta fecha en el cómputo oficial?`)) return;

            try {
                const res = await fetchConCsrf(`/api/mesa-partes/configuracion/calendario/${id}/estado?activo=${activo}`, {
                    method: 'PUT'
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo modificar el estado.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    });
}

/* =========================================================================
 * 5. MÓDULO DE PLANTILLAS DOCUMENTALES
 * ========================================================================= */
function inicializarPlantillas() {
    const modalPlantilla = document.getElementById('modalPlantilla');
    const formPlantilla = document.getElementById('formPlantilla');
    const btnNueva = document.getElementById('btnNuevaPlantilla');
    const btnCerrar = document.getElementById('btnCerrarModalPlantilla');
    const btnCancelar = document.getElementById('btnCancelarPlantilla');

    if (btnNueva && modalPlantilla) {
        btnNueva.addEventListener('click', () => {
            document.getElementById('modalPlantillaTitulo').innerText = 'Nueva Plantilla Documental';
            formPlantilla.reset();
            document.getElementById('plantillaId').value = '';
            document.getElementById('plantillaCodigo').readOnly = false;
            modalPlantilla.classList.remove('hidden');
            inicializarIconos();
        });
    }

    if (btnCerrar && modalPlantilla) btnCerrar.addEventListener('click', () => modalPlantilla.classList.add('hidden'));
    if (btnCancelar && modalPlantilla) btnCancelar.addEventListener('click', () => modalPlantilla.classList.add('hidden'));

    document.querySelectorAll('.btnEditarPlantilla').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            try {
                const res = await fetch(`/api/mesa-partes/configuracion/plantillas/${id}`);
                const json = await res.json();
                if (json.success && json.data) {
                    const p = json.data;
                    document.getElementById('modalPlantillaTitulo').innerText = 'Editar Plantilla: ' + p.codigo;
                    document.getElementById('plantillaId').value = p.id;
                    document.getElementById('plantillaCodigo').value = p.codigo;
                    document.getElementById('plantillaCodigo').readOnly = true;
                    document.getElementById('plantillaTipo').value = p.tipoDocumental;
                    document.getElementById('plantillaNombre').value = p.nombre;
                    document.getElementById('plantillaDescripcion').value = p.descripcion || '';
                    document.getElementById('plantillaCuerpo').value = p.cuerpoPlantilla;

                    modalPlantilla.classList.remove('hidden');
                    inicializarIconos();
                }
            } catch (err) {
                alert('Error al consultar plantilla: ' + err.message);
            }
        });
    });

    if (formPlantilla) {
        formPlantilla.addEventListener('submit', async function (e) {
            e.preventDefault();
            const id = document.getElementById('plantillaId').value;
            const esEdicion = !!id;

            const payload = {
                codigo: document.getElementById('plantillaCodigo').value.trim().toUpperCase(),
                tipoDocumental: document.getElementById('plantillaTipo').value,
                nombre: document.getElementById('plantillaNombre').value.trim(),
                descripcion: document.getElementById('plantillaDescripcion').value.trim() || null,
                cuerpoPlantilla: document.getElementById('plantillaCuerpo').value
            };

            const url = esEdicion ? `/api/mesa-partes/configuracion/plantillas/${id}` : '/api/mesa-partes/configuracion/plantillas';
            const method = esEdicion ? 'PUT' : 'POST';

            try {
                const res = await fetchConCsrf(url, {
                    method: method,
                    body: JSON.stringify(payload)
                });
                const json = await res.json();
                if (res.ok && json.success) {
                    alert('Plantilla guardada y versionada correctamente.');
                    window.location.reload();
                } else {
                    alert('Error: ' + (json.message || 'No se pudo guardar la plantilla.'));
                }
            } catch (err) {
                alert('Error de conexión: ' + err.message);
            }
        });
    }

    // Previsualización de sustitución de variables
    const modalPrevPlantilla = document.getElementById('modalPreviewPlantilla');
    const btnCerrarPrevPlantilla = document.getElementById('btnCerrarPreviewPlantilla');
    const contenedorTextoPrev = document.getElementById('contenedorTextoPreviewPlantilla');

    if (btnCerrarPrevPlantilla && modalPrevPlantilla) {
        btnCerrarPrevPlantilla.addEventListener('click', () => modalPrevPlantilla.classList.add('hidden'));
    }

    document.querySelectorAll('.btnPreviewPlantilla').forEach(btn => {
        btn.addEventListener('click', async function () {
            const id = this.getAttribute('data-id');
            try {
                const res = await fetchConCsrf(`/api/mesa-partes/configuracion/plantillas/${id}/preview`, {
                    method: 'POST',
                    body: JSON.stringify({
                        numero_expediente: 'MPV-2026-000125',
                        fecha: '16 de Octubre de 2026, 11:30 hrs',
                        solicitante_nombre: 'JUAN HUAMÁN QUISPE',
                        solicitante_doc: 'DNI 45892314',
                        asunto: 'SOLICITUD DE CONSTANCIA DE POSESIÓN URBANA',
                        oficina: 'Subgerencia de Desarrollo Urbano y Rural',
                        plazo_dias: '30',
                        codigo_seguimiento: 'C5A2-B91F-78E2',
                        resultado_final: 'PROCEDENTE',
                        detalle_observacion: 'Falta adjuntar copia simple de plano perimétrico visado',
                        fecha_limite: '20 de Octubre de 2026'
                    })
                });
                const json = await res.json();
                if (json.success && json.data) {
                    contenedorTextoPrev.innerText = json.data;
                    modalPrevPlantilla.classList.remove('hidden');
                    inicializarIconos();
                }
            } catch (err) {
                alert('Error al previsualizar plantilla: ' + err.message);
            }
        });
    });
}
