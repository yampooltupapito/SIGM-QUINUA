package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.LoteGeneracionObligaciones;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoteGeneracionObligacionesRepository extends JpaRepository<LoteGeneracionObligaciones, UUID> {

    @Query(value = "SELECT nextval('seq_tributaria_lote_obligacion')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<LoteGeneracionObligaciones> findByCodigoLote(String codigoLote);

    Page<LoteGeneracionObligaciones> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
