package de.necon.clieman_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.Serializable;

import static de.necon.clieman_backend.config.ServiceErrorMessages.INVALID_ID;

@Embeddable
public class ID implements Serializable {
    @NotBlank(message= INVALID_ID)
    @Column(name="ID_EMBEDDED")
    private String id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "USER_ID_EMBEDDED")
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

        Long userID = user != null ? user.getId()  : null;

        return "ID{" +
                "id='" + id + '\'' +
                ", User=" + userID +
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
