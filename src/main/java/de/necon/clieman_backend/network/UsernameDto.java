package de.necon.clieman_backend.network;

public class UsernameDto {

    private String username;

    public UsernameDto() {

    }

    public UsernameDto(String username) {
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
