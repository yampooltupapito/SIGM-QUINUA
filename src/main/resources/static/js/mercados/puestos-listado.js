/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para listado de Puestos Comerciales (Modal de Estado, Interacciones)
 */
document.addEventListener('DOMContentLoaded', () => {
    initModalEstado();
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

function abrirModalEstado(puestoId, codigo, estadoFisico, estadoOperativo) {
    const modal = document.getElementById('modalEstadoPuesto');
    if (!modal) return;

    document.getElementById('modalPuestoId').value = puestoId;
    document.getElementById('modalPuestoCodigo').textContent = codigo;
    if (estadoFisico) document.getElementById('modalNuevoEstadoFisico').value = estadoFisico;
    if (estadoOperativo) document.getElementById('modalNuevoEstadoOperativo').value = estadoOperativo;
    document.getElementById('modalMotivo').value = '';
    document.getElementById('charCountMotivo').textContent = '0/500';

    const alerta = document.getElementById('modalEstadoAlerta');
    alerta.classList.add('hidden');
    alerta.textContent = '';

    modal.classList.remove('hidden');
}

function cerrarModalEstado() {
    const modal = document.getElementById('modalEstadoPuesto');
    if (modal) modal.classList.add('hidden');
}

function initModalEstado() {
    const textarea = document.getElementById('modalMotivo');
    const charCounter = document.getElementById('charCountMotivo');
    if (textarea && charCounter) {
        textarea.addEventListener('input', () => {
            charCounter.textContent = `${textarea.value.length}/500`;
        });
    }

    const form = document.getElementById('formCambiarEstadoPuesto');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const puestoId = document.getElementById('modalPuestoId').value;
        const mercadoId = document.getElementById('modalMercadoId').value;
        const nuevoFisico = document.getElementById('modalNuevoEstadoFisico').value;
        const nuevoOperativo = document.getElementById('modalNuevoEstadoOperativo').value;
        const motivo = document.getElementById('modalMotivo').value.trim();
        const btn = document.getElementById('btnGuardarEstado');
        const alerta = document.getElementById('modalEstadoAlerta');

        alerta.classList.add('hidden');
        alerta.textContent = '';
        btn.disabled = true;
        btn.textContent = 'Guardando...';

        const { token, header } = getCsrfData();
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) headers[header] = token;

        try {
            const resp = await fetch(`/api/mercados/${mercadoId}/puestos/${puestoId}/estado`, {
                method: 'PATCH',
                headers: headers,
                body: JSON.stringify({
                    nuevoEstadoFisico: nuevoFisico,
                    nuevoEstadoOperativo: nuevoOperativo,
                    motivo: motivo
                })
            });

            if (!resp.ok) {
                const errorData = await resp.json().catch(() => ({}));
                throw new Error(errorData.message || errorData.error || `Error ${resp.status}`);
            }

            // Éxito: recargar página
            window.location.reload();
        } catch (err) {
            alerta.textContent = err.message || 'Error al actualizar el estado del puesto.';
            alerta.classList.remove('hidden');
            btn.disabled = false;
            btn.textContent = 'Confirmar Cambio';
        }
    });
}
