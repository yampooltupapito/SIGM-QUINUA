package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.SupervisionSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSupervisionSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupervisionSerenazgoRepository extends JpaRepository<SupervisionSerenazgo, UUID> {

    @Query(value = "SELECT nextval('seq_serenazgo_supervision')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<SupervisionSerenazgo> findByCodigo(String codigo);

    List<SupervisionSerenazgo> findByEstado(EstadoSupervisionSerenazgo estado);

    Page<SupervisionSerenazgo> findByEstado(EstadoSupervisionSerenazgo estado, Pageable pageable);

    List<SupervisionSerenazgo> findBySupervisorId(UUID supervisorId);

    long countByEstado(EstadoSupervisionSerenazgo estado);

    boolean existsByCodigo(String codigo);
}
