package de.necon.clieman_backend.service;

import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final int CODE_LENGTH  = 6;

    private RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', '9').build();

    @Override
    public String generateVerificationCode() {
        return generator.generate(CODE_LENGTH);
    }
}
