/**
 * comerciantes-formulario.js — Lógica cliente del formulario dinámico de registro y edición.
 */

function getCsrfTokens() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    return {
        token: metaToken ? metaToken.getAttribute('content') : '',
        header: metaHeader ? metaHeader.getAttribute('content') : 'X-CSRF-TOKEN'
    };
}

let tipoActual = 'PERSONA_NATURAL';

function alternarTipoComerciante(tipo) {
    tipoActual = tipo;
    const secNatural = document.getElementById('seccionPersonaNatural');
    const secJuridica = document.getElementById('seccionPersonaJuridica');

    if (tipo === 'PERSONA_NATURAL') {
        if (secNatural) secNatural.classList.remove('hidden');
        if (secJuridica) secJuridica.classList.add('hidden');

        document.getElementById('naturalDni')?.setAttribute('required', 'required');
        document.getElementById('naturalNombres')?.setAttribute('required', 'required');
        document.getElementById('naturalPaterno')?.setAttribute('required', 'required');

        document.getElementById('juridicoRuc')?.removeAttribute('required');
        document.getElementById('juridicoRazonSocial')?.removeAttribute('required');
    } else {
        if (secNatural) secNatural.classList.add('hidden');
        if (secJuridica) secJuridica.classList.remove('hidden');

        document.getElementById('naturalDni')?.removeAttribute('required');
        document.getElementById('naturalNombres')?.removeAttribute('required');
        document.getElementById('naturalPaterno')?.removeAttribute('required');

        document.getElementById('juridicoRuc')?.setAttribute('required', 'required');
        document.getElementById('juridicoRazonSocial')?.setAttribute('required', 'required');
    }
}

async function validarDniOnline() {
    const dniInput = document.getElementById('naturalDni');
    const msg = document.getElementById('msgValidacionDni');
    if (!dniInput || !msg) return;

    const dni = dniInput.value.trim();
    if (dni.length !== 8 || !/^[0-9]+$/.test(dni)) {
        msg.textContent = 'Ingrese un DNI válido de 8 dígitos.';
        msg.className = 'text-[11px] mt-1 text-red-400 font-medium';
        return;
    }

    msg.textContent = 'Validando DNI...';
    msg.className = 'text-[11px] mt-1 text-blue-400';

    try {
        const resp = await fetch(`/api/mercados/comerciantes/verificar-dni?dni=${dni}`);
        const data = await resp.json();
        if (data.formatoValido) {
            msg.textContent = '✓ ' + data.mensaje;
            msg.className = 'text-[11px] mt-1 text-emerald-400 font-medium';
        } else {
            msg.textContent = '✗ ' + data.mensaje;
            msg.className = 'text-[11px] mt-1 text-red-400 font-medium';
        }
    } catch (e) {
        msg.textContent = 'No se pudo contactar el servicio de validación.';
        msg.className = 'text-[11px] mt-1 text-amber-400';
    }
}

async function validarRucOnline() {
    const rucInput = document.getElementById('juridicoRuc');
    const msg = document.getElementById('msgValidacionRuc');
    if (!rucInput || !msg) return;

    const ruc = rucInput.value.trim();
    if (ruc.length !== 11 || !/^[0-9]+$/.test(ruc)) {
        msg.textContent = 'Ingrese un RUC válido de 11 dígitos.';
        msg.className = 'text-[11px] mt-1 text-red-400 font-medium';
        return;
    }

    msg.textContent = 'Validando RUC (Algoritmo Módulo 11 SUNAT)...';
    msg.className = 'text-[11px] mt-1 text-blue-400';

    try {
        const resp = await fetch(`/api/mercados/comerciantes/verificar-ruc?ruc=${ruc}`);
        const data = await resp.json();
        if (data.formatoValido) {
            msg.textContent = '✓ ' + data.mensaje;
            msg.className = 'text-[11px] mt-1 text-emerald-400 font-medium';
        } else {
            msg.textContent = '✗ ' + data.mensaje;
            msg.className = 'text-[11px] mt-1 text-red-400 font-medium';
        }
    } catch (e) {
        msg.textContent = 'No se pudo contactar el servicio de validación.';
        msg.className = 'text-[11px] mt-1 text-amber-400';
    }
}

