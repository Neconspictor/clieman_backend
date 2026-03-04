package de.necon.clieman_backend.network;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginResponseDto {

    @NotNull
    @NotBlank
    public final String email;

    public final String username;


    public LoginResponseDto(String email, String username) {
        this.email = email;
        this.username = username;
    }
}
