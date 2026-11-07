/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para formulario de creación y edición de Puestos Comerciales
 */
document.addEventListener('DOMContentLoaded', () => {
    initCalculoDimensiones();
    initFormularioPuesto();
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

function getCsrfData() {
    const token = document.querySelector('meta[name="_csrf"]')?.content ||
                  document.getElementById('csrf-token')?.value || '';
    const header = document.querySelector('meta[name="_csrf_header"]')?.content ||
                   document.getElementById('csrf-header')?.value || 'X-CSRF-TOKEN';
    return { token, header };
}

function initCalculoDimensiones() {
    const inputAncho = document.getElementById('anchoMetros');
    const inputLargo = document.getElementById('largoMetros');
    const inputArea = document.getElementById('areaMetrosCuadrados');

    if (!inputAncho || !inputLargo || !inputArea) return;

    function recalcular() {
        const ancho = parseFloat(inputAncho.value);
        const largo = parseFloat(inputLargo.value);
        if (!isNaN(ancho) && !isNaN(largo) && ancho > 0 && largo > 0) {
            // Solo auto-calcular si área está vacía o el usuario no la escribió manualmente
            const areaCalculada = (ancho * largo).toFixed(2);
            if (!inputArea.value || inputArea.dataset.autoCalculada === 'true') {
                inputArea.value = areaCalculada;
                inputArea.dataset.autoCalculada = 'true';
            }
        }
    }

    inputAncho.addEventListener('input', recalcular);
    inputLargo.addEventListener('input', recalcular);
    inputArea.addEventListener('input', () => {
        inputArea.dataset.autoCalculada = 'false';
    });
}

function initFormularioPuesto() {
    const form = document.getElementById('formPuestoComercial');
    if (!form) return;

    const esEdicion = document.getElementById('esEdicion').value === 'true';
    const mercadoId = document.getElementById('mercadoId').value;
    const btnGuardar = document.getElementById('btnGuardarPuesto');
    const alertaError = document.getElementById('alertaError');
    const mensajeErrorTexto = document.getElementById('mensajeErrorTexto');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertaError.classList.add('hidden');
        mensajeErrorTexto.textContent = '';
        btnGuardar.disabled = true;
        btnGuardar.textContent = 'Guardando...';

        const { token, header } = getCsrfData();
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) headers[header] = token;

        try {
            if (esEdicion) {
                const puestoId = document.getElementById('puestoId').value;
                const version = parseInt(document.getElementById('version').value, 10);
                const tipoPuestoId = document.getElementById('tipoPuestoId').value ? parseInt(document.getElementById('tipoPuestoId').value, 10) : null;
                const nombreReferencial = document.getElementById('nombreReferencial')?.value.trim();
                const descripcion = document.getElementById('descripcion')?.value.trim();
                const ancho = document.getElementById('anchoMetros')?.value ? parseFloat(document.getElementById('anchoMetros').value) : null;
                const largo = document.getElementById('largoMetros')?.value ? parseFloat(document.getElementById('largoMetros').value) : null;
                const area = document.getElementById('areaMetrosCuadrados')?.value ? parseFloat(document.getElementById('areaMetrosCuadrados').value) : null;
                const nivel = document.getElementById('nivel')?.value ? parseInt(document.getElementById('nivel').value, 10) : 1;
                const ubicacionRef = document.getElementById('ubicacionReferencia')?.value.trim();

                const payload = {
                    nombreReferencial: nombreReferencial || null,
                    tipoPuestoId: tipoPuestoId,
                    descripcion: descripcion || null,
                    areaMetrosCuadrados: area,
                    anchoMetros: ancho,
                    largoMetros: largo,
                    nivel: nivel,
                    ubicacionReferencia: ubicacionRef || null,
                    version: version
                };

                const resp = await fetch(`/api/mercados/${mercadoId}/puestos/${puestoId}`, {
                    method: 'PUT',
                    headers: headers,
                    body: JSON.stringify(payload)
                });

                if (!resp.ok) {
                    const err = await resp.json().catch(() => ({}));
                    throw new Error(err.message || err.error || `Error ${resp.status}`);
                }

                window.location.href = `/dashboard/mercados/${mercadoId}/puestos/${puestoId}`;
            } else {
                const codigo = document.getElementById('codigo').value.trim().toUpperCase();
                const numero = document.getElementById('numero').value.trim();
                const tipoPuestoId = parseInt(document.getElementById('tipoPuestoId').value, 10);
                const sectorId = document.getElementById('sectorId')?.value || null;
                const pabellonId = document.getElementById('pabellonId')?.value || null;
                const nivel = parseInt(document.getElementById('nivel').value, 10) || 1;
                const nombreReferencial = document.getElementById('nombreReferencial')?.value.trim();
                const descripcion = document.getElementById('descripcion')?.value.trim();
                const ancho = document.getElementById('anchoMetros')?.value ? parseFloat(document.getElementById('anchoMetros').value) : null;
                const largo = document.getElementById('largoMetros')?.value ? parseFloat(document.getElementById('largoMetros').value) : null;
                const area = document.getElementById('areaMetrosCuadrados')?.value ? parseFloat(document.getElementById('areaMetrosCuadrados').value) : null;
                const ubicacionRef = document.getElementById('ubicacionReferencia')?.value.trim();
                const estadoFisico = document.getElementById('estadoFisico')?.value || 'OPERATIVO';
                const estado = document.getElementById('estado')?.value || 'DISPONIBLE';

                const payload = {
                    mercadoId: mercadoId,
                    sectorId: sectorId,
                    pabellonId: pabellonId,
                    tipoPuestoId: tipoPuestoId,
                    codigo: codigo,
                    numero: numero,
                    nombreReferencial: nombreReferencial || null,
                    descripcion: descripcion || null,
                    areaMetrosCuadrados: area,
                    anchoMetros: ancho,
                    largoMetros: largo,
                    nivel: nivel,
                    ubicacionReferencia: ubicacionRef || null,
                    estadoFisico: estadoFisico,
                    estado: estado
                };

                const resp = await fetch(`/api/mercados/${mercadoId}/puestos`, {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify(payload)
                });

                if (!resp.ok) {
                    const err = await resp.json().catch(() => ({}));
                    throw new Error(err.message || err.error || `Error ${resp.status}`);
                }

                window.location.href = `/dashboard/mercados/${mercadoId}/puestos`;
            }
        } catch (err) {
            mensajeErrorTexto.textContent = err.message || 'Error al guardar los datos del puesto.';
            alertaError.classList.remove('hidden');
            btnGuardar.disabled = false;
            btnGuardar.textContent = esEdicion ? 'Guardar Cambios' : 'Registrar Puesto';
        }
    });
}
