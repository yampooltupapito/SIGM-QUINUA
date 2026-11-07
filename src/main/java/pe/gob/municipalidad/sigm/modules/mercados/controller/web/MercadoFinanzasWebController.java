package pe.gob.municipalidad.sigm.modules.mercados.controller.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.modules.mercados.dto.CuentaCorrienteResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ObligacionDetailResponse;
import pe.gob.municipalidad.sigm.modules.mercados.dto.PagoResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.CajaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.repository.CajaMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard/mercados")
public class MercadoFinanzasWebController {

    private final ConceptoCobroMercadoService conceptoService;
    private final TarifaMercadoService tarifaService;
    private final ObligacionMercadoService obligacionService;
    private final PagoMercadoService pagoService;
    private final CajaMercadoService cajaService;
    private final CuentaCorrienteMercadoService cuentaCorrienteService;
    private final FinanzasMercadoReporteService reporteService;
    private final MercadoMunicipalRepository mercadoRepository;
    private final CajaMercadoRepository cajaRepository;

    public MercadoFinanzasWebController(ConceptoCobroMercadoService conceptoService,
                                       TarifaMercadoService tarifaService,
                                       ObligacionMercadoService obligacionService,
                                       PagoMercadoService pagoService,
                                       CajaMercadoService cajaService,
                                       CuentaCorrienteMercadoService cuentaCorrienteService,
                                       FinanzasMercadoReporteService reporteService,
                                       MercadoMunicipalRepository mercadoRepository,
                                       CajaMercadoRepository cajaRepository) {
        this.conceptoService = conceptoService;
        this.tarifaService = tarifaService;
        this.obligacionService = obligacionService;
        this.pagoService = pagoService;
        this.cajaService = cajaService;
        this.cuentaCorrienteService = cuentaCorrienteService;
        this.reporteService = reporteService;
        this.mercadoRepository = mercadoRepository;
        this.cajaRepository = cajaRepository;
    }

    @GetMapping("/finanzas")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String dashboardFinanzas(Model model) {
        model.addAttribute("estadisticas", reporteService.obtenerEstadisticas());
        return "admin/mercados/finanzas/dashboard";
    }

    @GetMapping("/conceptos")
    @PreAuthorize("hasAuthority('MERCADOS_CONCEPTO_GESTIONAR') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listarConceptos(Model model) {
        model.addAttribute("conceptos", conceptoService.buscarPaginado(null, null, PageRequest.of(0, 100)).getContent());
        return "admin/mercados/conceptos/listado";
    }

    @GetMapping("/tarifas")
    @PreAuthorize("hasAuthority('MERCADOS_TARIFA_GESTIONAR') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listarTarifas(Model model) {
        model.addAttribute("tarifas", tarifaService.buscarPaginado(null, null, null, PageRequest.of(0, 100)).getContent());
        model.addAttribute("conceptos", conceptoService.listarActivos());
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/tarifas/listado";
    }

    @GetMapping("/obligaciones")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listarObligaciones(Model model) {
        model.addAttribute("conceptos", conceptoService.listarActivos());
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/obligaciones/listado";
    }

    @GetMapping("/obligaciones/nueva")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_CREAR') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioNuevaObligacion(
            @RequestParam(required = false) UUID comercianteId,
            @RequestParam(required = false) UUID asignacionId,
            Model model) {
        model.addAttribute("comercianteId", comercianteId);
        model.addAttribute("asignacionId", asignacionId);
        model.addAttribute("conceptos", conceptoService.listarActivos());
        model.addAttribute("mercados", mercadoRepository.findAll());
        return "admin/mercados/obligaciones/formulario";
    }

    @GetMapping("/obligaciones/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String detalleObligacion(@PathVariable UUID id, Model model) {
        ObligacionDetailResponse obligacion = obligacionService.obtenerDetalle(id);
        model.addAttribute("obligacion", obligacion);
        return "admin/mercados/obligaciones/detalle";
    }

    @GetMapping("/pagos")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String listarPagos(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("cajas", cajaRepository.findAll());
        return "admin/mercados/pagos/listado";
    }

    @GetMapping("/pagos/nuevo")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_REGISTRAR') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String formularioNuevoPago(
            @RequestParam(required = false) UUID comercianteId,
            Model model) {
        model.addAttribute("comercianteIdPreseleccionado", comercianteId);
        List<CajaMercado> cajas = cajaRepository.findByEstado("ACTIVA");
        if (cajas.isEmpty()) {
            cajas = cajaRepository.findAll();
        }
        if (cajas.isEmpty()) {
            MercadoMunicipal primerMercado = mercadoRepository.findAll().stream().findFirst().orElse(null);
            CajaMercado cajaDefecto = cajaRepository.save(CajaMercado.builder()
                    .codigo("CAJA-MKT-01")
                    .nombre("Caja Principal Recaudación Mercados")
                    .mercado(primerMercado)
                    .estado("ACTIVA")
                    .build());
            cajas = List.of(cajaDefecto);
        }
        model.addAttribute("cajas", cajas);
        return "admin/mercados/pagos/formulario";
    }

    @GetMapping("/pagos/{id}")
    @PreAuthorize("hasAuthority('MERCADOS_PAGO_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String detallePago(@PathVariable UUID id, Model model) {
        PagoResponse pago = pagoService.obtenerPorId(id);
        model.addAttribute("pago", pago);
        return "admin/mercados/pagos/detalle";
    }

    @GetMapping("/cuentas-corrientes/{comercianteId}")
    @PreAuthorize("hasAuthority('MERCADOS_OBLIGACION_VER') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String verCuentaCorriente(@PathVariable UUID comercianteId, Model model) {
        CuentaCorrienteResponse cc = cuentaCorrienteService.obtenerCuentaCorrienteComerciante(comercianteId);
        model.addAttribute("cuentaCorriente", cc);
        return "admin/mercados/cuentas-corrientes/detalle";
    }

    @GetMapping("/caja")
    @PreAuthorize("hasAuthority('MERCADOS_CAJA_ABRIR') or hasAuthority('MERCADOS_CAJA_CERRAR') or hasAuthority('MERCADOS_PAGO_REGISTRAR') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String vistaCaja(Model model) {
        List<CajaMercado> cajas = cajaRepository.findByEstado("ACTIVA");
        if (cajas.isEmpty()) {
            cajas = cajaRepository.findAll();
        }
        if (cajas.isEmpty()) {
            MercadoMunicipal primerMercado = mercadoRepository.findAll().stream().findFirst().orElse(null);
            CajaMercado cajaDefecto = cajaRepository.save(CajaMercado.builder()
                    .codigo("CAJA-MKT-01")
                    .nombre("Caja Principal Recaudación Mercados")
                    .mercado(primerMercado)
                    .estado("ACTIVA")
                    .build());
            cajas = List.of(cajaDefecto);
        }
        model.addAttribute("cajas", cajas);
        return "admin/mercados/caja/sesion";
    }

    @GetMapping("/finanzas/reportes")
    @PreAuthorize("hasAuthority('MERCADOS_FINANZAS_EXPORTAR') or hasAuthority('MERCADOS_FINANZAS_VER') or hasAnyRole('ADMIN', 'SUPERADMIN')")
    public String reportesFinanzas(Model model) {
        model.addAttribute("mercados", mercadoRepository.findAll());
        model.addAttribute("cajas", cajaRepository.findAll());
        return "admin/mercados/finanzas/reportes";
    }
}
