package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CorrelativoExpediente;

import java.util.Optional;

@Repository
public interface CorrelativoExpedienteRepository extends JpaRepository<CorrelativoExpediente, Long> {

    Optional<CorrelativoExpediente> findByPrefijoAndAnio(String prefijo, Integer anio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CorrelativoExpediente c WHERE c.prefijo = :prefijo AND c.anio = :anio")
    Optional<CorrelativoExpediente> findForUpdate(@Param("prefijo") String prefijo, @Param("anio") Integer anio);

    @Modifying
    @Query(value = "INSERT INTO mpv_correlativos (prefijo, anio, ultimo_numero, updated_at) " +
                   "VALUES (:prefijo, :anio, 0, NOW()) " +
                   "ON CONFLICT (prefijo, anio) DO NOTHING", nativeQuery = true)
    int asegurarExistenciaCorrelativo(@Param("prefijo") String prefijo, @Param("anio") Integer anio);
}
