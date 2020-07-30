package de.necon.dateman_backend.network;

import javax.validation.constraints.NotBlank;

public class UserDto implements Dto {

    @NotBlank
    public final String email;

    public final String username;

    public UserDto(String email, String username) {
        this.email = email;
        this.username = username;
    }

    @Override
    public String toString() {
        return "{" + "email: " + email + ", username: " + username + "}";
    }
}
