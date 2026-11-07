package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.MetaRecaudacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoMetaRecaudacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetaRecaudacionTributariaRepository extends JpaRepository<MetaRecaudacionTributaria, UUID> {

    List<MetaRecaudacionTributaria> findByEjercicioAndEstado(Integer ejercicio, EstadoMetaRecaudacion estado);

    Optional<MetaRecaudacionTributaria> findByEjercicioAndPeriodoAndConceptoIdAndEstado(
            Integer ejercicio, Integer periodo, UUID conceptoId, EstadoMetaRecaudacion estado);

    Optional<MetaRecaudacionTributaria> findFirstByEjercicioAndPeriodoAndConceptoIsNullAndEstado(
            Integer ejercicio, Integer periodo, EstadoMetaRecaudacion estado);

    Page<MetaRecaudacionTributaria> findByEjercicio(Integer ejercicio, Pageable pageable);

    @Query("SELECT m FROM MetaRecaudacionTributaria m WHERE (:ejercicio IS NULL OR m.ejercicio = :ejercicio) " +
           "AND (:estado IS NULL OR m.estado = :estado) ORDER BY m.ejercicio DESC, m.periodo ASC")
    Page<MetaRecaudacionTributaria> buscarMetas(
            @Param("ejercicio") Integer ejercicio,
            @Param("estado") EstadoMetaRecaudacion estado,
            Pageable pageable);
}

