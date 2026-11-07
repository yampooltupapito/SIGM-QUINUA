/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para administración de Sectores Comerciales (CRUD, Modal de Estado, Concurrencia)
 */
document.addEventListener('DOMContentLoaded', () => {
    initFormSector();
    initModalEstadoSector();
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

function initFormSector() {
    const form = document.getElementById('formSector');
    if (!form) return;

    const esEdicion = form.getAttribute('data-es-edicion') === 'true';
    const mercadoId = form.getAttribute('data-mercado-id');
    const sectorId = form.getAttribute('data-sector-id');
    const alertaError = document.getElementById('alertaErrorFormulario');
    const btnGuardar = document.getElementById('btnGuardarSector');

    const inputCodigo = document.getElementById('codigo');
    if (inputCodigo && !esEdicion) {
        inputCodigo.addEventListener('input', () => {
            inputCodigo.value = inputCodigo.value.toUpperCase();
        });
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertaError.classList.add('hidden');
        alertaError.textContent = '';

        const nombre = document.getElementById('nombre').value.trim();
        const descripcion = document.getElementById('descripcion')?.value.trim();
        const categoriaComercial = document.getElementById('categoriaComercial')?.value;
        const tipoSector = document.getElementById('tipoSector')?.value.trim();
        const ordenVisual = parseInt(document.getElementById('ordenVisual')?.value || '0', 10);

        if (!nombre) {
            alertaError.textContent = 'El nombre del sector es obligatorio.';
            alertaError.classList.remove('hidden');
            return;
        }

        let payload = {
            nombre,
            descripcion: descripcion || null,
            categoriaComercial: categoriaComercial || null,
            tipoSector: tipoSector || 'GENERAL',
            ordenVisual: isNaN(ordenVisual) ? 0 : ordenVisual
        };

        let url = `/api/mercados/${mercadoId}/sectores`;
        let method = 'POST';

        if (esEdicion) {
            url = `/api/mercados/${mercadoId}/sectores/${sectorId}`;
            method = 'PUT';
            const versionVal = document.getElementById('sectorVersion')?.value;
            payload.version = versionVal ? parseInt(versionVal, 10) : 0;
        } else {
            const codigo = inputCodigo?.value.trim().toUpperCase();
            if (!codigo) {
                alertaError.textContent = 'El código del sector es obligatorio.';
                alertaError.classList.remove('hidden');
                return;
            }
            payload.codigo = codigo;
            payload.mercadoId = mercadoId;
            payload.estado = document.getElementById('estadoInicial')?.value || 'ACTIVO';
        }

        const { token, header } = getCsrfData();
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (token) headers[header] = token;

        btnGuardar.disabled = true;
        const originalText = btnGuardar.innerHTML;
        btnGuardar.innerHTML = '<span>Guardando...</span>';

        try {
            const response = await fetch(url, {
                method,
                headers,
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.message || errData.mensaje || `Error en el servidor (${response.status})`);
            }

            window.location.href = `/dashboard/mercados/${mercadoId}/sectores`;
        } catch (error) {
            alertaError.textContent = error.message;
            alertaError.classList.remove('hidden');
            btnGuardar.disabled = false;
            btnGuardar.innerHTML = originalText;
            if (window.lucide) window.lucide.createIcons();
        }
    });
}

function initModalEstadoSector() {
    const modal = document.getElementById('modalCambioEstadoSector');
    if (!modal) return;

    const backdrop = document.getElementById('modalEstadoSectorBackdrop');
    const btnCerrar = document.getElementById('btnCerrarModalEstadoSector');
    const btnCancelar = document.getElementById('btnCancelarModalEstadoSector');
    const form = document.getElementById('formCambioEstadoSector');
    const inputSectorId = document.getElementById('modalSectorId');
    const lblCodigo = document.getElementById('modalSectorCodigo');
    const lblNombre = document.getElementById('modalSectorNombre');
    const lblEstadoActual = document.getElementById('modalSectorEstadoActual');
    const lblPuestos = document.getElementById('modalSectorPuestos');
    const alertaPuestos = document.getElementById('alertaPuestosActivosSector');
    const selectNuevoEstado = document.getElementById('modalNuevoEstadoSector');
    const textareaMotivo = document.getElementById('modalMotivoSector');
    const contadorMotivo = document.getElementById('contadorMotivoSector');
    const errorContainer = document.getElementById('modalEstadoSectorError');
    const btnGuardar = document.getElementById('btnGuardarModalEstadoSector');

    // Extraer mercadoId del contexto actual
    const matchMercado = window.location.pathname.match(/\/mercados\/([a-f0-9\-]+)/);
    const mercadoId = matchMercado ? matchMercado[1] : '';

    const openModal = (id, codigo, nombre, estado, puestos) => {
        inputSectorId.value = id;
        lblCodigo.textContent = codigo;
        lblNombre.textContent = nombre;
        lblEstadoActual.textContent = estado;
        lblPuestos.textContent = puestos || '0';
        selectNuevoEstado.value = '';
        textareaMotivo.value = '';
        if (contadorMotivo) contadorMotivo.textContent = '0 / 500';
        if (alertaPuestos) alertaPuestos.classList.add('hidden');
        if (errorContainer) {
            errorContainer.classList.add('hidden');
            errorContainer.textContent = '';
        }
        modal.classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
        if (window.lucide) window.lucide.createIcons();
    };

    const closeModal = () => {
        modal.classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
    };

    document.querySelectorAll('.btn-cambiar-estado-sector').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const id = btn.getAttribute('data-sector-id');
            const codigo = btn.getAttribute('data-sector-codigo');
            const nombre = btn.getAttribute('data-sector-nombre');
            const estado = btn.getAttribute('data-sector-estado');
            const puestos = btn.getAttribute('data-sector-puestos');
            openModal(id, codigo, nombre, estado, puestos);
        });
    });

    if (btnCerrar) btnCerrar.addEventListener('click', closeModal);
    if (btnCancelar) btnCancelar.addEventListener('click', closeModal);
    if (backdrop) backdrop.addEventListener('click', closeModal);

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !modal.classList.contains('hidden')) {
            closeModal();
        }
    });

    if (textareaMotivo && contadorMotivo) {
        textareaMotivo.addEventListener('input', () => {
            contadorMotivo.textContent = `${textareaMotivo.value.length} / 500`;
        });
    }

    if (selectNuevoEstado && alertaPuestos) {
        selectNuevoEstado.addEventListener('change', () => {
            const val = selectNuevoEstado.value;
            const puestos = parseInt(lblPuestos.textContent || '0', 10);
            if ((val === 'INACTIVO' || val === 'SUSPENDIDO') && puestos > 0) {
                alertaPuestos.classList.remove('hidden');
            } else {
                alertaPuestos.classList.add('hidden');
            }
        });
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const sectorId = inputSectorId.value;
            const nuevoEstado = selectNuevoEstado.value;
            const motivo = textareaMotivo.value.trim();

            if (!sectorId || !nuevoEstado || !motivo) {
                errorContainer.textContent = 'Seleccione el nuevo estado y detalle el motivo institucional.';
                errorContainer.classList.remove('hidden');
                return;
            }

            const { token, header } = getCsrfData();
            const headers = {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            };
            if (token) headers[header] = token;

            btnGuardar.disabled = true;
            const origText = btnGuardar.innerHTML;
            btnGuardar.innerHTML = '<span>Actualizando...</span>';

            try {
                const response = await fetch(`/api/mercados/${mercadoId}/sectores/${sectorId}/estado`, {
                    method: 'PATCH',
                    headers,
                    body: JSON.stringify({ nuevoEstado, motivo })
                });

                if (!response.ok) {
                    const err = await response.json().catch(() => ({}));
                    throw new Error(err.message || err.mensaje || `Error al cambiar estado (${response.status})`);
                }

                window.location.reload();
            } catch (err) {
                errorContainer.textContent = err.message;
                errorContainer.classList.remove('hidden');
                btnGuardar.disabled = false;
                btnGuardar.innerHTML = origText;
            }
        });
    }
}
