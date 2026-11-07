package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipificacionInfraccion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipificacionInfraccionRepository extends JpaRepository<TipificacionInfraccion, UUID> {

    Optional<TipificacionInfraccion> findByCodigo(String codigo);

    List<TipificacionInfraccion> findByEstadoOrderByCodigoAsc(String estado);

    @Query("SELECT t FROM TipificacionInfraccion t WHERE t.estado = 'ACTIVO' AND " +
           "(t.fechaFinVigencia IS NULL OR t.fechaFinVigencia >= :fecha) AND " +
           "t.fechaInicioVigencia <= :fecha ORDER BY t.codigo ASC")
    List<TipificacionInfraccion> findVigentes(@Param("fecha") LocalDate fecha);

    @Query("SELECT t FROM TipificacionInfraccion t WHERE " +
           "(:estado IS NULL OR t.estado = :estado) AND " +
           "(:q IS NULL OR LOWER(t.codigo) LIKE CAST(:q AS string) OR LOWER(t.descripcion) LIKE CAST(:q AS string))")
    Page<TipificacionInfraccion> buscarPaginado(
            @Param("estado") String estado,
            @Param("q") String q,
            Pageable pageable);
}
