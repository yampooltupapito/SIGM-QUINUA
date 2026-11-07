package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MedidaCorrectivaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoMedidaCorrectiva;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedidaCorrectivaMercadoRepository extends JpaRepository<MedidaCorrectivaMercado, UUID> {

    List<MedidaCorrectivaMercado> findByHallazgoId(UUID hallazgoId);

    List<MedidaCorrectivaMercado> findByEstado(EstadoMedidaCorrectiva estado);

    long countByEstado(EstadoMedidaCorrectiva estado);

    @Query("SELECT COUNT(m) FROM MedidaCorrectivaMercado m WHERE m.estado IN ('PENDIENTE', 'EN_PROCESO') AND m.fechaLimite < :hoy")
    long countVencidas(@Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(m) FROM MedidaCorrectivaMercado m WHERE m.estado IN ('PENDIENTE', 'EN_PROCESO') AND m.fechaLimite BETWEEN :hoy AND :proximosDias")
    long countPorVencer(@Param("hoy") LocalDate hoy, @Param("proximosDias") LocalDate proximosDias);

    @Query("SELECT m FROM MedidaCorrectivaMercado m WHERE " +
           "(:estado IS NULL OR m.estado = :estado) AND " +
           "(:mercadoId IS NULL OR m.hallazgo.inspeccion.mercado.id = :mercadoId) AND " +
           "(:comercianteId IS NULL OR m.comercianteResponsable.id = :comercianteId OR m.hallazgo.inspeccion.comerciante.id = :comercianteId) AND " +
           "(:soloVencidas = false OR (m.estado IN ('PENDIENTE', 'EN_PROCESO') AND m.fechaLimite < :hoy))")
    Page<MedidaCorrectivaMercado> buscarSeguimientoPaginado(
            @Param("estado") EstadoMedidaCorrectiva estado,
            @Param("mercadoId") UUID mercadoId,
            @Param("comercianteId") UUID comercianteId,
            @Param("soloVencidas") boolean soloVencidas,
            @Param("hoy") LocalDate hoy,
            Pageable pageable);
}
