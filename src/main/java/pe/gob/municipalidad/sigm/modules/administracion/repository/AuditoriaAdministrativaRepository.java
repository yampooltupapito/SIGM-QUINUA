package pe.gob.municipalidad.sigm.modules.administracion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.administracion.entity.AuditoriaAdministrativa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditoriaAdministrativaRepository extends JpaRepository<AuditoriaAdministrativa, Long>, JpaSpecificationExecutor<AuditoriaAdministrativa> {

    Page<AuditoriaAdministrativa> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditoriaAdministrativa> findByResultadoOrderByCreatedAtDesc(String resultado, Pageable pageable);

    Page<AuditoriaAdministrativa> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(OffsetDateTime desde, Pageable pageable);

    Page<AuditoriaAdministrativa> findByCreatedAtBetweenOrderByCreatedAtDesc(OffsetDateTime desde, OffsetDateTime hasta, Pageable pageable);

    Page<AuditoriaAdministrativa> findByUsuarioResponsableOrderByCreatedAtDesc(String usuarioResponsable, Pageable pageable);

    @Query("SELECT a.accion, COUNT(a) FROM AuditoriaAdministrativa a GROUP BY a.accion ORDER BY COUNT(a) DESC")
    List<Object[]> countGroupedByAccion();

    @Query("SELECT a.resultado, COUNT(a) FROM AuditoriaAdministrativa a GROUP BY a.resultado")
    List<Object[]> countGroupedByResultado();
}
