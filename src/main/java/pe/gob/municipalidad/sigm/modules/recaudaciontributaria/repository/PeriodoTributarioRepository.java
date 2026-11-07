package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PeriodoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoPeriodoTributario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PeriodoTributarioRepository extends JpaRepository<PeriodoTributario, UUID> {

    Optional<PeriodoTributario> findByAnio(Integer anio);

    List<PeriodoTributario> findByEstado(EstadoPeriodoTributario estado);

    Page<PeriodoTributario> findAllByOrderByAnioDesc(Pageable pageable);

    List<PeriodoTributario> findAllByOrderByAnioDesc();

    boolean existsByAnio(Integer anio);
}
