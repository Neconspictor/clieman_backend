package de.necon.dateman_backend.network;

public class EmailDto {
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
