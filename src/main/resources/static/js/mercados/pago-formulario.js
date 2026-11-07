let deudasCargadas = [];

document.addEventListener('DOMContentLoaded', () => {
    const preId = document.getElementById('comercianteId')?.value;
    if (preId) {
        cargarDeudasComerciante(preId);
    }

    const form = document.getElementById('formRegistrarPago');
    if (form) {
        form.addEventListener('submit', procesarPago);
    }

    document.getElementById('busquedaComerciante')?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            buscarComerciante();
        }
    });
});

async function buscarComerciante() {
    const q = document.getElementById('busquedaComerciante')?.value.trim();
    if (!q) {
        alert('Ingrese un número de documento o término de búsqueda');
        return;
    }

    try {
        const resp = await fetch(`/api/mercados/comerciantes?termino=${encodeURIComponent(q)}&size=1`);
        if (!resp.ok) throw new Error('Error al buscar comerciante');
        const data = await resp.json();

        if (!data.content || data.content.length === 0) {
            alert('No se encontró ningún comerciante con los datos ingresados.');
            return;
        }

        const com = data.content[0];
        document.getElementById('comercianteId').value = com.id;
        const nombre = com.titularNombre || `${com.personaNombres || ''} ${com.personaApellidoPaterno || ''}`.trim() || 'Comerciante Titular';
        const doc = com.documentoCompleto || com.documentoEnmascarado || com.personaNumeroDocumento || '-';
        document.getElementById('infoComerciante').innerHTML = `
            <div class="flex justify-between items-center">
                <div>
                    <span class="font-bold text-white text-sm block">${nombre}</span>
                    <span class="text-slate-400">DNI/RUC: ${doc} | Código: ${com.codigoComerciante || ''}</span>
                </div>
                <span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    COMERCIANTE SELECCIONADO
                </span>
            </div>
        `;
        document.getElementById('infoComerciante').classList.remove('hidden');

        await cargarDeudasComerciante(com.id);
    } catch (err) {
        alert(err.message);
    }
}

