package de.necon.dateman_backend.network;

public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;
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