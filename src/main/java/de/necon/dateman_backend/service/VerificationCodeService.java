package de.necon.dateman_backend.service;

public interface VerificationCodeService {

    /**
     * Generates a new verification code.
     * @return A verification code.
     */
    String generateVerificationCode();
}
