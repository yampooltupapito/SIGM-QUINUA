// SIGM-QUINUA: Manejo dinámico del formulario de autorizaciones comerciales
document.addEventListener('DOMContentLoaded', function () {
    const comercianteSelect = document.getElementById('comercianteId');
    const asignacionSelect = document.getElementById('asignacionId');
    const tipoAutorizacionSelect = document.getElementById('tipoAutorizacionId');
    const fechaInicioInput = document.getElementById('fechaInicio');
    const fechaFinInput = document.getElementById('fechaFin');
    const btnGuardar = document.getElementById('btnGuardarAutorizacion');

    if (tipoAutorizacionSelect) {
        tipoAutorizacionSelect.addEventListener('change', recalcularFechaFin);
    }

    if (btnGuardar) {
        btnGuardar.addEventListener('click', guardarAutorizacion);
    }

    function recalcularFechaFin() {
        const opt = tipoAutorizacionSelect.selectedOptions[0];
        if (!opt || !fechaInicioInput.value) return;

        const meses = parseInt(opt.dataset.meses || '12', 10);
        if (meses > 0 && !fechaFinInput.value) {
            const f = new Date(fechaInicioInput.value);
            f.setMonth(f.getMonth() + meses);
            fechaFinInput.value = f.toISOString().split('T')[0];
        }
    }

    async function guardarAutorizacion() {
        const autorizacionId = document.getElementById('autorizacionId').value;
        const isEdit = autorizacionId && autorizacionId.trim() !== '';

        const payload = {
            tipoAutorizacionId: tipoAutorizacionSelect ? tipoAutorizacionSelect.value : null,
            fechaInicio: fechaInicioInput.value || null,
            fechaFin: fechaFinInput.value || null,
            expedienteId: document.getElementById('expedienteId').value || null,
            oficinaEmisoraId: document.getElementById('oficinaEmisoraId').value || null,
            observaciones: document.getElementById('observaciones').value || null
        };

        let url = '/api/mercados/autorizaciones';
        let method = 'POST';

        if (isEdit) {
            url = `/api/mercados/autorizaciones/${autorizacionId}`;
            method = 'PUT';
        } else {
            if (!comercianteSelect || !comercianteSelect.value) {
                alert('Debe seleccionar al comerciante titular.');
                return;
            }
            payload.comercianteId = comercianteSelect.value;
            payload.asignacionId = asignacionSelect && asignacionSelect.value ? asignacionSelect.value : null;
        }

        btnGuardar.disabled = true;
        btnGuardar.textContent = 'Procesando...';

        try {
            const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            const headers = { 'Content-Type': 'application/json' };
            if (token && header) headers[header] = token;

            const res = await fetch(url, {
                method: method,
                headers: headers,
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || 'Error al guardar la autorización comercial.');
            }

            const data = await res.json();
            window.location.href = `/dashboard/mercados/autorizaciones/${data.id}`;
        } catch (err) {
            alert(err.message);
            btnGuardar.disabled = false;
            btnGuardar.textContent = isEdit ? 'Guardar Cambios' : 'Registrar Solicitud';
        }
    }
});
