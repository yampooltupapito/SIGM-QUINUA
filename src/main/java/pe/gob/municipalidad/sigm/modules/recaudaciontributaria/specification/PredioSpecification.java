package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.PredioFilterDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.RelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoPredio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Especificación de criterios para búsqueda y filtrado dinámico de predios tributarios.
 */
public class PredioSpecification {

    public static Specification<PredioTributario> conFiltro(PredioFilterDto filtro) {
        if (filtro == null) {
            return conFiltros(null, null, null, null, null);
        }
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getQuery() != null && !filtro.getQuery().trim().isEmpty()) {
                String search = "%" + filtro.getQuery().trim().toLowerCase() + "%";
                Predicate porCod = cb.like(cb.lower(root.get("codigoPredio")), search);
                Predicate porCat = cb.like(cb.lower(cb.coalesce(root.get("codigoCatastral"), "")), search);
                Predicate porDir = cb.like(cb.lower(root.get("direccion")), search);
                Predicate porSec = cb.like(cb.lower(cb.coalesce(root.get("sectorNombre"), "")), search);
                Predicate porCom = cb.like(cb.lower(cb.coalesce(root.get("comunidadCentroPoblado"), "")), search);
                Predicate porRef = cb.like(cb.lower(cb.coalesce(root.get("referenciaUbicacion"), "")), search);
                predicates.add(cb.or(porCod, porCat, porDir, porSec, porCom, porRef));
            }

            if (filtro.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipoPredio"), filtro.getTipo()));
            }

            if (filtro.getClasificacion() != null) {
                predicates.add(cb.equal(root.get("clasificacionPredio"), filtro.getClasificacion()));
            }

            if (filtro.getUso() != null) {
                predicates.add(cb.equal(root.get("usoDeclarado"), filtro.getUso()));
            }

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (filtro.getSectorId() != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), filtro.getSectorId()));
            }

            if (filtro.getSector() != null && !filtro.getSector().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(cb.coalesce(root.get("sectorNombre"), "")), "%" + filtro.getSector().trim().toLowerCase() + "%"));
            }

            if (filtro.getZona() != null && !filtro.getZona().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(cb.coalesce(root.get("zona"), "")), "%" + filtro.getZona().trim().toLowerCase() + "%"));
            }

            if (filtro.getContribuyenteId() != null) {
                Join<PredioTributario, RelacionContribuyentePredio> relJoin = root.join("relacionesContribuyente", JoinType.INNER);
                predicates.add(cb.equal(relJoin.get("contribuyente").get("id"), filtro.getContribuyenteId()));
                predicates.add(cb.equal(relJoin.get("estado"), EstadoRelacionContribuyentePredio.ACTIVO));
                criteriaQuery.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PredioTributario> conFiltros(
            String query,
            TipoPredio tipoPredio,
            EstadoPredio estado,
            UUID sectorId,
            UUID contribuyenteId) {

        return conFiltro(PredioFilterDto.builder()
                .query(query)
                .tipo(tipoPredio)
                .estado(estado)
                .sectorId(sectorId)
                .contribuyenteId(contribuyenteId)
                .build());
    }
}
