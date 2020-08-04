package de.necon.clieman_backend.network;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Data Transfer object for login
 */
public class LoginDto {

    @NotNull
    @NotBlank
    private String password;

    @NotNull
    @NotBlank
    private String principal;

    public LoginDto() {

    }

    public LoginDto(String principal, String password) {
        this.password = password;
        this.principal = principal;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Principal is either the username or the email address
     */
    public String getPrincipal() {
        return principal;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
