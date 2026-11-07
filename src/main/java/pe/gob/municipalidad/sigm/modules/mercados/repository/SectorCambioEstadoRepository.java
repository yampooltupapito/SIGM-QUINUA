package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SectorCambioEstado;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectorCambioEstadoRepository extends JpaRepository<SectorCambioEstado, UUID> {

    List<SectorCambioEstado> findBySectorIdOrderByFechaCambioDesc(UUID sectorId);
}
