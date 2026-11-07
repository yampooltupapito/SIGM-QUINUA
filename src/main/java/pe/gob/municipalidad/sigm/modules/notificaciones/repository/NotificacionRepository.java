package pe.gob.municipalidad.sigm.modules.notificaciones.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.notificaciones.model.Notificacion;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Page<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    List<Notificacion> findTop5ByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = :ahora WHERE n.usuario.id = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId, @Param("ahora") OffsetDateTime ahora);

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId " +
            "AND (:leida IS NULL OR n.leida = :leida) " +
            "AND (:tipo IS NULL OR :tipo = '' OR n.tipo = :tipo) " +
            "AND (:termino IS NULL OR :termino = '' OR LOWER(n.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(n.mensaje) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
            "ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> buscarNotificacionesFiltradas(
            @Param("usuarioId") Long usuarioId,
            @Param("leida") Boolean leida,
            @Param("tipo") String tipo,
            @Param("termino") String termino,
            Pageable pageable
    );
}
