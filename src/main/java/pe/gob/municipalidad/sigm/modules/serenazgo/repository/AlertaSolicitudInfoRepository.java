package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AlertaSolicitudInfo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoSolicitudInfoAlerta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaSolicitudInfoRepository extends JpaRepository<AlertaSolicitudInfo, UUID> {
    List<AlertaSolicitudInfo> findByAlertaIdOrderByFechaSolicitudDesc(UUID alertaId);
    Optional<AlertaSolicitudInfo> findFirstByAlertaIdAndEstadoOrderByFechaSolicitudDesc(UUID alertaId, EstadoSolicitudInfoAlerta estado);
}
