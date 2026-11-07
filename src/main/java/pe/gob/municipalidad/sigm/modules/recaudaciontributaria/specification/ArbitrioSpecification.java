package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ArbitrioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CalculoArbitrioMunicipal;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ArbitrioSpecification {

    public static Specification<CalculoArbitrioMunicipal> conFiltros(ArbitrioFilterDto filtro) {
        return conFiltro(filtro);
    }

    public static Specification<CalculoArbitrioMunicipal> conFiltro(ArbitrioFilterDto filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getQuery() != null && !filtro.getQuery().trim().isEmpty()) {
                String pattern = "%" + filtro.getQuery().trim().toUpperCase() + "%";
                Predicate porCodigo = cb.like(cb.upper(root.get("codigoCalculo")), pattern);
                Predicate porContribuyente = cb.like(cb.upper(root.get("contribuyente").get("nombreRazonSocial")), pattern);
                Predicate porDoc = cb.like(root.get("contribuyente").get("numeroDocumento"), pattern);
                Predicate porPredio = cb.like(cb.upper(root.get("predio").get("codigoPredio")), pattern);
                Predicate porDireccion = cb.like(cb.upper(root.get("predio").get("direccion")), pattern);
                predicates.add(cb.or(porCodigo, porContribuyente, porDoc, porPredio, porDireccion));
            }

            if (filtro.getEjercicioFiscal() != null) {
                predicates.add(cb.equal(root.get("periodo").get("anio"), filtro.getEjercicioFiscal()));
            }

            if (filtro.getPeriodoId() != null) {
                predicates.add(cb.equal(root.get("periodo").get("id"), filtro.getPeriodoId()));
            }

            if (filtro.getContribuyenteId() != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), filtro.getContribuyenteId()));
            }

            if (filtro.getPredioId() != null) {
                predicates.add(cb.equal(root.get("predio").get("id"), filtro.getPredioId()));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getTipoCalculo() != null) {
                predicates.add(cb.equal(root.get("tipoCalculo"), filtro.getTipoCalculo()));
            }

            if (filtro.getFechaDesde() != null) {
                OffsetDateTime desde = filtro.getFechaDesde().atStartOfDay().atOffset(ZoneOffset.ofHours(-5));
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCalculo"), desde));
            }

            if (filtro.getFechaHasta() != null) {
                OffsetDateTime hasta = filtro.getFechaHasta().atTime(LocalTime.MAX).atOffset(ZoneOffset.ofHours(-5));
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCalculo"), hasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
