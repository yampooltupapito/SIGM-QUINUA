package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ProgramaFraccionamientoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoProgramaFraccionamiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramaFraccionamientoTributarioRepository
        extends JpaRepository<ProgramaFraccionamientoTributario, UUID>, JpaSpecificationExecutor<ProgramaFraccionamientoTributario> {

    Optional<ProgramaFraccionamientoTributario> findByCodigo(String codigo);

    List<ProgramaFraccionamientoTributario> findByEstado(EstadoProgramaFraccionamiento estado);

    @Query("SELECT p FROM ProgramaFraccionamientoTributario p " +
           "WHERE p.estado = 'ACTIVO' " +
           "AND p.fechaInicioVigencia <= :fecha " +
           "AND (p.fechaFinVigencia IS NULL OR p.fechaFinVigencia >= :fecha) " +
           "ORDER BY p.codigo ASC")
    List<ProgramaFraccionamientoTributario> findVigentesParaFecha(@Param("fecha") LocalDate fecha);
}
