/**
 * SIGM-QUINUA - Mesa de Partes Virtual: Módulo de Gráficos Estadísticos (Chart.js en Modo Oscuro)
 */
const ReportesGraficos = (function () {
    'use strict';

    let chartEstadoInstance = null;
    let chartEvolucionInstance = null;
    let chartOficinasInstance = null;
    let chartTramitesInstance = null;

    function configurarTemaOscuro() {
        if (!window.Chart) return;
        Chart.defaults.color = '#94a3b8';
        Chart.defaults.borderColor = '#334155';
        Chart.defaults.font.family = 'ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
        Chart.defaults.font.size = 11;
        Chart.defaults.plugins.legend.labels.usePointStyle = true;
        Chart.defaults.plugins.legend.labels.boxWidth = 8;
        Chart.defaults.plugins.legend.labels.padding = 12;
    }

    function renderizarGraficoEstado(data) {
        const canvas = document.getElementById('chartExpedientesEstado');
        if (!canvas || !window.Chart) return;

        if (chartEstadoInstance) {
            chartEstadoInstance.destroy();
        }

        const labels = data.labels || [];
        const valores = data.datos || [];
        const colores = data.colores || ['#3b82f6', '#06b6d4', '#f59e0b', '#ef4444', '#8b5cf6', '#10b981', '#64748b'];

        chartEstadoInstance = new Chart(canvas, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: valores,
                    backgroundColor: colores,
                    borderColor: '#1e293b',
                    borderWidth: 2,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '68%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxHeight: 8 }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#e2e8f0',
                        bodyColor: '#cbd5e1',
                        borderColor: '#334155',
                        borderWidth: 1,
                        padding: 10,
                        callbacks: {
                            label: function (context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const val = context.raw || 0;
                                const pct = total > 0 ? ((val / total) * 100).toFixed(1) : '0.0';
                                return ` ${context.label}: ${val} (${pct}%)`;
                            }
                        }
                    }
                }
            }
        });
    }

    function renderizarGraficoEvolucion(data) {
        const canvas = document.getElementById('chartEvolucionMensual');
        if (!canvas || !window.Chart) return;

        if (chartEvolucionInstance) {
            chartEvolucionInstance.destroy();
        }

        const meses = data.meses || ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Set', 'Oct', 'Nov', 'Dic'];
        const presentados = data.presentados || [];
        const resueltos = data.resueltos || [];

        chartEvolucionInstance = new Chart(canvas, {
            type: 'line',
            data: {
                labels: meses,
                datasets: [
                    {
                        label: 'Presentados',
                        data: presentados,
                        borderColor: '#3b82f6',
                        backgroundColor: 'rgba(59, 130, 246, 0.1)',
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: '#3b82f6',
                        pointBorderColor: '#1e293b',
                        pointHoverRadius: 6,
                        borderWidth: 2
                    },
                    {
                        label: 'Resueltos',
                        data: resueltos,
                        borderColor: '#10b981',
                        backgroundColor: 'rgba(16, 185, 129, 0.1)',
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: '#10b981',
                        pointBorderColor: '#1e293b',
                        pointHoverRadius: 6,
                        borderWidth: 2
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        grid: { display: false }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: { precision: 0 }
                    }
                },
                plugins: {
                    legend: {
                        position: 'top',
                        align: 'end'
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#e2e8f0',
                        bodyColor: '#cbd5e1',
                        borderColor: '#334155',
                        borderWidth: 1,
                        padding: 10
                    }
                }
            }
        });
    }

    function renderizarGraficoOficinas(dataList) {
        const canvas = document.getElementById('chartExpedientesOficina');
        if (!canvas || !window.Chart) return;

        if (chartOficinasInstance) {
            chartOficinasInstance.destroy();
        }

        // Top 6 oficinas
        const top = (dataList || []).slice(0, 6);
        const labels = top.map(o => o.nombreOficina.length > 20 ? o.nombreOficina.substring(0, 18) + '...' : o.nombreOficina);
        const recibidos = top.map(o => o.recibidos);
        const enAtencion = top.map(o => o.enAtencion);
        const resueltos = top.map(o => o.resueltos);

        chartOficinasInstance = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Recibidos',
                        data: recibidos,
                        backgroundColor: '#06b6d4',
                        borderRadius: 4
                    },
                    {
                        label: 'En Atención',
                        data: enAtencion,
                        backgroundColor: '#8b5cf6',
                        borderRadius: 4
                    },
                    {
                        label: 'Resueltos',
                        data: resueltos,
                        backgroundColor: '#10b981',
                        borderRadius: 4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        stacked: true,
                        grid: { display: false }
                    },
                    y: {
                        stacked: true,
                        beginAtZero: true,
                        ticks: { precision: 0 }
                    }
                },
                plugins: {
                    legend: { position: 'top', align: 'end' }
                }
            }
        });
    }

    function renderizarGraficoTramites(dataList) {
        const canvas = document.getElementById('chartExpedientesTramite');
        if (!canvas || !window.Chart) return;

        if (chartTramitesInstance) {
            chartTramitesInstance.destroy();
        }

        // Top 5 trámites
        const top = (dataList || []).slice(0, 5);
        const labels = top.map(t => t.nombreTramite.length > 25 ? t.nombreTramite.substring(0, 22) + '...' : t.nombreTramite);
        const valores = top.map(t => t.cantidad);

        chartTramitesInstance = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Cantidad',
                    data: valores,
                    backgroundColor: '#1d4ed8',
                    hoverBackgroundColor: '#2563eb',
                    borderRadius: 4
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: { precision: 0 }
                    },
                    y: {
                        grid: { display: false }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }

    return {
        configurarTemaOscuro,
        renderizarGraficoEstado,
        renderizarGraficoEvolucion,
        renderizarGraficoOficinas,
        renderizarGraficoTramites
    };
})();
