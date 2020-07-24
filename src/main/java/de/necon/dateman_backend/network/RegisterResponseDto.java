package de.necon.dateman_backend.network;

public class RegisterResponseDto {
    public final String email;
    public final String username;

    public RegisterResponseDto(String email, String username) {
        this.email = email;
        this.username = username;
    }

    @Override
    public String toString() {
        return "{" + "email: " + email + ", username: " + username + "}";
    }
}
