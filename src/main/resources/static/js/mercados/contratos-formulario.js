// SIGM-QUINUA: Manejo dinámico del formulario de contratos de ocupación
document.addEventListener('DOMContentLoaded', function () {
    const asignacionSelect = document.getElementById('asignacionId');
    const tipoContratoSelect = document.getElementById('tipoContratoId');
    const fechaInicioInput = document.getElementById('fechaInicio');
    const fechaFinInput = document.getElementById('fechaFin');
    const btnGuardar = document.getElementById('btnGuardarContrato');

    if (asignacionSelect) {
        asignacionSelect.addEventListener('change', actualizarFichaAsignacion);
        if (asignacionSelect.value) {
            actualizarFichaAsignacion();
        }
    }

    if (tipoContratoSelect) {
        tipoContratoSelect.addEventListener('change', recalcularFechaFin);
    }

    if (btnGuardar) {
        btnGuardar.addEventListener('click', guardarContrato);
    }

    function actualizarFichaAsignacion() {
        const opt = asignacionSelect.selectedOptions[0];
        if (!opt || !opt.value) {
            document.getElementById('infoComerciante').textContent = '-';
            document.getElementById('infoDoc').textContent = '-';
            document.getElementById('infoMercado').textContent = '-';
            document.getElementById('infoPuesto').textContent = '-';
            return;
        }

        document.getElementById('infoComerciante').textContent = opt.dataset.comerciante || '-';
        document.getElementById('infoDoc').textContent = opt.dataset.doc || '-';
        document.getElementById('infoMercado').textContent = opt.dataset.mercado || '-';
        document.getElementById('infoPuesto').textContent = opt.dataset.puesto || '-';

        if (opt.dataset.finicio && fechaInicioInput && !fechaInicioInput.value) {
            fechaInicioInput.value = opt.dataset.finicio;
        }
        if (opt.dataset.ffin && fechaFinInput && !fechaFinInput.value) {
            fechaFinInput.value = opt.dataset.ffin;
        }
    }

    function recalcularFechaFin() {
        const opt = tipoContratoSelect.selectedOptions[0];
        if (!opt || !fechaInicioInput.value) return;

        const meses = parseInt(opt.dataset.meses || '12', 10);
        if (meses > 0 && !fechaFinInput.value) {
            const f = new Date(fechaInicioInput.value);
            f.setMonth(f.getMonth() + meses);
            fechaFinInput.value = f.toISOString().split('T')[0];
        }
    }

    async function guardarContrato() {
        const contratoId = document.getElementById('contratoId').value;
        const isEdit = contratoId && contratoId.trim() !== '';

        const payload = {
            tipoContratoId: tipoContratoSelect ? tipoContratoSelect.value : null,
            fechaInicio: fechaInicioInput.value,
            fechaFin: fechaFinInput.value || null,
            expedienteId: document.getElementById('expedienteId').value || null,
            oficinaResponsableId: document.getElementById('oficinaResponsableId').value || null,
            observaciones: document.getElementById('observaciones').value || null
        };

        if (!payload.fechaInicio) {
            alert('Debe especificar la fecha de inicio del contrato.');
            return;
        }

        let url = '/api/mercados/contratos';
        let method = 'POST';

        if (isEdit) {
            url = `/api/mercados/contratos/${contratoId}`;
            method = 'PUT';
        } else {
            if (!asignacionSelect || !asignacionSelect.value) {
                alert('Debe seleccionar una asignación comercial aprobada o vigente.');
                return;
            }
            payload.asignacionId = asignacionSelect.value;
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
                throw new Error(errData.message || 'Error al guardar el contrato.');
            }

            const data = await res.json();
            window.location.href = `/dashboard/mercados/contratos/${data.id}`;
        } catch (err) {
            alert(err.message);
            btnGuardar.disabled = false;
            btnGuardar.textContent = isEdit ? 'Guardar Cambios' : 'Registrar Borrador';
        }
    }
});
