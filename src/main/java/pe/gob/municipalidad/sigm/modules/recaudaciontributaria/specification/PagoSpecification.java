package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.MedioPagoTributario;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PagoSpecification {

    public static Specification<PagoTributario> conFiltros(String busqueda,
                                                           UUID contribuyenteId,
                                                           UUID sesionCajaId,
                                                           MedioPagoTributario medioPago,
                                                           EstadoPagoTributario estado,
                                                           OffsetDateTime fechaDesde,
                                                           OffsetDateTime fechaHasta) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String term = "%" + busqueda.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigoPago")), term);
                Predicate porReferencia = cb.like(cb.lower(root.get("referenciaExterna")), term);
                Predicate porNumeroOperacion = cb.like(cb.lower(root.get("numeroOperacionBancaria")), term);
                Predicate porDocContrib = cb.like(cb.lower(root.join("contribuyente").get("numeroDocumento")), term);
                Predicate porNombreContrib = cb.like(cb.lower(root.join("contribuyente").get("nombreRazonSocial")), term);
                predicates.add(cb.or(porCodigo, porReferencia, porNumeroOperacion, porDocContrib, porNombreContrib));
            }

            if (contribuyenteId != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), contribuyenteId));
            }

            if (sesionCajaId != null) {
                predicates.add(cb.equal(root.get("sesionCaja").get("id"), sesionCajaId));
            }

            if (medioPago != null) {
                predicates.add(cb.equal(root.get("medioPago"), medioPago));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHoraPago"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaHoraPago"), fechaHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
