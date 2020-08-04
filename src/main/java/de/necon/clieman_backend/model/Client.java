package de.necon.clieman_backend.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    @Valid
    @EmbeddedId
    @JsonSerialize(using = ID.IDSerializer.class)
    @JsonDeserialize(using = ID.IDDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private ID id = new ID();

    @Basic
    private String name;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Basic
    private String mobile;

    @Basic
    private String title;

    private static final long serialVersionUID = 1L;

    public Client() {

    }

    /**
     * Constructs a new Client with the specified id.
     * Note: This constructor is needed so that jackson library will create a not-null ID field.
     * @param id
     */
    public Client(@JsonProperty("id") ID id) {
        // ensure that id is not null
        if (id != null)
            this.id = id;
    }

    public Client(String address,
                  Date birthday,
                  String email,
                  String forename,
                  String id,
                  String mobile,
                  String name,
                  Sex sex,
                  String title,
                  User user) {
        this.address = address;
        this.birthday = birthday;
        this.email = email;
        this.forename = forename;
        this.id = new ID(id, user);
        this.mobile = mobile;
        this.name = name;
        this.sex = sex;
        this.title = title;
    }

    public Client copyShallow() {
        Date cBirthday = birthday != null ? new Date(birthday.getTime()) : null;
        String idStr = id != null ? id.getId() : null;
        User cUser = id != null ? id.getUser() : null;
        return new Client(address, cBirthday, email, forename, idStr, mobile, name, sex, title, cUser);
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
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
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


    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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


    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public User getUser() {
        return id.getUser();
    }

    @JsonIgnore
    public void setUser(User user) {
        id.setUser(user);
    }

    @Override
    public String toString() {

        String birthdayStr = null;

        if (birthday != null)  {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            birthdayStr = formatter.format(birthday);
        }

        var idString = id != null ? id.getId() : null;
        var userString = id != null && id.getUser() != null ? this.id.getUser().getEmail() : null;

        return "Client{" +
                "address='" + address + '\'' +
                ", birthday=" + birthdayStr +
                ", email='" + email + '\'' +
                ", forename='" + forename + '\'' +
                ", id=" + idString +
                ", mobile='" + mobile + '\'' +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", title='" + title + '\'' +
                ", user=" + userString +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return new EqualsBuilder()
                .append(address, client.address)
                .append(birthday, client.birthday)
                .append(email, client.email)
                .append(forename, client.forename)
                .append(id, client.id)
                .append(name, client.name)
                .append(sex, client.sex)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(address)
                .append(birthday)
                .append(email)
                .append(forename)
                .append(id)
                .append(name)
                .append(sex)
                .toHashCode();
    }
}