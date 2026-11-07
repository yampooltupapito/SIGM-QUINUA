/**
 * SIGM-QUINUA - Mesa de Partes Virtual: Módulo de Filtros para Reportes y Estadísticas
 */
const ReportesFiltros = (function () {
    'use strict';

    function obtenerValoresFiltro() {
        return {
            periodo: document.getElementById('filtroPeriodo')?.value || 'MES_ACTUAL',
            fechaInicio: document.getElementById('filtroFechaInicio')?.value || '',
            fechaFin: document.getElementById('filtroFechaFin')?.value || '',
            oficinaId: document.getElementById('filtroOficina')?.value || '',
            tipoTramiteId: document.getElementById('filtroTipoTramite')?.value || '',
            estado: document.getElementById('filtroEstado')?.value || '',
            prioridad: document.getElementById('filtroPrioridad')?.value || '',
            canalIngreso: document.getElementById('filtroCanal')?.value || '',
            termino: document.getElementById('filtroTermino')?.value || '',
            anio: document.getElementById('filtroAnio')?.value || new Date().getFullYear()
        };
    }

    function construirQueryString(extraParams = {}) {
        const filtros = { ...obtenerValoresFiltro(), ...extraParams };
        const params = new URLSearchParams();

        Object.keys(filtros).forEach(key => {
            const val = filtros[key];
            if (val !== null && val !== undefined && val !== '') {
                params.append(key, val);
            }
        });

        return params.toString();
    }

    function aplicarPreajustePeriodo(periodo) {
        const hoy = new Date();
        const fInicio = document.getElementById('filtroFechaInicio');
        const fFin = document.getElementById('filtroFechaFin');

        if (!fInicio || !fFin) return;

        const formatIso = (d) => d.toISOString().split('T')[0];

        if (periodo === 'MES_ACTUAL') {
            const primerDia = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
            const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
            fInicio.value = formatIso(primerDia);
            fFin.value = formatIso(ultimoDia);
        } else if (periodo === 'MES_ANTERIOR') {
            const primerDia = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
            const ultimoDia = new Date(hoy.getFullYear(), hoy.getMonth(), 0);
            fInicio.value = formatIso(primerDia);
            fFin.value = formatIso(ultimoDia);
        } else if (periodo === 'TRIMESTRE_ACTUAL') {
            const mesActual = hoy.getMonth();
            const primerMes = Math.floor(mesActual / 3) * 3;
            const primerDia = new Date(hoy.getFullYear(), primerMes, 1);
            const ultimoDia = new Date(hoy.getFullYear(), primerMes + 3, 0);
            fInicio.value = formatIso(primerDia);
            fFin.value = formatIso(ultimoDia);
        } else if (periodo === 'ANIO_ACTUAL') {
            const primerDia = new Date(hoy.getFullYear(), 0, 1);
            const ultimoDia = new Date(hoy.getFullYear(), 11, 31);
            fInicio.value = formatIso(primerDia);
            fFin.value = formatIso(ultimoDia);
        }
    }

    function inicializar() {
        const selectPeriodo = document.getElementById('filtroPeriodo');
        if (selectPeriodo) {
            selectPeriodo.addEventListener('change', function () {
                if (this.value !== 'PERSONALIZADO') {
                    aplicarPreajustePeriodo(this.value);
                }
            });
            // Preajustar al cargar si no tiene fechas
            const fInicio = document.getElementById('filtroFechaInicio');
            if (fInicio && !fInicio.value) {
                aplicarPreajustePeriodo(selectPeriodo.value);
            }
        }

        const btnLimpiar = document.getElementById('btnLimpiarFiltros');
        if (btnLimpiar) {
            btnLimpiar.addEventListener('click', function () {
                document.getElementById('formFiltrosReporte')?.reset();
                if (selectPeriodo) {
                    selectPeriodo.value = 'MES_ACTUAL';
                    aplicarPreajustePeriodo('MES_ACTUAL');
                }
                document.dispatchEvent(new CustomEvent('reportes:filtros-aplicados'));
            });
        }

        const formFiltros = document.getElementById('formFiltrosReporte');
        if (formFiltros) {
            formFiltros.addEventListener('submit', function (e) {
                e.preventDefault();
                document.dispatchEvent(new CustomEvent('reportes:filtros-aplicados'));
            });
        }
    }

    return {
        inicializar,
        obtenerValoresFiltro,
        construirQueryString
    };
})();
