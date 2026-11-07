package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MercadoMunicipalRepository extends JpaRepository<MercadoMunicipal, UUID> {

    Optional<MercadoMunicipal> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<MercadoMunicipal> findByEstado(EstadoMercado estado);

    Page<MercadoMunicipal> findByEstado(EstadoMercado estado, Pageable pageable);

    @Query("SELECT m FROM MercadoMunicipal m WHERE " +
           "LOWER(m.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(m.direccion) LIKE LOWER(CONCAT('%', :termino, '%'))")
    Page<MercadoMunicipal> buscarPorTermino(@Param("termino") String termino, Pageable pageable);

    @Query("SELECT m FROM MercadoMunicipal m WHERE " +
           "(LOWER(m.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(m.direccion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "AND m.estado = :estado")
    Page<MercadoMunicipal> buscarPorTerminoYEstado(
            @Param("termino") String termino,
            @Param("estado") EstadoMercado estado,
            Pageable pageable
    );

    @Query("SELECT m FROM MercadoMunicipal m WHERE " +
           "(:estado IS NULL OR m.estado = :estado) " +
           "AND (:oficinaId IS NULL OR (m.oficinaAdministradora IS NOT NULL AND m.oficinaAdministradora.id = :oficinaId)) " +
           "AND (:distrito IS NULL OR LOWER(m.distrito) = :distrito) " +
           "AND (:fechaDesde IS NULL OR m.fechaRegistro >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR m.fechaRegistro <= :fechaHasta)")
    Page<MercadoMunicipal> filtrarMercadosSinTermino(
            @Param("estado") EstadoMercado estado,
            @Param("oficinaId") Long oficinaId,
            @Param("distrito") String distrito,
            @Param("fechaDesde") java.time.LocalDate fechaDesde,
            @Param("fechaHasta") java.time.LocalDate fechaHasta,
            Pageable pageable
    );

    @Query("SELECT m FROM MercadoMunicipal m WHERE " +
           "(LOWER(m.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(m.direccion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "AND (:estado IS NULL OR m.estado = :estado) " +
           "AND (:oficinaId IS NULL OR (m.oficinaAdministradora IS NOT NULL AND m.oficinaAdministradora.id = :oficinaId)) " +
           "AND (:distrito IS NULL OR LOWER(m.distrito) = :distrito) " +
           "AND (:fechaDesde IS NULL OR m.fechaRegistro >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR m.fechaRegistro <= :fechaHasta)")
    Page<MercadoMunicipal> filtrarMercadosConTermino(
            @Param("termino") String termino,
            @Param("estado") EstadoMercado estado,
            @Param("oficinaId") Long oficinaId,
            @Param("distrito") String distrito,
            @Param("fechaDesde") java.time.LocalDate fechaDesde,
            @Param("fechaHasta") java.time.LocalDate fechaHasta,
            Pageable pageable
    );

    long countByEstado(EstadoMercado estado);

    long countByOficinaAdministradoraId(Long oficinaId);
}
