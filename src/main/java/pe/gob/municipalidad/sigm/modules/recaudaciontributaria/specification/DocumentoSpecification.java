package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DocumentoPagoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoDocumentoPagoTributario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentoSpecification {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    public static Specification<DocumentoPagoTributario> conFiltros(
            String query,
            UUID tipoDocumentoId,
            UUID serieId,
            UUID contribuyenteId,
            UUID pagoId,
            EstadoDocumentoPagoTributario estado,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toUpperCase() + "%";
                Predicate porCodigoDoc = cb.like(cb.upper(root.get("codigoDocumento")), pattern);
                Predicate porCodigoVerif = cb.like(cb.upper(root.get("codigoVerificacion")), pattern);
                Predicate porCodContrib = cb.like(cb.upper(root.get("contribuyente").get("codigoContribuyente")), pattern);
                Predicate porNombreContrib = cb.like(cb.upper(root.get("contribuyente").get("nombreRazonSocial")), pattern);
                Predicate porDocContrib = cb.like(cb.upper(root.get("contribuyente").get("numeroDocumento")), pattern);
                predicates.add(cb.or(porCodigoDoc, porCodigoVerif, porCodContrib, porNombreContrib, porDocContrib));
            }

            if (tipoDocumentoId != null) {
                predicates.add(cb.equal(root.get("tipoDocumento").get("id"), tipoDocumentoId));
            }

            if (serieId != null) {
                predicates.add(cb.equal(root.get("serie").get("id"), serieId));
            }

            if (contribuyenteId != null) {
                predicates.add(cb.equal(root.get("contribuyente").get("id"), contribuyenteId));
            }

            if (pagoId != null) {
                predicates.add(cb.equal(root.get("pagoTributario").get("id"), pagoId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (fechaDesde != null) {
                OffsetDateTime desde = fechaDesde.atStartOfDay(ZONA_LIMA).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaEmision"), desde));
            }

            if (fechaHasta != null) {
                OffsetDateTime hasta = fechaHasta.atTime(LocalTime.MAX).atZone(ZONA_LIMA).toOffsetDateTime();
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaEmision"), hasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
