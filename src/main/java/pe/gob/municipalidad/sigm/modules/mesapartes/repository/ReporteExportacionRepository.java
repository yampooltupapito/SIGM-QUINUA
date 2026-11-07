package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ReporteExportacion;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReporteExportacionRepository extends JpaRepository<ReporteExportacion, Long> {

    Optional<ReporteExportacion> findByUuid(UUID uuid);

    @Query("SELECT r FROM ReporteExportacion r LEFT JOIN FETCH r.usuario u LEFT JOIN FETCH u.persona WHERE r.usuario.id = :usuarioId ORDER BY r.fechaExportacion DESC")
    Page<ReporteExportacion> findByUsuarioIdOrderByFechaExportacionDesc(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT r FROM ReporteExportacion r LEFT JOIN FETCH r.usuario u LEFT JOIN FETCH u.persona ORDER BY r.fechaExportacion DESC")
    Page<ReporteExportacion> findAllWithUsuario(Pageable pageable);
}
