package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConvenioFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConvenioFraccionamiento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConvenioFraccionamientoSpecification {

    public static Specification<ConvenioFraccionamientoTributario> conFiltros(
            String search,
            UUID contribuyenteId,
            UUID programaId,
            EstadoConvenioFraccionamiento estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoConvenio")), pattern);
                Predicate porRes = cb.like(cb.lower(root.get("resolucionAprobacion")), pattern);
                Predicate porDoc = cb.like(cb.lower(root.get("contribuyente").get("numeroDocumento")), pattern);
                Predicate porNombre = cb.like(cb.lower(root.get("contribuyente").get("nombreRazonSocial")), pattern);
                predicates.add(cb.or(porCodigo, porRes, porDoc, porNombre));
            }

            if (contribuyenteId != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), contribuyenteId));
            }

            if (programaId != null) {
                predicates.add(cb.equal(root.get("programa").get("id"), programaId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaAprobacion"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaAprobacion"), fechaHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
