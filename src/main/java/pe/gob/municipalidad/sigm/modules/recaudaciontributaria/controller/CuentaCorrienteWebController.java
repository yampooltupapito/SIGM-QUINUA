package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteDetailResponse;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.CuentaCorrienteSummaryDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoCuentaCorriente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.CuentaCorrienteTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.CuentaCorrienteTributariaService;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service.ObligacionExportService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/dashboard/tributaria/cuentas-corrientes")
@RequiredArgsConstructor
public class CuentaCorrienteWebController {

    private final CuentaCorrienteTributariaService cuentaService;
    private final ObligacionExportService exportService;
    private final CuentaCorrienteTributariaRepository cuentaRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_VER') or hasRole('ADMIN')")
    public String listarCuentas(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EstadoCuentaCorriente estado,
            @RequestParam(required = false) Boolean conDeuda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codigoCuenta"));
        Page<CuentaCorrienteSummaryDto> cuentas = cuentaService.listarCuentas(query, estado, conDeuda, pageable);

        BigDecimal saldoTotalConsolidado = cuentaRepository.sumarSaldoTotalActivo();
        long cuentasConDeuda = cuentaRepository.countCuentasConDeuda();
        long totalCuentas = cuentaRepository.count();

        model.addAttribute("cuentas", cuentas);
        model.addAttribute("query", query);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("conDeuda", conDeuda);
        model.addAttribute("estados", EstadoCuentaCorriente.values());
        model.addAttribute("saldoTotalConsolidado", saldoTotalConsolidado != null ? saldoTotalConsolidado : BigDecimal.ZERO);
        model.addAttribute("cuentasConDeuda", cuentasConDeuda);
        model.addAttribute("totalCuentas", totalCuentas);

        return "admin/tributaria/cuentas-corrientes/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_VER') or hasRole('ADMIN')")
    public String verEstadoCuenta(@PathVariable UUID id, Model model) {
        CuentaCorrienteDetailResponse cuenta = cuentaService.obtenerPorId(id);
        model.addAttribute("cuenta", cuenta);
        return "admin/tributaria/cuentas-corrientes/detail";
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_EXPORTAR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarEstadoCuentaPdf(@PathVariable UUID id) {
        byte[] bytes = exportService.exportarEstadoCuentaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=estado_cuenta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('TRIBUTARIA_CUENTA_CORRIENTE_EXPORTAR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EstadoCuentaCorriente estado,
            @RequestParam(required = false) Boolean conDeuda
    ) {
        byte[] bytes = exportService.exportarCuentasCorrientesExcel(query, estado, conDeuda);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=padron_cuentas_corrientes_quinua.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