async function cargarDeudasComerciante(comercianteId) {
    const tbody = document.getElementById('tbodyDeudas');
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="6" class="px-3 py-6 text-center text-slate-400">Consultando obligaciones exigibles...</td></tr>`;

    try {
        const resp = await fetch(`/api/mercados/comerciantes/${comercianteId}/cuenta-corriente`);
        if (!resp.ok) throw new Error('Error al consultar cuenta corriente');
        const data = await resp.json();

        deudasCargadas = data.obligacionesExigibles || [];

        if (deudasCargadas.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="px-3 py-6 text-center text-emerald-400 font-semibold">El comerciante no registra deudas ni obligaciones pendientes de pago.</td></tr>`;
            recalcularTotalAPagar();
            return;
        }

        tbody.innerHTML = deudasCargadas.map((d, idx) => {
            return `
                <tr class="hover:bg-[#202c3d]/50 transition-colors">
                    <td class="px-3 py-3">
                        <input type="checkbox" id="chk_${idx}" onchange="toggleSeleccionDeuda(${idx})"
                               class="w-4 h-4 rounded border-[#334155] bg-[#101827] text-emerald-500 focus:ring-0">
                    </td>
                    <td class="px-3 py-3 font-mono font-bold text-blue-400">
                        ${d.codigoObligacion}
                    </td>
                    <td class="px-3 py-3">
                        <span class="text-white block font-semibold">${d.conceptoNombre}</span>
                        <span class="text-[10px] text-slate-400">Puesto: ${d.puestoNumero || '-'}</span>
                    </td>
                    <td class="px-3 py-3 text-slate-300">
                        ${d.fechaVencimiento || '-'}
                    </td>
                    <td class="px-3 py-3 text-right font-mono font-bold text-amber-400">
                        S/ ${Number(d.saldoPendiente).toFixed(2)}
                    </td>
                    <td class="px-3 py-3 text-right">
                        <input type="number" step="0.01" min="0.01" max="${d.saldoPendiente}"
                               id="inputMonto_${idx}" value="${Number(d.saldoPendiente).toFixed(2)}"
                               oninput="recalcularTotalAPagar()" disabled
                               class="w-full bg-[#101827] text-white text-right font-mono font-bold px-2 py-1.5 rounded-lg border border-[#334155] focus:outline-none focus:border-emerald-500 text-xs">
                    </td>
                </tr>
            `;
        }).join('');

        recalcularTotalAPagar();
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="6" class="px-3 py-6 text-center text-rose-400">No se pudieron cargar las obligaciones.</td></tr>`;
    }
}

function toggleSeleccionDeuda(idx) {
    const chk = document.getElementById(`chk_${idx}`);
    const input = document.getElementById(`inputMonto_${idx}`);
    if (chk && input) {
        input.disabled = !chk.checked;
        if (!chk.checked) {
            input.value = Number(deudasCargadas[idx].saldoPendiente).toFixed(2);
        }
    }
    recalcularTotalAPagar();
}

function recalcularTotalAPagar() {
    let total = 0;
    deudasCargadas.forEach((d, idx) => {
        const chk = document.getElementById(`chk_${idx}`);
        const input = document.getElementById(`inputMonto_${idx}`);
        if (chk && chk.checked && input) {
            const val = parseFloat(input.value) || 0;
            total += val;
        }
    });

    const badge = document.getElementById('totalSeleccionadoBadge');
    if (badge) {
        badge.innerText = `Total a Pagar: S/ ${total.toFixed(2)}`;
    }

    const inputRecibido = document.getElementById('montoRecibido');
    if (inputRecibido && (!inputRecibido.value || parseFloat(inputRecibido.value) === 0)) {
        inputRecibido.value = total > 0 ? total.toFixed(2) : '';
    }
}

async function procesarPago(e) {
    e.preventDefault();
    const errDiv = document.getElementById('alertaErrorPago');
    const okDiv = document.getElementById('alertaExitoPago');
    errDiv.classList.add('hidden');
    okDiv.classList.add('hidden');

    const comercianteId = document.getElementById('comercianteId')?.value;
    if (!comercianteId) {
        errDiv.innerText = 'Debe seleccionar un comerciante titular.';
        errDiv.classList.remove('hidden');
        return;
    }

    const aplicaciones = [];
    deudasCargadas.forEach((d, idx) => {
        const chk = document.getElementById(`chk_${idx}`);
        const input = document.getElementById(`inputMonto_${idx}`);
        if (chk && chk.checked && input) {
            const m = parseFloat(input.value) || 0;
            if (m > 0) {
                aplicaciones.push({
                    obligacionId: d.id,
                    monto: m
                });
            }
        }
    });

    if (aplicaciones.length === 0) {
        errDiv.innerText = 'Seleccione al menos una obligación para aplicar el pago.';
        errDiv.classList.remove('hidden');
        return;
    }

    const montoRecibido = parseFloat(document.getElementById('montoRecibido')?.value) || 0;
    const medioPago = document.getElementById('medioPago')?.value;
    const cajaSesionId = document.getElementById('cajaSesionId')?.value || null;
    const referencia = document.getElementById('referenciaOperacion')?.value || null;
    const observaciones = document.getElementById('observacionesPago')?.value || null;

    const payload = {
        comercianteId: comercianteId,
        montoRecibido: montoRecibido,
        medioPago: medioPago,
        cajaSesionId: cajaSesionId,
        referenciaOperacion: referencia,
        observaciones: observaciones,
        aplicaciones: aplicaciones
    };

    try {
        const resp = await fetch('/api/mercados/pagos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!resp.ok) {
            const errData = await resp.json().catch(() => ({}));
            throw new Error(errData.message || 'Error al procesar el pago.');
        }

        const data = await resp.json();
        okDiv.innerText = `¡Pago registrado exitosamente! Recibo: ${data.codigoPago}`;
        okDiv.classList.remove('hidden');

        setTimeout(() => {
            window.location.href = `/dashboard/mercados/pagos/${data.id}`;
        }, 1200);
    } catch (err) {
        errDiv.innerText = err.message;
        errDiv.classList.remove('hidden');
    }
}

window.buscarComerciante = buscarComerciante;
window.cargarDeudasComerciante = cargarDeudasComerciante;
window.toggleSeleccionDeuda = toggleSeleccionDeuda;
window.recalcularTotalAPagar = recalcularTotalAPagar;
window.procesarPago = procesarPago;

