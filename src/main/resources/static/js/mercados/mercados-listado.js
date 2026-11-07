/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para listado, modal de cambio de estado y filtros
 */
document.addEventListener('DOMContentLoaded', () => {
    initModalCambioEstado();
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

function initModalCambioEstado() {
    const modal = document.getElementById('modalCambioEstado');
    if (!modal) return;

    const backdrop = document.getElementById('modalEstadoBackdrop');
    const btnCerrar = document.getElementById('btnCerrarModalEstado');
    const btnCancelar = document.getElementById('btnCancelarModalEstado');
    const form = document.getElementById('formCambioEstado');
    const inputMercadoId = document.getElementById('modalMercadoId');
    const labelCodigo = document.getElementById('modalMercadoCodigo');
    const labelNombre = document.getElementById('modalMercadoNombre');
    const labelEstadoActual = document.getElementById('modalMercadoEstadoActual');
    const selectNuevoEstado = document.getElementById('modalNuevoEstado');
    const textareaMotivo = document.getElementById('modalMotivoEstado');
    const contadorMotivo = document.getElementById('contadorCaracteresMotivo');
    const alertaSuspension = document.getElementById('alertaSuspensionMercado');
    const errorContainer = document.getElementById('modalEstadoError');
    const btnGuardar = document.getElementById('btnGuardarModalEstado');

    const openModal = (id, codigo, nombre, estadoActual) => {
        if (!modal) return;
        inputMercadoId.value = id || '';
        labelCodigo.textContent = codigo || '-';
        labelNombre.textContent = nombre || '-';
        labelEstadoActual.textContent = estadoActual || '-';
        selectNuevoEstado.value = '';
        textareaMotivo.value = '';
        if (contadorMotivo) contadorMotivo.textContent = '0 / 500';
        if (alertaSuspension) alertaSuspension.classList.add('hidden');
        if (errorContainer) {
            errorContainer.classList.add('hidden');
            errorContainer.textContent = '';
        }
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        document.body.classList.add('overflow-hidden');
        if (window.lucide) window.lucide.createIcons();
    };

    const closeModal = () => {
        if (!modal) return;
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        document.body.classList.remove('overflow-hidden');
    };

    document.querySelectorAll('.btn-abrir-modal-estado').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-mercado-id');
            const codigo = btn.getAttribute('data-mercado-codigo');
            const nombre = btn.getAttribute('data-mercado-nombre');
            const estado = btn.getAttribute('data-mercado-estado');
            openModal(id, codigo, nombre, estado);
        });
    });

    if (btnCerrar) btnCerrar.addEventListener('click', closeModal);
    if (btnCancelar) btnCancelar.addEventListener('click', closeModal);
    if (backdrop) backdrop.addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal || e.target.id === 'modalEstadoBackdrop') {
            closeModal();
        }
    });

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

    if (selectNuevoEstado && alertaSuspension) {
        selectNuevoEstado.addEventListener('change', () => {
            const val = selectNuevoEstado.value;
            if (val === 'SUSPENDIDO' || val === 'INACTIVO') {
                alertaSuspension.classList.remove('hidden');
            } else {
                alertaSuspension.classList.add('hidden');
            }
        });
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = inputMercadoId.value;
            const nuevoEstado = selectNuevoEstado.value;
            const motivo = textareaMotivo.value.trim();

            if (!id || !nuevoEstado || !motivo) {
                showModalError('Por favor complete el nuevo estado y el motivo institucional.');
                return;
            }

            // CSRF
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content ||
                              document.getElementById('csrf-token')?.value || '';
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content ||
                               document.getElementById('csrf-header')?.value || 'X-CSRF-TOKEN';

            const headers = {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            };
            if (csrfToken) {
                headers[csrfHeader] = csrfToken;
            }

            btnGuardar.disabled = true;
            const originalHtml = btnGuardar.innerHTML;
            btnGuardar.innerHTML = `<span class="animate-spin mr-1.5">&#9696;</span> Guardando...`;

            try {
                const response = await fetch(`/api/mercados/${id}/estado`, {
                    method: 'PATCH',
                    headers: headers,
                    body: JSON.stringify({
                        nuevoEstado: nuevoEstado,
                        motivo: motivo
                    })
                });

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(data.error || data.message || 'Error al procesar el cambio de estado');
                }

                closeModal();
                // Recargar listado o detalle
                window.location.reload();
            } catch (err) {
                showModalError(err.message);
            } finally {
                btnGuardar.disabled = false;
                btnGuardar.innerHTML = originalHtml;
                if (window.lucide) window.lucide.createIcons();
            }
        });
    }

    function showModalError(msg) {
        if (!errorContainer) return;
        errorContainer.textContent = msg;
        errorContainer.classList.remove('hidden');
    }
}
