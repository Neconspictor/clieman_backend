package de.necon.dateman_backend.network;

import javax.validation.constraints.NotBlank;

public class PasswordDto {

    @NotBlank
    private String password;


    public PasswordDto() {
    }

    public PasswordDto(String password) {
        this.password = password;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}