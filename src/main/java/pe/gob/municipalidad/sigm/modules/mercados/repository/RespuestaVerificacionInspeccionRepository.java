package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.RespuestaVerificacionInspeccion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RespuestaVerificacionInspeccionRepository extends JpaRepository<RespuestaVerificacionInspeccion, UUID> {

    List<RespuestaVerificacionInspeccion> findByInspeccionId(UUID inspeccionId);

    Optional<RespuestaVerificacionInspeccion> findByInspeccionIdAndItemVerificacionId(UUID inspeccionId, UUID itemVerificacionId);
}
