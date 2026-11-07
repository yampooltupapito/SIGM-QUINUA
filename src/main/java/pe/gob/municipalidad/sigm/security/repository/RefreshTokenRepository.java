package pe.gob.municipalidad.sigm.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.security.model.RefreshToken;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenId(String tokenId);

    List<RefreshToken> findByFamilyId(String familyId);

    List<RefreshToken> findByUsuarioId(Long usuarioId);

    @Query("SELECT r FROM RefreshToken r WHERE r.usuario.id = :usuarioId AND r.revoked = false AND r.expiresAt > :now ORDER BY r.createdAt DESC")
    List<RefreshToken> findActiveByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.familyId = :familyId")
    void revokeFamily(@Param("familyId") String familyId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.usuario.id = :usuarioId AND r.revoked = false")
    void revokeAllByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.usuario.id = :usuarioId AND r.id <> :excludeId AND r.revoked = false")
    void revokeAllByUsuarioIdExcept(@Param("usuarioId") Long usuarioId, @Param("excludeId") Long excludeId, @Param("now") OffsetDateTime now);
}
