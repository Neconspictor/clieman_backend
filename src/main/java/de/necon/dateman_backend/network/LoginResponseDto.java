package de.necon.dateman_backend.network;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
