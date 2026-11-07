package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MovimientoCuentaCorrienteTributaria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovimientoCuentaCorrienteTributariaRepository extends JpaRepository<MovimientoCuentaCorrienteTributaria, UUID> {

    @Query(value = "SELECT nextval('seq_tributaria_movimiento_cta')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    List<MovimientoCuentaCorrienteTributaria> findByCuentaCorrienteIdOrderByNumeroSecuenciaAsc(UUID cuentaCorrienteId);

    Page<MovimientoCuentaCorrienteTributaria> findByCuentaCorrienteIdOrderByNumeroSecuenciaDesc(UUID cuentaCorrienteId, Pageable pageable);

    List<MovimientoCuentaCorrienteTributaria> findByObligacionIdOrderByNumeroSecuenciaAsc(UUID obligacionId);

    @Query("SELECT COALESCE(MAX(m.numeroSecuencia), 0) FROM MovimientoCuentaCorrienteTributaria m WHERE m.cuentaCorriente.id = :cuentaCorrienteId")
    Long findUltimoNumeroSecuenciaByCuentaId(@Param("cuentaCorrienteId") UUID cuentaCorrienteId);

    long countByCuentaCorrienteId(UUID cuentaCorrienteId);
}
