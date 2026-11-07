package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.HistorialDelimitacionTerritorial;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoEntidadTerritorial;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistorialDelimitacionTerritorialRepository extends JpaRepository<HistorialDelimitacionTerritorial, UUID> {

    List<HistorialDelimitacionTerritorial> findByTipoEntidadAndEntidadIdOrderByNumeroVersionDesc(
            TipoEntidadTerritorial tipoEntidad, UUID entidadId);

    Optional<HistorialDelimitacionTerritorial> findTopByTipoEntidadAndEntidadIdOrderByNumeroVersionDesc(
            TipoEntidadTerritorial tipoEntidad, UUID entidadId);

    long countByTipoEntidadAndEntidadId(TipoEntidadTerritorial tipoEntidad, UUID entidadId);
}
