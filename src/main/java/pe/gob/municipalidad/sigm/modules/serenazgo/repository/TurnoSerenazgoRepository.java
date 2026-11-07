package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.TurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TurnoSerenazgoRepository extends JpaRepository<TurnoSerenazgo, UUID> {

    Optional<TurnoSerenazgo> findByCodigo(String codigo);

    List<TurnoSerenazgo> findByEstadoOrderByHoraInicioAsc(EstadoTurnoSerenazgo estado);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);

    @Query("SELECT t FROM TurnoSerenazgo t WHERE " +
            "(:estado IS NULL OR t.estado = :estado) AND " +
            "(LOWER(t.codigo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
            "OR LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
            "ORDER BY t.horaInicio ASC")
    Page<TurnoSerenazgo> buscarConFiltrosConTermino(@Param("termino") String termino,
                                                   @Param("estado") EstadoTurnoSerenazgo estado,
                                                   Pageable pageable);

    @Query("SELECT t FROM TurnoSerenazgo t WHERE " +
            "(:estado IS NULL OR t.estado = :estado) " +
            "ORDER BY t.horaInicio ASC")
    Page<TurnoSerenazgo> buscarConFiltrosSinTermino(@Param("estado") EstadoTurnoSerenazgo estado,
                                                   Pageable pageable);

    long countByEstado(EstadoTurnoSerenazgo estado);
}
