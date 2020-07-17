package de.necon.dateman_backend.repository;

import de.necon.dateman_backend.model.User;
import de.necon.dateman_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);
}