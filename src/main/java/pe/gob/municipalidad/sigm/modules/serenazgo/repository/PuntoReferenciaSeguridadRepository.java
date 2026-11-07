package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PuntoReferenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSectorPatrullaje;

import java.util.List;
import java.util.UUID;

@Repository
public interface PuntoReferenciaSeguridadRepository extends JpaRepository<PuntoReferenciaSeguridad, UUID> {
    List<PuntoReferenciaSeguridad> findBySectorIdAndEstado(UUID sectorId, EstadoSectorPatrullaje estado);
    List<PuntoReferenciaSeguridad> findByZonaIdAndEstado(UUID zonaId, EstadoSectorPatrullaje estado);
}
