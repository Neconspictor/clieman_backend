package de.necon.dateman_backend.network;

import javax.validation.constraints.NotBlank;

public class UserDto {

    @NotBlank
    public String email;

    public String username;

    public UserDto() {

    }

    public UserDto(String email, String username) {
        this.email = email;
        this.username = username;
    }

    @Override
    public String toString() {
        return "{" + "email: " + email + ", username: " + username + "}";
    }
}
