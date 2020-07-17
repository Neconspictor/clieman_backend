package de.necon.dateman_backend.dto;

/**
 * Data Transfer object for login
 */
public class LoginDto {

    private String password;

    private String principal;

    public LoginDto() {

    }

    public LoginDto(String password, String principal) {
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
