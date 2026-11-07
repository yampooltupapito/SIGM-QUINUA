package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoComercial;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PuestoComercialRepository extends JpaRepository<PuestoComercial, UUID>, JpaSpecificationExecutor<PuestoComercial> {

    Optional<PuestoComercial> findByIdAndMercadoId(UUID id, UUID mercadoId);

    boolean existsByIdAndMercadoId(UUID id, UUID mercadoId);

    Optional<PuestoComercial> findByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    boolean existsByMercadoIdAndCodigo(UUID mercadoId, String codigo);

    boolean existsByMercadoIdAndCodigoIgnoreCase(UUID mercadoId, String codigo);

    boolean existsByMercadoIdAndCodigoIgnoreCaseAndIdNot(UUID mercadoId, String codigo, UUID id);

    Page<PuestoComercial> findByMercadoId(UUID mercadoId, Pageable pageable);

    List<PuestoComercial> findByMercadoId(UUID mercadoId);

    Page<PuestoComercial> findByMercadoIdAndEstado(UUID mercadoId, EstadoPuesto estado, Pageable pageable);

    List<PuestoComercial> findByMercadoIdAndEstado(UUID mercadoId, EstadoPuesto estado);

    List<PuestoComercial> findByEstado(EstadoPuesto estado);

    long countByMercadoId(UUID mercadoId);

    long countByEstado(EstadoPuesto estado);

    long countByMercadoIdAndEstado(UUID mercadoId, EstadoPuesto estado);

    long countByMercadoIdAndEstadoFisico(UUID mercadoId, EstadoFisicoPuesto estadoFisico);

    long countByMercadoIdAndSectorIdAndEstado(UUID mercadoId, UUID sectorId, EstadoPuesto estado);

    long countByMercadoIdAndTipoPuestoId(UUID mercadoId, Long tipoPuestoId);

    long countBySectorId(UUID sectorId);

    long countByPabellonId(UUID pabellonId);

    long countBySectorIdAndEstado(UUID sectorId, EstadoPuesto estado);

    long countByPabellonIdAndEstado(UUID pabellonId, EstadoPuesto estado);

    boolean existsBySectorId(UUID sectorId);

    boolean existsByPabellonId(UUID pabellonId);

    boolean existsByZonaId(UUID zonaId);

    List<PuestoComercial> findBySectorIdOrderByCodigoAsc(UUID sectorId);

    List<PuestoComercial> findByPabellonIdOrderByCodigoAsc(UUID pabellonId);

    List<PuestoComercial> findBySectorIdAndPabellonIdIsNullOrderByCodigoAsc(UUID sectorId);

    List<PuestoComercial> findByMercadoIdAndSectorIdIsNullAndPabellonIdIsNullOrderByCodigoAsc(UUID mercadoId);

    @Query("SELECT p FROM PuestoComercial p WHERE p.mercado.id = :mercadoId " +
           "AND p.estado = pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoPuesto.DISPONIBLE " +
           "AND p.estadoFisico = pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoFisicoPuesto.OPERATIVO " +
           "ORDER BY p.codigo ASC")
    List<PuestoComercial> findDisponiblesByMercadoId(@Param("mercadoId") UUID mercadoId);
}
