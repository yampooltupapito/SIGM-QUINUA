package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.HistorialAccesoPortalTributario;

import java.util.UUID;

@Repository
public interface HistorialAccesoPortalTributarioRepository extends JpaRepository<HistorialAccesoPortalTributario, UUID> {

    Page<HistorialAccesoPortalTributario> findByUsuarioIdOrderByFechaAccesoDesc(Long usuarioId, Pageable pageable);

    Page<HistorialAccesoPortalTributario> findByContribuyenteIdOrderByFechaAccesoDesc(UUID contribuyenteId, Pageable pageable);
}

