package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.LoteNotificacionesTributarias;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoLoteNotificaciones;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoteNotificacionesTributariasRepository extends JpaRepository<LoteNotificacionesTributarias, UUID>, JpaSpecificationExecutor<LoteNotificacionesTributarias> {

    Optional<LoteNotificacionesTributarias> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    long countByEstado(EstadoLoteNotificaciones estado);

    @Query("SELECT COUNT(l) FROM LoteNotificacionesTributarias l")
    long countTotalLotes();
}

