package de.necon.clieman_backend.network;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmailDto {

    @NotNull
    @NotBlank
    private String email;

    public EmailDto() {

    }

    public EmailDto(String email) {
        this.email = email;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
