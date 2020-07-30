package de.necon.dateman_backend.network;

import javax.validation.constraints.NotBlank;

public class TokenDto {

    @NotBlank
    private String token;

    public TokenDto() {

    }

    public TokenDto(String token) {
        this.token = token;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
