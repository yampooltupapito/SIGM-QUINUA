package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.CargoOperativoSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CargoOperativoSerenazgoRepository extends JpaRepository<CargoOperativoSerenazgo, UUID> {
    Optional<CargoOperativoSerenazgo> findByCodigo(String codigo);
    List<CargoOperativoSerenazgo> findByActivoTrueOrderByNivelJerarquicoAsc();
    boolean existsByCodigo(String codigo);
}
