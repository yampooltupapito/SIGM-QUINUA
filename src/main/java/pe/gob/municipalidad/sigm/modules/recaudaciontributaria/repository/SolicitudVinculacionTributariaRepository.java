package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.SolicitudVinculacionTributaria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudVinculacionTributariaRepository extends JpaRepository<SolicitudVinculacionTributaria, UUID> {

    Optional<SolicitudVinculacionTributaria> findByCodigoSolicitud(String codigoSolicitud);

    List<SolicitudVinculacionTributaria> findByVinculacionIdOrderByCreatedAtDesc(UUID vinculacionId);

    Page<SolicitudVinculacionTributaria> findByEstado(String estado, Pageable pageable);
}

