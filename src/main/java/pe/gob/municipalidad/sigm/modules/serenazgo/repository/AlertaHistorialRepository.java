package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.AlertaHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertaHistorialRepository extends JpaRepository<AlertaHistorial, UUID> {
    List<AlertaHistorial> findByAlertaIdOrderByCreatedAtDesc(UUID alertaId);
    List<AlertaHistorial> findByAlertaIdOrderByCreatedAtAsc(UUID alertaId);
    List<AlertaHistorial> findByAlertaIdAndEsVisibleCiudadanoTrueOrderByCreatedAtAsc(UUID alertaId);
}
