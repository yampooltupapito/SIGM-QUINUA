// static/js/mercados/asignaciones-formulario.js
document.addEventListener('DOMContentLoaded', function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const esEdicion = document.getElementById('esEdicion')?.value === 'true';
    const asignacionId = document.getElementById('asignacionId')?.value;

    const mercadoSelect = document.getElementById('mercadoSelect');
    const sectorSelect = document.getElementById('sectorSelect');
    const puestoSelect = document.getElementById('puestoId');

    const panelDisp = document.getElementById('panelDisponibilidadPuesto');
    const dispTitulo = document.getElementById('dispTitulo');
    const dispDetalle = document.getElementById('dispDetalle');

    const buscarInput = document.getElementById('buscarComercianteInput');
    const btnBuscar = document.getElementById('btnBuscarComerciante');
    const resultadosBox = document.getElementById('resultadosComerciantes');
    const comercianteIdInput = document.getElementById('comercianteId');
    const tarjetaComerciante = document.getElementById('tarjetaComercianteSeleccionado');
    const nombreDisplay = document.getElementById('comercianteNombreDisplay');
    const docDisplay = document.getElementById('comercianteDocDisplay');
    const btnCambiar = document.getElementById('btnCambiarComerciante');

    const form = document.getElementById('formAsignacion');

    // 1. Cargar Sectores y Puestos dinámicamente al cambiar mercado
    if (mercadoSelect) {
        mercadoSelect.addEventListener('change', function () {
            const mercadoId = this.value;
            sectorSelect.innerHTML = '<option value="">-- Todos los Sectores --</option>';
            puestoSelect.innerHTML = '<option value="">-- Seleccione Puesto Disponible --</option>';
            sectorSelect.disabled = true;
            puestoSelect.disabled = true;
            ocultarDisponibilidad();

            if (!mercadoId) return;

            // Cargar sectores del mercado
            fetch(`/api/mercados/${mercadoId}/sectores`)
                .then(res => res.json())
                .then(sectores => {
                    if (Array.isArray(sectores) && sectores.length > 0) {
                        sectores.forEach(s => {
                            const opt = document.createElement('option');
                            opt.value = s.id;
                            opt.textContent = s.nombre;
                            sectorSelect.appendChild(opt);
                        });
                        sectorSelect.disabled = false;
                    }
                })
                .catch(err => console.error('Error cargando sectores:', err));

            // Cargar puestos disponibles del mercado
            cargarPuestosDisponibles(mercadoId, null);
        });
    }

    if (sectorSelect) {
        sectorSelect.addEventListener('change', function () {
            const mercadoId = mercadoSelect.value;
            const sectorId = this.value;
            cargarPuestosDisponibles(mercadoId, sectorId);
        });
    }

    function cargarPuestosDisponibles(mercadoId, sectorId) {
        puestoSelect.innerHTML = '<option value="">-- Cargando puestos disponibles... --</option>';
        puestoSelect.disabled = true;
        ocultarDisponibilidad();

        let url = `/api/mercados/${mercadoId}/puestos?estado=DISPONIBLE&size=100`;
        if (sectorId) {
            url += `&sectorId=${sectorId}`;
        }

        fetch(url)
            .then(res => res.json())
            .then(page => {
                puestoSelect.innerHTML = '<option value="">-- Seleccione Puesto Disponible --</option>';
                const lista = page.content || page;
                if (Array.isArray(lista) && lista.length > 0) {
                    lista.forEach(p => {
                        const opt = document.createElement('option');
                        opt.value = p.id;
                        opt.textContent = `Puesto ${p.codigo} (N° ${p.numero}) - ${p.tipoPuestoNombre || 'Comercial'}`;
                        puestoSelect.appendChild(opt);
                    });
                    puestoSelect.disabled = false;
                } else {
                    puestoSelect.innerHTML = '<option value="">-- No hay puestos disponibles en este sector/mercado --</option>';
                }
            })
            .catch(err => {
                console.error('Error cargando puestos:', err);
                puestoSelect.innerHTML = '<option value="">-- Error al cargar puestos --</option>';
            });
    }

    // 2. Verificar disponibilidad del puesto al seleccionar
    if (puestoSelect) {
        puestoSelect.addEventListener('change', function () {
            const puestoId = this.value;
            if (!puestoId) {
                ocultarDisponibilidad();
                return;
            }

            fetch(`/api/mercados/asignaciones/puestos/${puestoId}/disponibilidad`)
                .then(res => res.json())
                .then(data => {
                    panelDisp.classList.remove('hidden');
                    if (data.disponible) {
                        panelDisp.className = 'p-4 rounded-xl border border-emerald-500/50 bg-emerald-950/20 text-xs text-emerald-300';
                        dispTitulo.textContent = 'Puesto Disponible para Asignación';
                        dispDetalle.textContent = `Código: ${data.puestoCodigo} | Área: ${data.areaMetrosCuadrados || '-'} m² | Tipo: ${data.tipoPuestoNombre || 'Estándar'}`;
                    } else {
                        panelDisp.className = 'p-4 rounded-xl border border-rose-500/50 bg-rose-950/20 text-xs text-rose-300';
                        dispTitulo.textContent = 'Puesto No Disponible';
                        dispDetalle.textContent = data.motivoNoDisponible || 'El puesto no está libre para nuevas solicitudes.';
                    }
                })
                .catch(err => console.error('Error verificando disponibilidad:', err));
        });
    }

    function ocultarDisponibilidad() {
        if (panelDisp) panelDisp.classList.add('hidden');
    }

    // 3. Búsqueda y Selección de Comerciantes
    if (btnBuscar && buscarInput) {
        btnBuscar.addEventListener('click', buscarComerciantes);
        buscarInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                buscarComerciantes();
            }
        });
    }

    function buscarComerciantes() {
        const query = buscarInput.value.trim();
        if (query.length < 2) {
            alert('Ingrese al menos 2 caracteres para buscar comerciante.');
            return;
        }

        fetch(`/api/mercados/comerciantes?termino=${encodeURIComponent(query)}&size=10`)
            .then(res => res.json())
            .then(page => {
                const lista = page.content || page;
                resultadosBox.innerHTML = '';
                if (!Array.isArray(lista) || lista.length === 0) {
                    resultadosBox.innerHTML = '<div class="p-3 text-xs text-slate-400 text-center">No se encontraron comerciantes activos con ese criterio.</div>';
                    resultadosBox.classList.remove('hidden');
                    return;
                }

                lista.forEach(c => {
                    const div = document.createElement('div');
                    div.className = 'p-3 hover:bg-[#202c3d] cursor-pointer transition-colors flex items-center justify-between text-xs';
                    div.innerHTML = `
                        <div>
                            <p class="font-bold text-white">${c.titularNombre || 'Sin nombre'}</p>
                            <p class="text-[11px] text-slate-400 font-mono">${c.codigoComerciante} — ${c.documentoEnmascarado || ''}</p>
                        </div>
                        <span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-900/40 text-emerald-300 border border-emerald-700">${c.estado}</span>
                    `;
                    div.addEventListener('click', () => seleccionarComerciante(c));
                    resultadosBox.appendChild(div);
                });
                resultadosBox.classList.remove('hidden');
            })
            .catch(err => console.error('Error buscando comerciantes:', err));
    }

    function seleccionarComerciante(c) {
        comercianteIdInput.value = c.id;
        nombreDisplay.textContent = c.titularNombre;
        docDisplay.textContent = `${c.codigoComerciante} — ${c.documentoEnmascarado || ''}`;
        tarjetaComerciante.classList.remove('hidden');
        resultadosBox.classList.add('hidden');
        buscarInput.value = '';
    }

    if (btnCambiar) {
        btnCambiar.addEventListener('click', function () {
            comercianteIdInput.value = '';
            tarjetaComerciante.classList.add('hidden');
            buscarInput.focus();
        });
    }

    // 4. Guardar Formulario
    if (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();

            if (!esEdicion && !comercianteIdInput.value) {
                alert('Debe buscar y seleccionar un comerciante titular.');
                return;
            }

            const formData = new FormData(form);
            const data = {};

            formData.forEach((value, key) => {
                if (key === 'presentarInmediatamente') {
                    data[key] = true;
                } else if (value.trim() !== '') {
                    data[key] = value.trim();
                }
            });

            const method = esEdicion ? 'PUT' : 'POST';
            const url = esEdicion ? `/api/mercados/asignaciones/${asignacionId}` : '/api/mercados/asignaciones';

            const headers = {
                'Content-Type': 'application/json'
            };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            fetch(url, {
                method: method,
                headers: headers,
                body: JSON.stringify(data)
            })
                .then(async res => {
                    if (!res.ok) {
                        const err = await res.json().catch(() => ({}));
                        throw new Error(err.message || 'Error al procesar la solicitud de asignación.');
                    }
                    return res.json();
                })
                .then(result => {
                    alert('Solicitud guardada exitosamente.');
                    window.location.href = `/dashboard/mercados/asignaciones/${result.id}`;
                })
                .catch(err => {
                    alert(err.message);
                });
        });
    }
});
