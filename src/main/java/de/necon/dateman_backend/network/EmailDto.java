package de.necon.dateman_backend.network;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
