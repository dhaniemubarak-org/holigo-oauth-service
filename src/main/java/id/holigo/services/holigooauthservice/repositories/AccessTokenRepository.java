package id.holigo.services.holigooauthservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigooauthservice.domain.AccessToken;

public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {

}