async function guardarComerciante(event) {
    event.preventDefault();
    const btn = document.getElementById('btnGuardar');
    const esEdicion = document.getElementById('esEdicion').value === 'true';
    const comercianteId = document.getElementById('comercianteId').value;
    const version = document.getElementById('version').value;

    const codigo = document.getElementById('codigoComerciante')?.value.trim();
    const observaciones = document.getElementById('observaciones')?.value.trim();

    // Obtener actividades seleccionadas
    const actividadesChecked = Array.from(document.querySelectorAll('input[name="actividadIds"]:checked'))
        .map(cb => parseInt(cb.value));

    const csrf = getCsrfTokens();
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = 'Procesando...';

    try {
        let endpoint = '';
        let method = 'POST';
        let payload = {};

        if (esEdicion) {
            endpoint = `/api/mercados/comerciantes/${comercianteId}`;
            method = 'PUT';
            payload = {
                codigoComerciante: codigo,
                observaciones: observaciones,
                actividadIds: actividadesChecked,
                version: parseInt(version)
            };
        } else {
            const estado = document.getElementById('estadoInicial')?.value || 'ACTIVO';

            if (tipoActual === 'PERSONA_NATURAL') {
                endpoint = '/api/mercados/comerciantes/natural';
                payload = {
                    numeroDocumento: document.getElementById('naturalDni')?.value.trim(),
                    nombres: document.getElementById('naturalNombres')?.value.trim(),
                    apellidoPaterno: document.getElementById('naturalPaterno')?.value.trim(),
                    apellidoMaterno: document.getElementById('naturalMaterno')?.value.trim(),
                    telefono: document.getElementById('naturalTelefono')?.value.trim(),
                    correo: document.getElementById('naturalCorreo')?.value.trim(),
                    direccion: document.getElementById('naturalDireccion')?.value.trim(),
                    codigoComerciante: codigo,
                    estado: estado,
                    observaciones: observaciones,
                    actividadIds: actividadesChecked
                };
            } else {
                endpoint = '/api/mercados/comerciantes/juridico';
                payload = {
                    ruc: document.getElementById('juridicoRuc')?.value.trim(),
                    razonSocial: document.getElementById('juridicoRazonSocial')?.value.trim(),
                    nombreComercial: document.getElementById('juridicoNombreComercial')?.value.trim(),
                    domicilioFiscal: document.getElementById('juridicoDomicilioFiscal')?.value.trim(),
                    telefono: document.getElementById('juridicoTelefono')?.value.trim(),
                    correo: document.getElementById('juridicoCorreo')?.value.trim(),
                    codigoComerciante: codigo,
                    estado: estado,
                    observaciones: observaciones,
                    actividadIds: actividadesChecked,
                    representanteDni: document.getElementById('repDni')?.value.trim(),
                    representanteNombres: document.getElementById('repNombres')?.value.trim(),
                    representanteApellidoPaterno: document.getElementById('repPaterno')?.value.trim(),
                    representanteTipo: document.getElementById('repTipo')?.value,
                    representanteFechaInicio: document.getElementById('repInicio')?.value || null
                };
            }
        }

        const response = await fetch(endpoint, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const result = await response.json();
            window.location.href = `/dashboard/mercados/comerciantes/${result.id}`;
        } else {
            const err = await response.json();
            alert('Error: ' + (err.message || 'No se pudo guardar el registro'));
        }
    } catch (e) {
        console.error('Error al guardar comerciante:', e);
        alert('Ocurrió un error inesperado al procesar la solicitud.');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}
