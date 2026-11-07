package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ParametroInteresTributario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametroInteresTributarioRepository extends JpaRepository<ParametroInteresTributario, UUID> {

    Optional<ParametroInteresTributario> findByCodigo(String codigo);

    List<ParametroInteresTributario> findByEstadoTrueOrderByFechaInicioVigenciaDesc();

    @Query("""
        SELECT p FROM ParametroInteresTributario p
        WHERE p.estado = true
          AND p.fechaInicioVigencia <= :fecha
          AND (p.fechaFinVigencia IS NULL OR p.fechaFinVigencia >= :fecha)
          AND (p.concepto IS NULL OR p.concepto.id = :conceptoId)
        ORDER BY CASE WHEN p.concepto IS NOT NULL THEN 0 ELSE 1 END ASC, p.fechaInicioVigencia DESC
    """)
    List<ParametroInteresTributario> findVigenteParaFechaYConcepto(
            @Param("fecha") LocalDate fecha,
            @Param("conceptoId") UUID conceptoId);
}
