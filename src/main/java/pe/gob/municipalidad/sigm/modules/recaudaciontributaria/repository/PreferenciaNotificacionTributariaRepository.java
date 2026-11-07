package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PreferenciaNotificacionTributaria;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreferenciaNotificacionTributariaRepository extends JpaRepository<PreferenciaNotificacionTributaria, UUID> {

    Optional<PreferenciaNotificacionTributaria> findByUsuarioIdAndContribuyenteId(Long usuarioId, UUID contribuyenteId);
}

