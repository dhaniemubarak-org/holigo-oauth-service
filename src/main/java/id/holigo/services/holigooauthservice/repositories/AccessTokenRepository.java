package id.holigo.services.holigooauthservice.repositories;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigooauthservice.domain.AccessToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {

    @Modifying
    @Query(value = "UPDATE AccessToken t SET t.revoked = :revoked , t.updated_at = :updatedAt WHERE t.user_id = :userId AND id <> :id", nativeQuery = true)
    void revokeAccessToken(@Param("revoked") int revoked, @Param("updatedAt") Timestamp updatedAt, @Param("userId") Long userId, @Param("id") UUID id);
}
