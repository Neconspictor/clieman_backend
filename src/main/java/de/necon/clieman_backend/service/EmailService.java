package de.necon.clieman_backend.service;

import de.necon.clieman_backend.model.VerificationToken;

/**
 * A service for sending emails.
 */
public interface EmailService {

    /**
     * Sends a simple email.
     * @param to The addressee.
     * @param subject The subject the email is about.
     * @param text The content of the email.
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Sends an email to a user's email containing the verification token.
     * @param token The token which is used to get the user's email address and the token.
     */
    void sendVerificationMessage(VerificationToken token);
}