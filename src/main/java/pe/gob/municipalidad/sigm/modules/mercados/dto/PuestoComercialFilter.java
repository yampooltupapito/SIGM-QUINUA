package pe.gob.municipalidad.sigm.modules.mercados.dto;

import jakarta.persistence.criteria.Predicate;
import lombok.*;
import org.springframework.data.jpa.domain.Specification;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Criterios de filtrado y búsqueda avanzada para el padrón de puestos comerciales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuestoComercialFilter {

    private UUID mercadoId;
    private String termino;
    private UUID sectorId;
    private UUID pabellonId;
    private UUID zonaId;
    private Long tipoPuestoId;
    private EstadoFisicoPuesto estadoFisico;
    private EstadoPuesto estado;
    private Integer nivel;
    private BigDecimal minArea;
    private BigDecimal maxArea;
    @Builder.Default
    private String vista = "tabla";

    /**
     * Convierte los filtros configurados en una especificación JPA reutilizable.
     */
    public Specification<PuestoComercial> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (mercadoId != null) {
                predicates.add(cb.equal(root.get("mercado").get("id"), mercadoId));
            }

            if (termino != null && !termino.isBlank()) {
                String likePattern = "%" + termino.trim().toLowerCase() + "%";
                Predicate porCodigo = cb.like(cb.lower(root.get("codigo")), likePattern);
                Predicate porNumero = cb.like(cb.lower(root.get("numero")), likePattern);
                Predicate porNombre = cb.like(cb.lower(root.get("nombreReferencial")), likePattern);
                Predicate porUbicacion = cb.like(cb.lower(root.get("ubicacionReferencia")), likePattern);
                predicates.add(cb.or(porCodigo, porNumero, porNombre, porUbicacion));
            }

            if (sectorId != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), sectorId));
            }
            if (pabellonId != null) {
                predicates.add(cb.equal(root.get("pabellon").get("id"), pabellonId));
            }
            if (zonaId != null) {
                predicates.add(cb.equal(root.get("zona").get("id"), zonaId));
            }
            if (tipoPuestoId != null) {
                predicates.add(cb.equal(root.get("tipoPuesto").get("id"), tipoPuestoId));
            }
            if (estadoFisico != null) {
                predicates.add(cb.equal(root.get("estadoFisico"), estadoFisico));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (nivel != null) {
                predicates.add(cb.equal(root.get("nivel"), nivel));
            }
            if (minArea != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("areaMetrosCuadrados"), minArea));
            }
            if (maxArea != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("areaMetrosCuadrados"), maxArea));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
