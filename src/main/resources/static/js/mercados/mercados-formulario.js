/**
 * SIGM-QUINUA — Módulo de Mercados Municipales
 * Script para formulario de registro y edición con control de concurrencia optimista
 */
document.addEventListener('DOMContentLoaded', () => {
    initFormularioMercado();
    if (window.lucide) {
        window.lucide.createIcons();
    }
});

function initFormularioMercado() {
    const form = document.getElementById('mercadoForm');
    if (!form) return;

    const esEdicion = document.getElementById('esEdicion')?.value === 'true';
    const mercadoId = document.getElementById('mercadoId')?.value;
    const versionInput = document.getElementById('version');
    const btnGuardar = document.getElementById('btnGuardarMercado');
    const btnGuardarTexto = document.getElementById('btnGuardarTexto');
    const btnGuardarSpinner = document.getElementById('btnGuardarSpinner');
    const alertBox = document.getElementById('form-alert');
    const alertTitle = document.getElementById('form-alert-title');
    const alertMessage = document.getElementById('form-alert-message');
    const btnCoordenadasQuinua = document.getElementById('btnCoordenadasQuinua');

    // Botón para fijar coordenadas de Quinua (-13.042500, -74.140800)
    if (btnCoordenadasQuinua) {
        btnCoordenadasQuinua.addEventListener('click', () => {
            const latInput = document.getElementById('latitud');
            const lngInput = document.getElementById('longitud');
            if (latInput) latInput.value = '-13.042500';
            if (lngInput) lngInput.value = '-74.140800';
        });
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        limpiarErrores();
        esconderAlerta();

        // Validaciones en cliente
        let valid = true;

        const codigoInput = document.getElementById('codigo');
        const nombreInput = document.getElementById('nombre');
        const direccionInput = document.getElementById('direccion');
        const dptoInput = document.getElementById('departamento');
        const provInput = document.getElementById('provincia');
        const distInput = document.getElementById('distrito');
        const latInput = document.getElementById('latitud');
        const lngInput = document.getElementById('longitud');
        const capInput = document.getElementById('capacidadReferencial');
        const estadoInput = document.getElementById('estado');
        const oficinaInput = document.getElementById('oficinaAdministradoraId');
        const descInput = document.getElementById('descripcion');
        const refInput = document.getElementById('referenciaUbicacion');

        if (!esEdicion && (!codigoInput.value.trim() || codigoInput.value.trim().length < 2)) {
            mostrarError('codigo', 'El código institucional es obligatorio (mínimo 2 caracteres).');
            valid = false;
        }

        if (!nombreInput.value.trim() || nombreInput.value.trim().length < 3) {
            mostrarError('nombre', 'El nombre oficial del mercado es obligatorio (mínimo 3 caracteres).');
            valid = false;
        }

        if (!direccionInput.value.trim()) {
            mostrarError('direccion', 'La dirección oficial es obligatoria.');
            valid = false;
        }

        if (!dptoInput.value.trim()) {
            mostrarError('departamento', 'El departamento es obligatorio.');
            valid = false;
        }

        if (!provInput.value.trim()) {
            mostrarError('provincia', 'La provincia es obligatoria.');
            valid = false;
        }

        if (!distInput.value.trim()) {
            mostrarError('distrito', 'El distrito es obligatorio.');
            valid = false;
        }

        if (capInput.value && parseInt(capInput.value, 10) < 0) {
            mostrarError('capacidadReferencial', 'La capacidad no puede ser un número negativo.');
            valid = false;
        }

        if (latInput.value) {
            const lat = parseFloat(latInput.value);
            if (isNaN(lat) || lat < -90 || lat > 90) {
                mostrarError('latitud', 'La latitud debe estar comprendida entre -90 y 90 grados.');
                valid = false;
            }
        }

        if (lngInput.value) {
            const lng = parseFloat(lngInput.value);
            if (isNaN(lng) || lng < -180 || lng > 180) {
                mostrarError('longitud', 'La longitud debe estar comprendida entre -180 y 180 grados.');
                valid = false;
            }
        }

        if (!valid) {
            mostrarAlerta('Error de Validación', 'Por favor verifique los campos señalados en rojo.', 'error');
            return;
        }

        // Construcción del payload
        const payload = {
            nombre: nombreInput.value.trim(),
            descripcion: descInput.value.trim() || null,
            direccion: direccionInput.value.trim(),
            referenciaUbicacion: refInput.value.trim() || null,
            distrito: distInput.value.trim(),
            provincia: provInput.value.trim(),
            departamento: dptoInput.value.trim(),
            latitud: latInput.value ? parseFloat(latInput.value) : null,
            longitud: lngInput.value ? parseFloat(lngInput.value) : null,
            capacidadReferencial: capInput.value ? parseInt(capInput.value, 10) : 0,
            estado: estadoInput.value,
            oficinaAdministradoraId: oficinaInput.value ? parseInt(oficinaInput.value, 10) : null
        };

        if (!esEdicion) {
            payload.codigo = codigoInput.value.trim().toUpperCase();
        } else {
            payload.version = versionInput ? parseInt(versionInput.value, 10) : 0;
        }

        // CSRF
        const csrfToken = document.getElementById('csrf-token')?.value || '';
        const csrfHeader = document.getElementById('csrf-header')?.value || 'X-CSRF-TOKEN';

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        const url = esEdicion ? `/api/mercados/${mercadoId}` : '/api/mercados';
        const method = esEdicion ? 'PUT' : 'POST';

        setLoading(true);

        try {
            const res = await fetch(url, {
                method: method,
                headers: headers,
                body: JSON.stringify(payload)
            });

            const data = await res.json();

            if (res.status === 409) {
                mostrarAlerta('Conflicto de Concurrencia',
                    'El registro fue modificado simultáneamente por otro usuario o proceso. Por favor recargue la página para obtener la versión más reciente.',
                    'error');
                if (typeof mostrarToast === 'function') {
                    mostrarToast('Conflicto de concurrencia. Por favor recargue la página.', 'warning');
                }
                return;
            }

            if (!res.ok) {
                if (data.fieldErrors) {
                    Object.entries(data.fieldErrors).forEach(([field, msg]) => {
                        mostrarError(field, msg);
                    });
                }
                throw new Error(data.error || data.message || 'Error al guardar los datos del mercado.');
            }

            mostrarAlerta('Operación Exitosa', 'Mercado guardado correctamente en la base de datos municipal. Redirigiendo...', 'success');
            if (typeof mostrarToast === 'function') {
                mostrarToast('Mercado actualizado correctamente', 'success');
            }
            setTimeout(() => {
                const targetId = data.id || mercadoId;
                window.location.href = targetId ? `/dashboard/mercados/${targetId}` : '/dashboard/mercados/listado';
            }, 800);

        } catch (err) {
            console.error('Error al guardar mercado:', err);
            mostrarAlerta('Fallo al Guardar', err.message, 'error');
            if (typeof mostrarToast === 'function') {
                mostrarToast(err.message || 'Error al guardar mercado', 'error');
            }
        } finally {
            setLoading(false);
        }
    });

    function setLoading(isLoading) {
        if (!btnGuardar) return;
        btnGuardar.disabled = isLoading;
        if (btnGuardarSpinner) {
            if (isLoading) btnGuardarSpinner.classList.remove('hidden');
            else btnGuardarSpinner.classList.add('hidden');
        }
        if (btnGuardarTexto) {
            if (isLoading) btnGuardarTexto.textContent = esEdicion ? 'Actualizando...' : 'Guardando...';
            else btnGuardarTexto.textContent = esEdicion ? 'Actualizar Mercado' : 'Registrar Mercado';
        }
    }

    function mostrarError(campo, mensaje) {
        const span = document.getElementById(`err-${campo}`);
        const input = document.getElementById(campo);
        if (span) {
            span.textContent = mensaje;
            span.classList.remove('hidden');
        }
        if (input) {
            input.classList.add('border-rose-500');
        }
    }

    function limpiarErrores() {
        document.querySelectorAll('[id^="err-"]').forEach(el => {
            el.textContent = '';
            el.classList.add('hidden');
        });
        document.querySelectorAll('input, select, textarea').forEach(el => {
            el.classList.remove('border-rose-500');
        });
    }

    function mostrarAlerta(titulo, mensaje, tipo) {
        if (!alertBox) return;
        alertTitle.textContent = titulo;
        alertMessage.textContent = mensaje;
        alertBox.className = 'p-4 rounded-xl border text-sm flex items-start space-x-3 ';
        if (tipo === 'success') {
            alertBox.classList.add('bg-emerald-500/10', 'border-emerald-500/20', 'text-emerald-300');
        } else {
            alertBox.classList.add('bg-rose-500/10', 'border-rose-500/20', 'text-rose-300');
        }
        alertBox.classList.remove('hidden');
        try {
            alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } catch (e) {
            // Ignorar en navegadores sin soporte de smooth scroll
        }
    }

    function esconderAlerta() {
        if (alertBox) alertBox.classList.add('hidden');
    }
}
