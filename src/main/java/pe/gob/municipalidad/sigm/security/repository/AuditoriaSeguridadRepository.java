package pe.gob.municipalidad.sigm.security.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.security.model.AuditoriaSeguridad;

import java.util.List;

@Repository
public interface AuditoriaSeguridadRepository extends JpaRepository<AuditoriaSeguridad, Long> {

    List<AuditoriaSeguridad> findByUsuarioOrderByCreatedAtDesc(String usuario);

    Page<AuditoriaSeguridad> findByTipoEvento(String tipoEvento, Pageable pageable);

    List<AuditoriaSeguridad> findByTraceId(String traceId);
}
