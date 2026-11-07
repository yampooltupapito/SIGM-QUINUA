package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoExigibilidadObligacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoObligacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TramoAntiguedadDeuda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CarteraDeudaSpecification {

    public static Specification<ObligacionTributaria> filtrarCartera(
            String query,
            UUID contribuyenteId,
            UUID predioId,
            UUID conceptoId,
            UUID periodoId,
            EstadoObligacionTributaria estado,
            EstadoExigibilidadObligacion exigibilidad,
            Boolean soloConSaldo,
            LocalDate fechaCorte,
            TramoAntiguedadDeuda tramo) {

        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Excluir estados no transaccionales de deuda
            predicates.add(root.get("estado").in(List.of(
                    EstadoObligacionTributaria.ANULADA,
                    EstadoObligacionTributaria.BORRADOR,
                    EstadoObligacionTributaria.RECTIFICADA
            )).not());

            if (soloConSaldo == null || soloConSaldo) {
                predicates.add(cb.greaterThan(root.get("saldo"), BigDecimal.ZERO));
            }

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoObligacion")), pattern);
                Predicate porNombre = cb.like(cb.lower(root.join("contribuyente").get("nombreRazonSocial")), pattern);
                Predicate porDoc = cb.like(cb.lower(root.join("contribuyente").get("numeroDocumento")), pattern);
                predicates.add(cb.or(porCodigo, porNombre, porDoc));
            }

            if (contribuyenteId != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), contribuyenteId));
            }

            if (predioId != null) {
                predicates.add(cb.equal(root.get("predio").get("id"), predioId));
            }

            if (conceptoId != null) {
                predicates.add(cb.equal(root.get("concepto").get("id"), conceptoId));
            }

            if (periodoId != null) {
                predicates.add(cb.equal(root.get("periodo").get("id"), periodoId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (exigibilidad != null) {
                predicates.add(cb.equal(root.get("estadoExigibilidad"), exigibilidad));
            }

            LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();

            if (tramo != null) {
                switch (tramo) {
                    case NO_VENCIDA -> {
                        predicates.add(cb.or(
                                cb.isNull(root.get("fechaVencimiento")),
                                cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), corte)
                        ));
                    }
                    case DE_01_A_30_DIAS -> {
                        predicates.add(cb.lessThan(root.get("fechaVencimiento"), corte));
                        predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), corte.minusDays(30)));
                    }
                    case DE_31_A_60_DIAS -> {
                        predicates.add(cb.lessThan(root.get("fechaVencimiento"), corte.minusDays(30)));
                        predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), corte.minusDays(60)));
                    }
                    case DE_61_A_90_DIAS -> {
                        predicates.add(cb.lessThan(root.get("fechaVencimiento"), corte.minusDays(60)));
                        predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), corte.minusDays(90)));
                    }
                    case MAS_DE_90_DIAS -> {
                        predicates.add(cb.lessThan(root.get("fechaVencimiento"), corte.minusDays(90)));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
