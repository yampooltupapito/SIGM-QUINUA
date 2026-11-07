/**
 * SIGM-QUINUA - Mesa de Partes Virtual
 * Controlador del Formulario Multi-Paso de Solicitud Ciudadana (solicitud-form.js)
 */
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    let pasoActual = 1;
    const totalPasos = 5;

    // Estado en memoria
    let expedienteUuid = null;
    let tipoTramiteSeleccionado = null;
    let esquemaTramiteActual = null;
    let idempotencyKey = null;

    // Inicializar Idempotency Key
    function obtenerOInicializarIdempotencyKey() {
        if (!idempotencyKey) {
            idempotencyKey = (window.crypto && crypto.randomUUID) 
                ? crypto.randomUUID() 
                : 'IDEM-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
        }
        return idempotencyKey;
    }

    // Elementos DOM
    const stepperItems = document.querySelectorAll('.stepper-step');
    const pasoContainers = document.querySelectorAll('.paso-contenido');
    const btnAnterior = document.getElementById('btnPasoAnterior');
    const btnSiguiente = document.getElementById('btnPasoSiguiente');
    const btnGuardarBorrador = document.getElementById('btnGuardarBorrador');
    const btnPresentarFinal = document.getElementById('btnPresentarFinal');
    const contenedorCamposDinamicos = document.getElementById('contenedorCamposDinamicos');
    const contenedorRequisitos = document.getElementById('contenedorRequisitos');

    // Inicializar desde borrador existente si viene en la URL
    const urlParams = new URLSearchParams(window.location.search);
    const paramBorrador = urlParams.get('borrador');
    const paramTramite = urlParams.get('tramiteId');

    if (paramBorrador) {
        expedienteUuid = paramBorrador;
        cargarBorradorExistente(paramBorrador);
    } else if (paramTramite) {
        seleccionarTramitePorId(paramTramite);
    }

    // Configuración de CSRF para fetch
    function getCsrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (token) headers[header] = token;
        return headers;
    }

    // ==========================================
    // NAVEGACIÓN ENTRE PASOS (STEPPER)
    // ==========================================
    function actualizarVisualizacionPaso() {
        pasoContainers.forEach(container => {
            const paso = parseInt(container.getAttribute('data-paso'), 10);
            if (paso === pasoActual) {
                container.classList.remove('hidden');
            } else {
                container.classList.add('hidden');
            }
        });

        stepperItems.forEach(item => {
            const paso = parseInt(item.getAttribute('data-paso'), 10);
            const badge = item.querySelector('.step-badge');
            const label = item.querySelector('.step-label');

            if (paso < pasoActual) {
                // Completado
                badge.className = 'step-badge w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-emerald-600 text-white shadow-sm';
                badge.innerHTML = '<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>';
                if (label) label.className = 'step-label text-xs font-semibold text-emerald-800 hidden sm:inline';
            } else if (paso === pasoActual) {
                // Activo
                badge.className = 'step-badge w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-blue-700 text-white ring-4 ring-blue-100 shadow-sm';
                badge.textContent = paso;
                if (label) label.className = 'step-label text-xs font-bold text-blue-900 hidden sm:inline';
            } else {
                // Pendiente
                badge.className = 'step-badge w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-slate-100 text-slate-400 border border-slate-200';
                badge.textContent = paso;
                if (label) label.className = 'step-label text-xs font-medium text-slate-400 hidden sm:inline';
            }
        });

        // Botones de acción
        if (btnAnterior) {
            btnAnterior.style.display = (pasoActual === 1) ? 'none' : 'inline-flex';
        }
        if (btnSiguiente) {
            btnSiguiente.style.display = (pasoActual === totalPasos) ? 'none' : 'inline-flex';
        }
        if (btnPresentarFinal) {
            btnPresentarFinal.style.display = (pasoActual === totalPasos) ? 'inline-flex' : 'none';
        }
        if (btnGuardarBorrador) {
            btnGuardarBorrador.style.display = (pasoActual >= 2 && pasoActual <= 4) ? 'inline-flex' : 'none';
        }

        window.scrollTo({ top: 0, behavior: 'smooth' });
        if (window.lucide) window.lucide.createIcons();
    }

    if (btnAnterior) {
        btnAnterior.addEventListener('click', function() {
            if (pasoActual > 1) {
                pasoActual--;
                actualizarVisualizacionPaso();
            }
        });
    }

    if (btnSiguiente) {
        btnSiguiente.addEventListener('click', async function() {
            if (validarPasoActual()) {
                if (pasoActual === 3) {
                    // Guardar automáticamente borrador antes de pasar a documentos
                    try {
                        mostrarNotificacion('Guardando borrador...', 'info');
                        await guardarBorradorEnServidor(false);
                    } catch (e) {
                        mostrarNotificacion(e.message, 'error');
                        return;
                    }
                } else if (pasoActual === 4) {
                    // Validar requisitos documentales obligatorios antes de ir a revisión
                    if (!validarRequisitosDocumentales()) {
                        return;
                    }
                    generarResumenRevision();
                }

                if (pasoActual < totalPasos) {
                    pasoActual++;
                    actualizarVisualizacionPaso();
                }
            }
        });
    }

    if (btnGuardarBorrador) {
        btnGuardarBorrador.addEventListener('click', async function() {
            try {
                btnGuardarBorrador.disabled = true;
                btnGuardarBorrador.classList.add('opacity-50');
                await guardarBorradorEnServidor(true);
                mostrarNotificacion('Borrador guardado exitosamente.', 'success');
            } catch (err) {
                mostrarNotificacion(err.message, 'error');
            } finally {
                btnGuardarBorrador.disabled = false;
                btnGuardarBorrador.classList.remove('opacity-50');
            }
        });
    }

    // ==========================================
    // VALIDACIÓN POR PASO
    // ==========================================
    function validarPasoActual() {
        const contenedor = document.querySelector(`.paso-contenido[data-paso="${pasoActual}"]`);
        if (!contenedor) return true;
        window.SolicitudValidacion.limpiarErrores(contenedor);

        if (pasoActual === 1) {
            if (!tipoTramiteSeleccionado) {
                mostrarNotificacion('Debe seleccionar un tipo de trámite para continuar.', 'warning');
                return false;
            }
            return true;
        }

        if (pasoActual === 2) {
            // Datos del solicitante verificados
            return true;
        }

        if (pasoActual === 3) {
            let esValido = true;
            const inputAsunto = document.getElementById('inputAsunto');
            const inputDesc = document.getElementById('inputDescripcion');

            const errAsunto = window.SolicitudValidacion.validarTexto(inputAsunto.value, 5, 255, true, 'Asunto');
            if (errAsunto) {
                window.SolicitudValidacion.mostrarError(inputAsunto, errAsunto);
                esValido = false;
            }

            const errDesc = window.SolicitudValidacion.validarTexto(inputDesc.value, 10, 4000, true, 'Descripción');
            if (errDesc) {
                window.SolicitudValidacion.mostrarError(inputDesc, errDesc);
                esValido = false;
            }

            // Validar campos dinámicos
            if (esquemaTramiteActual && esquemaTramiteActual.formulario && esquemaTramiteActual.formulario.campos) {
                esquemaTramiteActual.formulario.campos.forEach(campo => {
                    const inputCampo = document.querySelector(`[name="campo_${campo.nombreCampo}"]`);
                    if (inputCampo) {
                        let errCampo = null;
                        const val = inputCampo.value;

                        if (campo.tipoCampo === 'NUMERO') {
                            errCampo = window.SolicitudValidacion.validarNumero(val, campo.obligatorio, campo.etiqueta);
                        } else if (campo.tipoCampo === 'FECHA') {
                            errCampo = window.SolicitudValidacion.validarFecha(val, campo.obligatorio, campo.etiqueta);
                        } else {
                            errCampo = window.SolicitudValidacion.validarTexto(val, null, campo.longitudMaxima, campo.obligatorio, campo.etiqueta);
                        }

                        if (errCampo) {
                            window.SolicitudValidacion.mostrarError(inputCampo, errCampo);
                            esValido = false;
                        }
                    }
                });
            }

            return esValido;
        }

        return true;
    }

    function validarRequisitosDocumentales() {
        if (!esquemaTramiteActual || !esquemaTramiteActual.requisitos) return true;

        const obligatorios = esquemaTramiteActual.requisitos.filter(r => r.obligatorio);
        if (obligatorios.length > 0 && window.SolicitudDocumentos.documentosAdjuntos.length === 0) {
            mostrarNotificacion('Debe adjuntar al menos un documento para cumplir los requisitos obligatorios del trámite.', 'warning');
            return false;
        }
        return true;
    }

    // ==========================================
    // SELECCIÓN Y CARGA DE TRÁMITES
    // ==========================================
    const radiosTramite = document.querySelectorAll('.radio-tipo-tramite');
    radiosTramite.forEach(radio => {
        radio.addEventListener('change', function() {
            seleccionarTramitePorId(this.value);
        });
    });

    async function seleccionarTramitePorId(tramiteId) {
        try {
            mostrarCargando(true);
            const resp = await fetch(`/api/mesa-partes/tipos-tramite/${tramiteId}/formulario`);
            if (!resp.ok) throw new Error('No fue posible cargar la configuración del tipo de trámite.');

            const apiData = await resp.json();
            esquemaTramiteActual = apiData.data;
            tipoTramiteSeleccionado = esquemaTramiteActual.tipoTramiteId;

            // Actualizar tarjetas visuales de selección
            document.querySelectorAll('.card-tipo-tramite').forEach(card => {
                const id = card.getAttribute('data-tramite-id');
                if (id === String(tramiteId)) {
                    card.classList.add('border-blue-600', 'bg-blue-50/40', 'ring-2', 'ring-blue-500');
                    card.classList.remove('border-slate-200');
                    const r = card.querySelector('input[type="radio"]');
                    if (r) r.checked = true;
                } else {
                    card.classList.remove('border-blue-600', 'bg-blue-50/40', 'ring-2', 'ring-blue-500');
                    card.classList.add('border-slate-200');
                }
            });

            renderizarRequisitosDocumentales(esquemaTramiteActual.requisitos);
            renderizarCamposDinamicos(esquemaTramiteActual.formulario);
        } catch (e) {
            mostrarNotificacion(e.message, 'error');
        } finally {
            mostrarCargando(false);
        }
    }

    function renderizarRequisitosDocumentales(requisitos) {
        if (!contenedorRequisitos) return;
        if (!requisitos || requisitos.length === 0) {
            contenedorRequisitos.innerHTML = `
                <div class="p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-500">
                    Este trámite no tiene requisitos documentales específicos preconfigurados.
                </div>
            `;
            return;
        }

        let html = '<div class="space-y-3">';
        requisitos.forEach(req => {
            const badge = req.obligatorio
                ? '<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-700 uppercase">Obligatorio</span>'
                : '<span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-100 text-slate-600">Opcional</span>';

            html += `
                <div class="p-3.5 rounded-xl border border-slate-200 bg-slate-50/70 hover:bg-slate-50 transition-colors">
                    <div class="flex items-start justify-between gap-3">
                        <div class="space-y-1">
                            <div class="flex items-center gap-2">
                                <h4 class="text-xs font-bold text-slate-900">${req.nombre}</h4>
                                ${badge}
                            </div>
                            <p class="text-[11px] text-slate-600 leading-relaxed">${req.descripcion || ''}</p>
                            <p class="text-[10px] text-slate-400">Formatos permitidos: ${req.formatosPermitidos} | Máx: ${req.tamanoMaximoMb} MB</p>
                        </div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        contenedorRequisitos.innerHTML = html;
    }

    function renderizarCamposDinamicos(formulario) {
        if (!contenedorCamposDinamicos) return;
        if (!formulario || !formulario.campos || formulario.campos.length === 0) {
            contenedorCamposDinamicos.innerHTML = '';
            contenedorCamposDinamicos.classList.add('hidden');
            return;
        }

        contenedorCamposDinamicos.classList.remove('hidden');
        let html = `
            <div class="border-t border-slate-200 pt-6 mt-6 space-y-4">
                <div class="flex items-center gap-2">
                    <div class="w-1.5 h-4 bg-blue-600 rounded-full"></div>
                    <h3 class="text-sm font-bold text-slate-900">Información Específica del Trámite (${formulario.nombreFormulario})</h3>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        `;

        formulario.campos.forEach(campo => {
            const reqMark = campo.obligatorio ? '<span class="text-red-500 font-bold">*</span>' : '';
            const helpText = campo.mensajeAyuda ? `<p class="text-[11px] text-slate-400 mt-1">${campo.mensajeAyuda}</p>` : '';
            const fullCol = (campo.tipoCampo === 'TEXTO_LARGO' || campo.longitudMaxima > 100) ? 'md:col-span-2' : '';

            html += `<div class="space-y-1.5 ${fullCol}">
                <label class="block text-xs font-semibold text-slate-700">
                    ${campo.etiqueta} ${reqMark}
                </label>
            `;

            if (campo.tipoCampo === 'TEXTO_LARGO') {
                html += `
                    <textarea name="campo_${campo.nombreCampo}" rows="3" placeholder="${campo.placeholder || ''}"
                              class="w-full text-xs rounded-xl border border-slate-300 px-3 py-2.5 focus:border-blue-600 focus:ring-1 focus:ring-blue-600 outline-none transition-colors"></textarea>
                `;
            } else if (campo.tipoCampo === 'SELECCION') {
                html += `
                    <select name="campo_${campo.nombreCampo}" class="w-full text-xs rounded-xl border border-slate-300 px-3 py-2.5 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600 outline-none transition-colors">
                        <option value="">-- Seleccione una opción --</option>
                `;
                if (campo.opciones) {
                    campo.opciones.forEach(op => {
                        html += `<option value="${op.valor}">${op.etiqueta}</option>`;
                    });
                }
                html += `</select>`;
            } else if (campo.tipoCampo === 'CHECKBOX') {
                html += `
                    <div class="flex items-center gap-2 pt-2">
                        <input type="checkbox" name="campo_${campo.nombreCampo}" value="true" class="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500">
                        <span class="text-xs text-slate-700">${campo.placeholder || campo.etiqueta}</span>
                    </div>
                `;
            } else if (campo.tipoCampo === 'FECHA') {
                html += `
                    <input type="date" name="campo_${campo.nombreCampo}"
                           class="w-full text-xs rounded-xl border border-slate-300 px-3 py-2.5 focus:border-blue-600 focus:ring-1 focus:ring-blue-600 outline-none transition-colors">
                `;
            } else {
                html += `
                    <input type="${campo.tipoCampo === 'NUMERO' ? 'number' : 'text'}" name="campo_${campo.nombreCampo}"
                           placeholder="${campo.placeholder || ''}"
                           class="w-full text-xs rounded-xl border border-slate-300 px-3 py-2.5 focus:border-blue-600 focus:ring-1 focus:ring-blue-600 outline-none transition-colors">
                `;
            }

            html += `${helpText}</div>`;
        });

        html += `</div></div>`;
        contenedorCamposDinamicos.innerHTML = html;
    }

    // ==========================================
    // CARGA DE ARCHIVOS ADJUNTOS
    // ==========================================
    const inputArchivo = document.getElementById('inputArchivoAdjunto');
    const selectTipoDoc = document.getElementById('selectTipoRelacionDoc');
    const dropzone = document.getElementById('dropzoneArchivos');

    if (inputArchivo) {
        inputArchivo.addEventListener('change', async function() {
            if (this.files && this.files.length > 0) {
                await procesarSubidaArchivo(this.files[0]);
                this.value = '';
            }
        });
    }

    if (dropzone) {
        ['dragenter', 'dragover'].forEach(eventName => {
            dropzone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                dropzone.classList.add('border-blue-500', 'bg-blue-50/50');
            });
        });
        ['dragleave', 'drop'].forEach(eventName => {
            dropzone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                dropzone.classList.remove('border-blue-500', 'bg-blue-50/50');
            });
        });
        dropzone.addEventListener('drop', async function(e) {
            const files = e.dataTransfer.files;
            if (files && files.length > 0) {
                await procesarSubidaArchivo(files[0]);
            }
        });
    }

    async function procesarSubidaArchivo(file) {
        // Validar primero si tenemos borrador activo; si no, guardar borrador primero
        if (!expedienteUuid) {
            try {
                mostrarNotificacion('Creando registro de borrador antes de adjuntar...', 'info');
                await guardarBorradorEnServidor(false);
            } catch (err) {
                mostrarNotificacion('Debe completar el Asunto y Descripción antes de adjuntar archivos.', 'warning');
                return;
            }
        }

        const errorArchivo = window.SolicitudValidacion.validarArchivo(file, 'application/pdf,image/jpeg,image/png', 25);
        if (errorArchivo) {
            mostrarNotificacion(errorArchivo, 'error');
            return;
        }

        try {
            mostrarCargando(true);
            const tipoRel = selectTipoDoc ? selectTipoDoc.value : 'ANEXO';
            const resp = await window.SolicitudDocumentos.subirArchivo(expedienteUuid, file, tipoRel);
            
            // Actualizar lista en memoria
            if (resp.data && resp.data.documentos) {
                window.SolicitudDocumentos.documentosAdjuntos = resp.data.documentos;
            }
            window.SolicitudDocumentos.renderizarLista('listaDocumentosAdjuntos', eliminarArchivoHandler);
            mostrarNotificacion('Archivo adjuntado correctamente.', 'success');
        } catch (e) {
            mostrarNotificacion(e.message, 'error');
        } finally {
            mostrarCargando(false);
        }
    }

    async function eliminarArchivoHandler(docUuid) {
        if (!confirm('¿Confirma que desea retirar este documento de su solicitud?')) return;
        try {
            mostrarCargando(true);
            await window.SolicitudDocumentos.eliminarArchivo(expedienteUuid, docUuid);
            window.SolicitudDocumentos.documentosAdjuntos = window.SolicitudDocumentos.documentosAdjuntos.filter(d => (d.documentoUuid || d.uuid) !== docUuid);
            window.SolicitudDocumentos.renderizarLista('listaDocumentosAdjuntos', eliminarArchivoHandler);
            mostrarNotificacion('Documento retirado.', 'info');
        } catch (e) {
            mostrarNotificacion(e.message, 'error');
        } finally {
            mostrarCargando(false);
        }
    }

    // ==========================================
    // PERSISTENCIA DE BORRADOR
    // ==========================================
    async function guardarBorradorEnServidor(mostrarAlerta) {
        const inputAsunto = document.getElementById('inputAsunto');
        const inputDesc = document.getElementById('inputDescripcion');

        if (!tipoTramiteSeleccionado) {
            throw new Error('Seleccione un tipo de trámite.');
        }

        const asuntoVal = inputAsunto ? inputAsunto.value.trim() : 'Borrador sin asunto';
        const descVal = inputDesc ? inputDesc.value.trim() : '';

        // Recolectar valores dinámicos
        const valoresFormulario = {};
        if (esquemaTramiteActual && esquemaTramiteActual.formulario && esquemaTramiteActual.formulario.campos) {
            esquemaTramiteActual.formulario.campos.forEach(c => {
                const input = document.querySelector(`[name="campo_${c.nombreCampo}"]`);
                if (input) {
                    if (input.type === 'checkbox') {
                        valoresFormulario[c.nombreCampo] = input.checked ? 'true' : 'false';
                    } else {
                        valoresFormulario[c.nombreCampo] = input.value.trim();
                    }
                }
            });
        }

        const payload = {
            tipoTramiteId: tipoTramiteSeleccionado,
            asunto: asuntoVal.length > 0 ? asuntoVal : 'Borrador de Solicitud en Mesa de Partes',
            descripcion: descVal,
            valoresFormulario: valoresFormulario
        };

        const url = expedienteUuid
            ? `/api/mesa-partes/expedientes/borradores/${expedienteUuid}`
            : '/api/mesa-partes/expedientes/borradores';

        const method = expedienteUuid ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: getCsrfHeaders(),
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.message || 'Error al guardar el borrador.');
        }

        const data = await response.json();
        expedienteUuid = data.data.uuid;
        return data.data;
    }

    async function cargarBorradorExistente(uuid) {
        try {
            mostrarCargando(true);
            const resp = await fetch(`/api/mesa-partes/expedientes/borradores/${uuid}`);
            if (!resp.ok) throw new Error('No fue posible recuperar el borrador solicitado.');
            const data = await resp.json();
            const exp = data.data;

            if (exp.tipoTramite && exp.tipoTramite.id) {
                await seleccionarTramitePorId(exp.tipoTramite.id);
            }

            const inputAsunto = document.getElementById('inputAsunto');
            const inputDesc = document.getElementById('inputDescripcion');
            if (inputAsunto) inputAsunto.value = exp.asunto || '';
            if (inputDesc) inputDesc.value = exp.descripcion || '';

            if (exp.documentos) {
                window.SolicitudDocumentos.documentosAdjuntos = exp.documentos;
                window.SolicitudDocumentos.renderizarLista('listaDocumentosAdjuntos', eliminarArchivoHandler);
            }
        } catch (e) {
            mostrarNotificacion(e.message, 'error');
        } finally {
            mostrarCargando(false);
        }
    }

    // ==========================================
    // PASO 5: RESUMEN DE REVISIÓN
    // ==========================================
    function generarResumenRevision() {
        const contenedor = document.getElementById('contenedorResumenRevision');
        if (!contenedor) return;

        const inputAsunto = document.getElementById('inputAsunto');
        const inputDesc = document.getElementById('inputDescripcion');

        const nomTramite = esquemaTramiteActual ? esquemaTramiteActual.nombre : '-';
        const oficina = esquemaTramiteActual ? esquemaTramiteActual.oficinaResponsableNombre : '-';

        let html = `
            <div class="space-y-6">
                <!-- Tarjeta Principal del Trámite -->
                <div class="bg-blue-50/60 rounded-2xl p-5 border border-blue-200">
                    <span class="text-[10px] font-bold uppercase tracking-wider text-blue-700 bg-white px-2.5 py-0.5 rounded-full border border-blue-200">
                        Trámite Seleccionado
                    </span>
                    <h3 class="text-base font-bold text-slate-900 mt-2">${nomTramite}</h3>
                    <p class="text-xs text-slate-600 mt-1">Unidad Orgánica: <span class="font-semibold text-slate-800">${oficina}</span></p>
                </div>

                <!-- Detalle del Contenido -->
                <div class="bg-white rounded-2xl p-5 border border-slate-200 space-y-4 shadow-sm text-xs">
                    <div>
                        <span class="text-slate-400 font-semibold block text-[11px]">Asunto:</span>
                        <p class="font-bold text-slate-900 mt-0.5 text-sm">${inputAsunto ? inputAsunto.value : ''}</p>
                    </div>
                    <div>
                        <span class="text-slate-400 font-semibold block text-[11px]">Fundamentación / Descripción:</span>
                        <p class="text-slate-700 mt-0.5 whitespace-pre-line leading-relaxed">${inputDesc ? inputDesc.value : ''}</p>
                    </div>
                </div>

                <!-- Resumen de Documentos Adjuntos -->
                <div class="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm space-y-3">
                    <h4 class="text-xs font-bold text-slate-900 flex items-center justify-between">
                        <span>Documentos a Remitir</span>
                        <span class="px-2 py-0.5 rounded text-[10px] bg-slate-100 text-slate-600 font-semibold">
                            ${window.SolicitudDocumentos.documentosAdjuntos.length} archivo(s)
                        </span>
                    </h4>
                    <ul class="divide-y divide-slate-100 text-xs">
        `;

        if (window.SolicitudDocumentos.documentosAdjuntos.length === 0) {
            html += `<li class="py-2 text-slate-400 italic">No ha adjuntado archivos.</li>`;
        } else {
            window.SolicitudDocumentos.documentosAdjuntos.forEach(d => {
                html += `
                    <li class="py-2 flex items-center justify-between">
                        <span class="font-medium text-slate-800 flex items-center gap-1.5 truncate">
                            <i data-lucide="file-text" class="w-3.5 h-3.5 text-blue-600 flex-shrink-0"></i>
                            ${d.nombreOriginal || d.nombreArchivo}
                        </span>
                        <span class="text-[10px] text-slate-400 font-mono">${d.tipoRelacion || 'ANEXO'}</span>
                    </li>
                `;
            });
        }

        html += `
                    </ul>
                </div>
            </div>
        `;

        contenedor.innerHTML = html;
        if (window.lucide) window.lucide.createIcons();
    }

    // ==========================================
    // PRESENTACIÓN FINAL FORMAL
    // ==========================================
    if (btnPresentarFinal) {
        btnPresentarFinal.addEventListener('click', async function() {
            const checkVeracidad = document.getElementById('chkDeclaracionVeracidad');
            if (checkVeracidad && !checkVeracidad.checked) {
                mostrarNotificacion('Debe marcar la casilla de declaración jurada de veracidad para presentar la solicitud.', 'warning');
                return;
            }

            if (!confirm('¿Está seguro de presentar formalmente su solicitud a la Municipalidad Distrital de Quinua? Una vez enviada, se generará su número oficial de expediente.')) {
                return;
            }

            try {
                btnPresentarFinal.disabled = true;
                btnPresentarFinal.classList.add('opacity-50');
                mostrarCargando(true);

                // 1. Asegurar guardado previo de los últimos datos del formulario
                await guardarBorradorEnServidor(false);

                // 2. Enviar presentación formal con Idempotency Key
                const payload = {
                    claveIdempotencia: obtenerOInicializarIdempotencyKey(),
                    declaracionVeracidad: true
                };

                const response = await fetch(`/api/mesa-partes/expedientes/${expedienteUuid}/presentar`, {
                    method: 'POST',
                    headers: getCsrfHeaders(),
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const err = await response.json().catch(() => ({}));
                    throw new Error(err.message || 'Error al procesar la presentación formal.');
                }

                const data = await response.json();
                const expResp = data.data;

                // Redirigir a pantalla de confirmación institucional
                window.location.href = `/mesa-partes/confirmacion/${expResp.uuid}`;
            } catch (e) {
                mostrarNotificacion(e.message, 'error');
                btnPresentarFinal.disabled = false;
                btnPresentarFinal.classList.remove('opacity-50');
            } finally {
                mostrarCargando(false);
            }
        });
    }

    // Notificaciones emergentes accesibles
    function mostrarNotificacion(mensaje, tipo) {
        let toast = document.getElementById('mpvToast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'mpvToast';
            toast.className = 'fixed bottom-5 right-5 z-50 max-w-md px-4 py-3 rounded-2xl shadow-xl text-xs font-semibold flex items-center gap-3 transition-all duration-300 transform translate-y-10 opacity-0';
            document.body.appendChild(toast);
        }

        const estilos = {
            success: 'bg-emerald-600 text-white shadow-emerald-500/20',
            error: 'bg-red-600 text-white shadow-red-500/20',
            warning: 'bg-amber-500 text-white shadow-amber-500/20',
            info: 'bg-blue-600 text-white shadow-blue-500/20'
        };

        toast.className = `fixed bottom-5 right-5 z-50 max-w-md px-4 py-3 rounded-2xl shadow-xl text-xs font-semibold flex items-center gap-3 transition-all duration-300 transform ${estilos[tipo] || estilos.info}`;
        toast.innerHTML = `<span>${mensaje}</span>`;
        toast.style.transform = 'translateY(0)';
        toast.style.opacity = '1';

        setTimeout(() => {
            toast.style.transform = 'translateY(10px)';
            toast.style.opacity = '0';
        }, 4500);
    }

    function mostrarCargando(activo) {
        let spinner = document.getElementById('mpvLoadingOverlay');
        if (activo) {
            if (!spinner) {
                spinner = document.createElement('div');
                spinner.id = 'mpvLoadingOverlay';
                spinner.className = 'fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center';
                spinner.innerHTML = `
                    <div class="bg-white p-5 rounded-2xl shadow-2xl flex items-center gap-3 border border-slate-200">
                        <div class="w-6 h-6 border-3 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                        <span class="text-xs font-bold text-slate-800">Procesando solicitud...</span>
                    </div>
                `;
                document.body.appendChild(spinner);
            }
            spinner.classList.remove('hidden');
        } else if (spinner) {
            spinner.classList.add('hidden');
        }
    }

    actualizarVisualizacionPaso();
});
