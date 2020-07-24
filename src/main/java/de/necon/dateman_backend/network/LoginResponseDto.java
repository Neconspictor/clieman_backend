package de.necon.dateman_backend.network;

public class LoginResponseDto {
    public final String email;
    public final String username;


    public LoginResponseDto(String email, String username) {
        this.email = email;
        this.username = username;
    }
}
