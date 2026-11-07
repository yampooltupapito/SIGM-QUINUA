/**
 * comerciantes-listado.js — Gestión de interactividad y modal de estado para el Padrón de Comerciantes.
 */

function getCsrfTokens() {
    const metaToken = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    return {
        token: metaToken ? metaToken.getAttribute('content') : '',
        header: metaHeader ? metaHeader.getAttribute('content') : 'X-CSRF-TOKEN'
    };
}

function abrirModalEstado(comercianteId, nombreTitular, estadoActual) {
    const modal = document.getElementById('modalEstadoComerciante');
    if (!modal) return;

    document.getElementById('modalComercianteId').value = comercianteId;
    document.getElementById('modalComercianteNombre').textContent = `Titular: ${nombreTitular} (Actual: ${estadoActual})`;

    const selectEstado = document.getElementById('modalNuevoEstado');
    if (selectEstado) {
        selectEstado.value = '';
    }

    const textareaMotivo = document.getElementById('modalMotivo');
    if (textareaMotivo) {
        textareaMotivo.value = '';
    }

    const textareaObs = document.getElementById('modalObservaciones');
    if (textareaObs) {
        textareaObs.value = '';
    }

    modal.classList.remove('hidden');
}

function cerrarModalEstado() {
    const modal = document.getElementById('modalEstadoComerciante');
    if (modal) {
        modal.classList.add('hidden');
    }
}

async function enviarCambioEstado(event) {
    event.preventDefault();
    const btn = document.getElementById('btnConfirmarEstado');
    const comercianteId = document.getElementById('modalComercianteId').value;
    const nuevoEstado = document.getElementById('modalNuevoEstado').value;
    const motivo = document.getElementById('modalMotivo').value.trim();
    const observaciones = document.getElementById('modalObservaciones').value.trim();

    if (!nuevoEstado) {
        alert('Debe seleccionar el nuevo estado del comerciante.');
        return;
    }
    if (!motivo || motivo.length < 5) {
        alert('El motivo debe contener al menos 5 caracteres.');
        return;
    }

    const csrf = getCsrfTokens();
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = 'Guardando...';

    try {
        const payload = {
            nuevoEstado: nuevoEstado,
            motivo: motivo,
            observaciones: observaciones
        };

        const response = await fetch(`/api/mercados/comerciantes/${comercianteId}/estado`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                [csrf.header]: csrf.token
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            cerrarModalEstado();
            window.location.reload();
        } else {
            const err = await response.json();
            alert('Error: ' + (err.message || 'No se pudo cambiar el estado'));
        }
    } catch (e) {
        console.error('Error al actualizar estado:', e);
        alert('Ocurrió un error inesperado al conectar con el servidor.');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}
