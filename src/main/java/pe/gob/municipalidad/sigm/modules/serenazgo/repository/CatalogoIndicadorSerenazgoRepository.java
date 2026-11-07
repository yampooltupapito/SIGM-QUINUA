package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.CatalogoIndicadorSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CatalogoIndicadorSerenazgoRepository extends JpaRepository<CatalogoIndicadorSerenazgo, UUID> {

    Optional<CatalogoIndicadorSerenazgo> findByCodigo(String codigo);

    List<CatalogoIndicadorSerenazgo> findByModuloOrigen(String moduloOrigen);

    List<CatalogoIndicadorSerenazgo> findByEstadoOrderByModuloOrigenAscCodigoAsc(String estado);
}
