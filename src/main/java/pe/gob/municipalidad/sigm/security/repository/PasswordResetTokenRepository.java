package pe.gob.municipalidad.sigm.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.security.model.PasswordResetToken;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT p FROM PasswordResetToken p JOIN FETCH p.usuario u JOIN FETCH u.persona per WHERE p.tokenHash = :tokenHash")
    Optional<PasswordResetToken> findByTokenHashWithUsuario(@Param("tokenHash") String tokenHash);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true, p.usedAt = :now WHERE p.usuario.id = :usuarioId AND p.used = false")
    int invalidatePendingTokensByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("now") OffsetDateTime now);
}
