package de.necon.clieman_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.necon.clieman_backend.config.RepositoryConfig;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Objects;

import static de.necon.clieman_backend.config.ServiceErrorMessages.*;

/**
 * Holds information about users registered at Dateman.
 */
@Entity
@Table( name = "tb_user", // NOTE: We have to use a different name since User is a reserved word in SQL
        uniqueConstraints={@UniqueConstraint(columnNames={"email", "username"})})
public class User {

    public static final int MIN_PASSWORD_LENGTH = 8;


    /**
     *  Primary source for authentication and used to send emails to the user (e.g. during registration process).
     */
    @NotNull(message = NO_EMAIL)
    @Email(regexp = ".+@.+\\..+", message = EMAIL_NOT_VALID)
    @Column(nullable = false, unique=true)
    @Size(max = RepositoryConfig.MAX_STRING_SIZE)
    private String email;

    @Column(columnDefinition="BOOLEAN DEFAULT false")
    private boolean enabled = false;

    @Id
    @GeneratedValue(generator = "sequence-generator")
    @GenericGenerator(
            name = "sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "seq_tb_user"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    private Long id;

    /**
     * Password for authentication. Should be result of a cryptographic hash function.
     */
    @NotNull(message=NO_PASSWORD)
    @NotEmpty(message=PASSWORD_TOO_SHORT)
    @Column(nullable = false)
    @Size(min = MIN_PASSWORD_LENGTH, message=PASSWORD_TOO_SHORT)
    @Size(max = RepositoryConfig.MAX_STRING_SIZE, message=PASSWORD_TOO_LONG)
    private String password;

    /**
     * Name of the user. Is optional and can be used as a replace of email when user authenticates.
     */
    @Size(max = RepositoryConfig.MAX_STRING_SIZE)
    @Column(name="username", unique=true)
    private String username;

    public User() {

    }

    /**
     * Creates a new User object.
     * @param email The email of the user. Has to be a valid email and mustn't be null.
     * @param password The password for authentication. Mustn't be null.
     * @param username The user's nick name. Can be null.
     */
    public User(String email, String password, String username, Boolean enabled) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.enabled = enabled ? enabled : false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new EqualsBuilder()
                .append(enabled, user.enabled)
                .append(email, user.email)
                .append(id, user.id)
                .append(password, user.password)
                .append(username, user.username)
                .isEquals();
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, enabled, id, password, username);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    public boolean isDisabled() {
        return !enabled;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the {@link #password}.
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Id of the user. Is used for retrieving user information and data (clients, events, etc.).
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", enabled=" + enabled +
                ", id=" + id +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}