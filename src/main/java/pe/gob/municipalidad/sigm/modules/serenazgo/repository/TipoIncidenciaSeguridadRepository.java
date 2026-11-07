package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.TipoIncidenciaSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.CategoriaIncidenciaSeguridad;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoIncidenciaSeguridadRepository extends JpaRepository<TipoIncidenciaSeguridad, UUID> {
    Optional<TipoIncidenciaSeguridad> findByCodigo(String codigo);
    List<TipoIncidenciaSeguridad> findByActivoTrue();
    List<TipoIncidenciaSeguridad> findByActivoTrueOrderByNombreAsc();
    List<TipoIncidenciaSeguridad> findByCategoriaAndActivoTrue(CategoriaIncidenciaSeguridad categoria);
    boolean existsByCodigo(String codigo);
}
