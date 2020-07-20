package de.necon.dateman_backend.service;

import de.necon.dateman_backend.unit.VerificationToken;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendVerificationMessage(VerificationToken token);
}