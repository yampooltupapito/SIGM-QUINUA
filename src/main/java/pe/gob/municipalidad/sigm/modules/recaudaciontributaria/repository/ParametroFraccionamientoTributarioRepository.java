package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroFraccionamientoTributario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametroFraccionamientoTributarioRepository
        extends JpaRepository<ParametroFraccionamientoTributario, UUID> {

    List<ParametroFraccionamientoTributario> findByProgramaId(UUID programaId);

    @Query("SELECT p FROM ParametroFraccionamientoTributario p " +
           "WHERE p.programa.id = :programaId " +
           "AND p.codigo = :codigo " +
           "AND p.estado = true " +
           "AND p.fechaInicioVigencia <= :fecha " +
           "AND (p.fechaFinVigencia IS NULL OR p.fechaFinVigencia >= :fecha) " +
           "ORDER BY p.fechaInicioVigencia DESC")
    List<ParametroFraccionamientoTributario> findVigente(
            @Param("programaId") UUID programaId,
            @Param("codigo") String codigo,
            @Param("fecha") LocalDate fecha
    );

    default Optional<ParametroFraccionamientoTributario> findPrimerVigente(UUID programaId, String codigo, LocalDate fecha) {
        List<ParametroFraccionamientoTributario> list = findVigente(programaId, codigo, fecha);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
