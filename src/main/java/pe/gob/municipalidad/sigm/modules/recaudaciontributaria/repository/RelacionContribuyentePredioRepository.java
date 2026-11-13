package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.RelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelacionContribuyentePredioRepository extends JpaRepository<RelacionContribuyentePredio, UUID> {

    List<RelacionContribuyentePredio> findByContribuyenteId(UUID contribuyenteId);

    List<RelacionContribuyentePredio> findByContribuyenteIdAndEstado(UUID contribuyenteId, EstadoRelacionContribuyentePredio estado);

    List<RelacionContribuyentePredio> findByPredioId(UUID predioId);

    List<RelacionContribuyentePredio> findByPredioIdAndEstado(UUID predioId, EstadoRelacionContribuyentePredio estado);

    Optional<RelacionContribuyentePredio> findByContribuyenteIdAndPredioId(UUID contribuyenteId, UUID predioId);

    // Variantes que toleran varias filas para el mismo contribuyente y predio (p. ej. una relación HISTORICA
    // y otra ACTIVA tras desvincular y volver a vincular). Se usan en el cálculo de arbitrios.
    Optional<RelacionContribuyentePredio> findFirstByContribuyenteIdAndPredioIdAndEstadoOrderByFechaInicioDesc(
            UUID contribuyenteId, UUID predioId, EstadoRelacionContribuyentePredio estado);

    Optional<RelacionContribuyentePredio> findFirstByContribuyenteIdAndPredioIdOrderByFechaInicioDesc(
            UUID contribuyenteId, UUID predioId);

    boolean existsByContribuyenteIdAndPredioIdAndEstado(UUID contribuyenteId, UUID predioId, EstadoRelacionContribuyentePredio estado);
}
