package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.KpiDashboardTributarioDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteFilterRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.reportes.ReporteTributarioDtos.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CentroReportesTributariosService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.IndicadoresRecaudacionService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ReporteCarteraTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ReporteRecaudacionService;

import java.util.List;

/**
 * Endpoints REST protegidos para alimentar componentes interactivos y gráficos del Centro de Reportes.
 */
@RestController
@RequestMapping("/api/dashboard/tributaria/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRIBUTARIA_REPORTES_ACCEDER') or hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_TRIBUTACION')")
public class CentroReportesTributariosRestController {

    private final CentroReportesTributariosService centroService;
    private final IndicadoresRecaudacionService indicadoresService;
    private final ReporteRecaudacionService recaudacionService;
    private final ReporteCarteraTributariaService carteraService;

    @GetMapping("/kpis")
    public ResponseEntity<KpiDashboardTributarioDto> getKpis() {
        return ResponseEntity.ok(centroService.obtenerResumenEjecutivo());
    }

    @GetMapping("/recaudacion-mensual")
    public ResponseEntity<List<RecaudacionPeriodoItemDto>> getRecaudacionMensual(
            @RequestParam(value = "ejercicio", required = false) Integer ejercicio) {
        return ResponseEntity.ok(recaudacionService.obtenerRecaudacionMensualEjercicio(ejercicio));
    }

    @GetMapping("/recaudacion-conceptos")
    public ResponseEntity<List<RecaudacionConceptoItemDto>> getRecaudacionConceptos(
            @ModelAttribute ReporteFilterRequest filtro) {
        return ResponseEntity.ok(recaudacionService.obtenerRecaudacionPorConcepto(filtro));
    }

    @GetMapping("/recaudacion-medios")
    public ResponseEntity<List<RecaudacionMedioItemDto>> getRecaudacionMedios(
            @ModelAttribute ReporteFilterRequest filtro) {
        return ResponseEntity.ok(recaudacionService.obtenerRecaudacionPorMedioPago(filtro));
    }

    @GetMapping("/cartera-tramos")
    public ResponseEntity<List<CarteraTramoItemDto>> getCarteraTramos() {
        return ResponseEntity.ok(carteraService.obtenerDistribucionPorTramosAntiguedad());
    }
}

