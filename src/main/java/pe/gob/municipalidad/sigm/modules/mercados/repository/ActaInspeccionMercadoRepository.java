package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActaInspeccionMercado;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActaInspeccionMercadoRepository extends JpaRepository<ActaInspeccionMercado, UUID> {

    @Query(value = "SELECT nextval('mercados_actas_inspeccion_seq')", nativeQuery = true)
    Long obtenerSiguienteCorrelativo();

    Optional<ActaInspeccionMercado> findByInspeccionId(UUID inspeccionId);

    Optional<ActaInspeccionMercado> findByCodigoActa(String codigoActa);

    Optional<ActaInspeccionMercado> findByCodigoVerificacion(String codigoVerificacion);

    boolean existsByInspeccionId(UUID inspeccionId);
}
