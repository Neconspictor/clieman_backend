package de.necon.clieman_backend.repository;

import de.necon.clieman_backend.model.User;
import de.necon.clieman_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Searches a verification token by its textual representation.
     * @param token The token to search.
     * @return The found token.
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Searches a verification token by its linked user.
     * @param user The user linked with the desired verification token.
     * @return The found token.
     */
    Optional<VerificationToken> findByUser(User user);
}