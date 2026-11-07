package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.DefinicionReporteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.CategoriaReporteTributario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DefinicionReporteTributarioRepository extends JpaRepository<DefinicionReporteTributario, UUID> {

    Optional<DefinicionReporteTributario> findByCodigo(String codigo);

    List<DefinicionReporteTributario> findByActivoTrueOrderByNombreAsc();

    List<DefinicionReporteTributario> findByCategoriaAndActivoTrueOrderByNombreAsc(CategoriaReporteTributario categoria);
}

