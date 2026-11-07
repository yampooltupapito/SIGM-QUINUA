package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialEstadoTerritorio;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialEstadoTerritorioRepository extends JpaRepository<HistorialEstadoTerritorio, UUID> {

    List<HistorialEstadoTerritorio> findByTipoEntidadAndEntidadIdOrderByCreatedAtDesc(
            TipoEntidadTerritorial tipoEntidad, UUID entidadId);
}
