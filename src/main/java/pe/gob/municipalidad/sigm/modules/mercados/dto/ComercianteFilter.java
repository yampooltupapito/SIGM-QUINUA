package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.persistence.criteria.*;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.PersonaJuridica;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActividadComercial;
import pe.gob.municipalidad.sigm.modules.mercados.entity.AsignacionPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.entity.Comerciante;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Filtro multicriterio para el padrón de comerciantes municipales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercianteFilter {

    private String termino;
    private TipoComerciante tipoComerciante;
    private EstadoComerciante estado;
    private Long actividadId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private UUID mercadoId;

    public Specification<Comerciante> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);
            }

            // Filtro por Tipo de Comerciante
            if (tipoComerciante != null) {
                predicates.add(cb.equal(root.get("tipoComerciante"), tipoComerciante));
            }

            // Filtro por Estado
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            // Filtro por Rango de Fechas de Registro
            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaRegistro"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaRegistro"), fechaHasta));
            }

            // Filtro por Término de Búsqueda
            if (termino != null && !termino.trim().isEmpty()) {
                String pattern = "%" + termino.trim().toLowerCase() + "%";
                Join<Comerciante, Persona> joinPersona = root.join("persona", JoinType.LEFT);
                Join<Comerciante, PersonaJuridica> joinPj = root.join("personaJuridica", JoinType.LEFT);

                Predicate porCodigo = cb.like(cb.lower(root.get("codigoComerciante")), pattern);
                Predicate porNombres = cb.like(cb.lower(joinPersona.get("nombres")), pattern);
                Predicate porPaterno = cb.like(cb.lower(joinPersona.get("apellidoPaterno")), pattern);
                Predicate porMaterno = cb.like(cb.lower(joinPersona.get("apellidoMaterno")), pattern);
                Predicate porDocNatural = cb.like(cb.lower(joinPersona.get("numeroDocumento")), pattern);

                Predicate porRazonSocial = cb.like(cb.lower(joinPj.get("razonSocial")), pattern);
                Predicate porNombreComercial = cb.like(cb.lower(joinPj.get("nombreComercial")), pattern);
                Predicate porRuc = cb.like(cb.lower(joinPj.get("ruc")), pattern);

                predicates.add(cb.or(
                        porCodigo, porNombres, porPaterno, porMaterno, porDocNatural,
                        porRazonSocial, porNombreComercial, porRuc
                ));
            }

            // Filtro por Actividad Comercial
            if (actividadId != null) {
                Join<Comerciante, ActividadComercial> joinActividad = root.join("actividades", JoinType.INNER);
                predicates.add(cb.equal(joinActividad.get("id"), actividadId));
            }

            // Filtro por Mercado Relacionado (a través de Asignación de Puesto)
            if (mercadoId != null && query != null) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<AsignacionPuesto> asignacionRoot = subquery.from(AsignacionPuesto.class);
                Join<AsignacionPuesto, PuestoComercial> puestoJoin = asignacionRoot.join("puesto");

                subquery.select(asignacionRoot.get("comerciante").get("id"))
                        .where(
                                cb.equal(puestoJoin.get("mercadoId"), mercadoId),
                                cb.equal(asignacionRoot.get("comerciante").get("id"), root.get("id"))
                        );

                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
