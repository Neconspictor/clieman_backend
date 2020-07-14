package de.necon.dateman_backend.model;

import de.necon.dateman_backend.config.RepositoryConfig;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Holds information about users registered at Dateman.
 */
@Entity
@Table( name = "tb_user", // NOTE: We have to use a different name since User is a reserved word in SQL
        uniqueConstraints={@UniqueConstraint(columnNames={"username"})})
public class User {

    /**
     *  Primary source for authentication and used to send emails to the user (e.g. during registration process).
     */
    @Id
    @NotNull
    @Email(regexp = ".+@.+\\..+", message = "Email for user must be valid.")
    @Column(nullable = false)
    @Size(max = RepositoryConfig.MAX_STRING_SIZE)
    private String email;

    /**
     * Id of the user. Is used for retrieving user information and data (clients, events, etc.).
     */
    /*@GeneratedValue(generator = "sequence-generator")
    @GenericGenerator(
            name = "sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "seq_tb_user"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    private long id;*/

    /**
     * Password for authentication. Should be result of a cryptographic hash function.
     */
    @NotNull
    @NotEmpty
    @Column(nullable = false)
    @Size(max = RepositoryConfig.MAX_STRING_SIZE)
    private String password;

    /**
     * Name of the user. Is optional and can be used as a replace of email when user authenticates.
     */
    @Size(max = RepositoryConfig.MAX_STRING_SIZE)
    @Column(name="username")
    private String username;

    public User() {

    }

    /**
     * Creates a new User object.
     * @param email The email of the user. Has to be a valid email and mustn't be null.
     * @param password The password for authentication. Mustn't be null.
     * @param username The user's nick name. Can be null.
     */
    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
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

    public void setEmail(String email) {
        this.email = email;
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

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ email: ");
        builder.append(email);
        builder.append(", password: ");
        builder.append(password);
        builder.append(" }");

        return builder.toString();
    }
}