package pe.gob.municipalidad.sigm.modules.mercados.enums;

public enum TipoReporteMercado {
    EJECUTIVO("Dashboard Ejecutivo Consolidado"),
    OCUPACION("Análisis de Ocupación de Puestos"),
    PUESTOS("Padrón Maestro de Puestos Comerciales"),
    COMERCIANTES("Padrón Oficial de Comerciantes"),
    ASIGNACIONES("Historial de Asignaciones de Puestos"),
    CONTRATOS("Contratos y Actos de Ocupación"),
    AUTORIZACIONES("Autorizaciones y Licencias Comerciales"),
    VENCIMIENTOS("Vencimientos de Instrumentos Legales"),
    OBLIGACIONES("Obligaciones y Liquidaciones de Merced"),
    RECAUDACION("Recaudación y Amortizaciones Confirmadas"),
    DEUDAS("Estado de Deuda y Antigüedad de Saldos"),
    INSPECCIONES("Inspecciones Sanitarias y de Seguridad"),
    INCIDENCIAS("Reportes de Incidencias Operativas"),
    RENOVACIONES("Trámites de Renovación y Cambio de Titular");

    private final String titulo;

    TipoReporteMercado(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
