package de.necon.dateman_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.TimeZone;

import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;

@Entity
public class Client implements Serializable  {

    @Basic
    private String address;

    @Basic
    private Date birthday;

    @Basic
    private String email;

    @Basic
    private String forename;

    @Id
    @GeneratedValue
    private Long id;

    @Basic
    private String name;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @NotNull(message=NO_USER)
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private static final long serialVersionUID = 0L;

    public Client() {

    }

    public Client(String address, Date birthday, String email, String forename, String name, Sex sex, User user) {
        this.address = address;
        this.birthday = birthday;
        this.email = email;
        this.forename = forename;
        this.name = name;
        this.sex = sex;
        this.user = user;
    }

    /**
     *
     * @return The contact address of the client.
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *
     * @return The birthday of the client.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     *
     * @return contact email address of the client.
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return the forename.
     */
    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    /**
     *  @return The id of the client.
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return  The family name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Provides the user who has created this client.
     * @return The user the client belongs to.
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {

        String birthdayStr = null;

        if (birthday != null)  {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            birthdayStr = formatter.format(birthday);
        }


        return "Client{" +
                "address='" + address + '\'' +
                ", birthday=" + birthdayStr +
                ", email='" + email + '\'' +
                ", forename='" + forename + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                //NOTE: user mustn't be null, so it is safe to access the email property!
                ", user=" + user.getEmail() +
                '}';
    }
}