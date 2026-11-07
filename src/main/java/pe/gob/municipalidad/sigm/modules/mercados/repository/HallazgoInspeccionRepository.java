package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HallazgoInspeccionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.GravedadHallazgo;

import java.util.List;
import java.util.UUID;

@Repository
public interface HallazgoInspeccionRepository extends JpaRepository<HallazgoInspeccionMercado, UUID> {

    List<HallazgoInspeccionMercado> findByInspeccionIdOrderByFechaRegistroAsc(UUID inspeccionId);

    long countByEstado(String estado);

    long countByGravedadTecnica(GravedadHallazgo gravedad);

    @Query("SELECT h FROM HallazgoInspeccionMercado h WHERE " +
           "(:inspeccionId IS NULL OR h.inspeccion.id = :inspeccionId) AND " +
           "(:estado IS NULL OR h.estado = :estado) AND " +
           "(:gravedad IS NULL OR h.gravedadTecnica = :gravedad)")
    Page<HallazgoInspeccionMercado> buscarPaginado(
            @Param("inspeccionId") UUID inspeccionId,
            @Param("estado") String estado,
            @Param("gravedad") GravedadHallazgo gravedad,
            Pageable pageable);
}
