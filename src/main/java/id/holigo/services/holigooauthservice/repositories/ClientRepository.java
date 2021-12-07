package id.holigo.services.holigooauthservice.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigooauthservice.domain.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByIdAndSecret(UUID id, String secret);

    Optional<Client> findByName(String name);
}
