package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.CuentaCorrienteTributaria;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CuentaCorrienteTributariaRepository extends JpaRepository<CuentaCorrienteTributaria, UUID>, JpaSpecificationExecutor<CuentaCorrienteTributaria> {

    @Query(value = "SELECT nextval('seq_tributaria_cuenta_corriente')", nativeQuery = true)
    Long obtenerSiguienteValorSecuencia();

    Optional<CuentaCorrienteTributaria> findByContribuyenteId(UUID contribuyenteId);

    Optional<CuentaCorrienteTributaria> findByCodigoCuenta(String codigoCuenta);

    boolean existsByContribuyenteId(UUID contribuyenteId);

    @Query("SELECT COALESCE(SUM(c.saldoActual), 0) FROM CuentaCorrienteTributaria c WHERE c.estado = 'ACTIVA'")
    BigDecimal sumarSaldoTotalActivo();

    @Query("SELECT COUNT(c) FROM CuentaCorrienteTributaria c WHERE c.saldoActual > 0")
    long countCuentasConDeuda();
}
