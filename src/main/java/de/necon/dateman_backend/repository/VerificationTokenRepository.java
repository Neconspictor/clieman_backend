package de.necon.dateman_backend.repository;

import de.necon.dateman_backend.unit.User;
import de.necon.dateman_backend.unit.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);
}