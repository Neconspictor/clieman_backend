package de.necon.dateman_backend.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static de.necon.dateman_backend.config.ServiceErrorMessages.CLIENT_INVLAID_ID;
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

    @Valid
    @EmbeddedId
    @JsonSerialize(using = IDSerializer.class)
    @JsonDeserialize(using = IDDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private ID id;

    @Basic
    private String name;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    private static final long serialVersionUID = 1L;

    public Client() {
        this.id = new ID();
    }

    public Client(String address,
                  Date birthday,
                  String email,
                  String forename,
                  String id,
                  String name,
                  Sex sex,
                  User user) {
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

    public Client(
            @JsonProperty("address") String address,
            @JsonProperty("birthday") Date birthday,
            @JsonProperty("email") String email,
            @JsonProperty("forename") String forename,
            @JsonProperty("id") ID id,
            @JsonProperty("name") String name,
            @JsonProperty("sex") Sex sex)
    {
        this.address = address;
        this.birthday = birthday;
        this.email = email;
        this.forename = forename;
        this.id = id;
        this.name = name;
        this.sex = sex;

        // ensure that id is not null
        if (this.id == null)
            this.id = new ID();
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

    /**
     * @return  The family name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", user=" + userString +
                '}';
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
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

    @Embeddable
    public static class ID implements Serializable {
        @NotBlank(message=CLIENT_INVLAID_ID)
        private String id;

        @NotNull(message=NO_USER)
        @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
        @JoinColumn(nullable = false, name = "user_id_embedded")
        @JsonIgnore
        private User user;


        public ID() {

        }

        public ID(String id, User user) {
            this.id = id;
            this.user = user;
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

            var userString = user != null ? user.getId()  : "null";

            return "ID{" +
                    "id='" + id + '\'' +
                    ", User=" + userString +
                    '}';
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            ID id1 = (ID) o;

            return new EqualsBuilder()
                    .append(id, id1.id)
                    .append(user, id1.user)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(id)
                    .append(user)
                    .toHashCode();
        }
    }


    public static class IDSerializer extends JsonSerializer<ID> {

        @Override
        public void serialize(ID id,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {

            jsonGenerator.writeString(id.getId());
        }
    }

    public static class IDDeserializer extends JsonDeserializer<ID> {

        @Override
        public ID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return new ID(p.getValueAsString(), null);
        }
    }
}