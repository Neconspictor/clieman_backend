package de.necon.dateman_backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static de.necon.dateman_backend.config.ServiceErrorMessages.NO_USER;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL) //This ensures that only non null fields get serialized
public class Client implements Serializable  {

    @Basic
    private String address;

    @Basic
    private Date birthday;

    @Basic
    private String email;

    @Basic
    private String forename;

    @EmbeddedId
    private ID id;

    @Basic
    private String name;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    private static final long serialVersionUID = 1L;

    public Client() {
        this.id = new ID();
    }

    public Client(String address, Date birthday, String email, String forename, String id, String name, Sex sex, User user) {
        this.address = address;
        this.birthday = birthday;
        this.email = email;
        this.forename = forename;
        this.name = name;
        this.sex = sex;
        this.id = new ID();
        this.id.id = id;
        this.id.user = user;
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
    public String getId() {
        return id.getId();
    }

    public void setId(String id) {
        this.id.setId(id);
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
        return id.getUser();
    }

    public void setUser(User user) {
        this.id.setUser(user);
    }

    @Override
    public String toString() {

        String birthdayStr = null;

        if (birthday != null)  {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            birthdayStr = formatter.format(birthday);
        }

        String user = null;

        if (this.id.getUser() != null) user = this.id.getUser().getEmail();


        return "Client{" +
                "address='" + address + '\'' +
                ", birthday=" + birthdayStr +
                ", email='" + email + '\'' +
                ", forename='" + forename + '\'' +
                ", id=" + id.getId() +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", user=" + user +
                '}';
    }

    @Embeddable
    public static class ID implements Serializable {
        private String id;

        @NotNull(message=NO_USER)
        @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
        @JoinColumn(nullable = false, name = "user_id_embedded")
        private User user;


        public ID() {

        }

        private static final long serialVersionUID = 2L;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return "ID{" +
                    "id='" + id + '\'' +
                    ", User=" + user.getId() +
                    '}';
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}