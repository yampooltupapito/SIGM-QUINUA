package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaTipoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoAdministrativoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoEntregaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoValidezLegalNotificacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificacionSpecification {

    public static Specification<NotificacionTributaria> conFiltros(
            String termino,
            UUID tipoId,
            CategoriaTipoNotificacion categoria,
            UUID medioId,
            EstadoAdministrativoNotificacion estadoAdm,
            EstadoEntregaNotificacion estadoEntrega,
            EstadoValidezLegalNotificacion estadoValidez,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (termino != null && !termino.isBlank()) {
                String likePattern = "%" + termino.trim().toLowerCase() + "%";
                Predicate pCodigo = cb.like(cb.lower(root.get("codigoNotificacion")), likePattern);
                Predicate pDest = cb.like(cb.lower(root.get("destinatarioNombre")), likePattern);
                Predicate pDoc = cb.like(cb.lower(root.get("destinatarioNumeroDoc")), likePattern);
                Predicate pDom = cb.like(cb.lower(root.get("domicilioNotificacion")), likePattern);
                predicates.add(cb.or(pCodigo, pDest, pDoc, pDom));
            }

            if (tipoId != null) {
                predicates.add(cb.equal(root.get("tipoNotificacion").get("id"), tipoId));
            }

            if (categoria != null) {
                predicates.add(cb.equal(root.get("tipoNotificacion").get("categoria"), categoria));
            }

            if (medioId != null) {
                predicates.add(cb.equal(root.get("medioNotificacion").get("id"), medioId));
            }

            if (estadoAdm != null) {
                predicates.add(cb.equal(root.get("estadoAdministrativo"), estadoAdm));
            }

            if (estadoEntrega != null) {
                predicates.add(cb.equal(root.get("estadoEntrega"), estadoEntrega));
            }

            if (estadoValidez != null) {
                predicates.add(cb.equal(root.get("estadoValidezLegal"), estadoValidez));
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaDesde.atStartOfDay().atOffset(java.time.ZoneOffset.UTC)));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaHasta.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

