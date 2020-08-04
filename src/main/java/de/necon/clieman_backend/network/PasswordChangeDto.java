package de.necon.clieman_backend.network;

import javax.validation.constraints.NotBlank;

public class PasswordChangeDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmationPassword;

    public PasswordChangeDto() {
    }

    public PasswordChangeDto(String oldPassword, String newPassword, String confirmationPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmationPassword = confirmationPassword;
    }


    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmationPassword() {
        return confirmationPassword;
    }

    public void setConfirmationPassword(String confirmationPassword) {
        this.confirmationPassword = confirmationPassword;
    }
}