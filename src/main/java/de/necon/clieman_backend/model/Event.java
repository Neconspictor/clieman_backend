package de.necon.clieman_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL) //This ensures that only non null fields get serialized
public class Event implements Serializable  {

    @JsonSerialize(contentUsing=ClientSerializer.class) //Note: We only want to serialize the client id string
    @JsonDeserialize(contentUsing = ClientDeserializer.class)

    @ManyToMany
    @JoinTable(
            name="EVENT_CLIENTS",
            joinColumns={
                    @JoinColumn(name="EVENT_ID", referencedColumnName="ID_EMBEDDED"),
                    @JoinColumn(name="EVENT_USER", referencedColumnName="USER_ID_EMBEDDED")
            },
            inverseJoinColumns={
                    @JoinColumn(name="CLIENT_ID", referencedColumnName="ID_EMBEDDED"),
                    @JoinColumn(name="CLIENT_USER", referencedColumnName="USER_ID_EMBEDDED")
            }
    )
    @NotNull
    private List<Client> clients = new ArrayList<>();

    @Basic
    private String color;

    @Basic
    private String details;

    @Basic
    @Column(name="end_date")
    private Date end;

    @Valid
    @EmbeddedId
    @JsonSerialize(using = ID.IDSerializer.class)
    @JsonDeserialize(using = ID.IDDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private ID id = new ID();

    @Basic
    private String name;

    @Basic
    @Column(name="start_date")
    private Date start;

    private static final long serialVersionUID = 0L;

    public Event() {
    }

    public Event(@JsonProperty("id") ID id, @JsonProperty("clients") List<Client> clients) {
        // ensure that id is not null
        this.id = id;
        this.clients = clients;
        if (this.id == null) this.id = new ID();
        if (this.clients == null) this.clients = new ArrayList<>();
    }

    public Event(String details,
                 Date start,
                 Date end,
                 List<Client> clients,
                 String color,
                 String id,
                 String name,
                 User user) {
        this.details = details;
        this.start = start;
        this.end = end;
        this.clients = clients;
        this.color = color;
        this.id = new ID(id, user);
        this.name = name;
    }

    /**
     * @return  a deep copy except the list entries of clients and the user.
     */
    public Event copyMiddle() {

        Date startDate = start != null ? new Date(start.getTime()) : null;
        Date endDate = end != null ? new Date(end.getTime()) : null;
        String idString = id != null ? id.getId() : null;
        User user = id != null ?  id.getUser() : null;

        return new Event(details,
                    startDate,
                    endDate,
                    new ArrayList<>(clients),
                    color,
                    idString,
                    name,
                    user
                );
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    /**
     *
     * @return the display color of the event.
     */
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     *
     * @return The contact address of the client.
     */
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    /**
     *
     * @return The end date of the event.
     */
    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }


    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    /**
     * @return  The name of the event.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return The start date of the event.
     */
    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    @JsonIgnore
    public User getUser() {
        return id.getUser();
    }

    @JsonIgnore
    public void setUser(User user) {
        id.setUser(user);
        propagateUser();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return new EqualsBuilder()
                .append(details, event.details)
                .append(start, event.start)
                .append(end, event.end)
                .append(clients, event.clients)
                .append(color, event.color)
                .append(id, event.id)
                .append(name, event.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(details)
                .append(start)
                .append(end)
                .append(clients)
                .append(color)
                .append(id)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {

        List<ID> clientsIds = new ArrayList<>();
        if (clients != null) {
            clients.forEach(c -> {
                clientsIds.add(c.getId());
            });
        }

        return "Event{" +
                "details='" + details + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", clients=" + clientsIds +
                ", color='" + color + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    private void propagateUser() {
        clients.forEach(c->{
            c.setUser(getUser());
        });
    }


    public static class ClientSerializer extends JsonSerializer<Client> {

        @Override
        public void serialize(Client value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getId().getId());
        }
    }

    public static class ClientDeserializer extends JsonDeserializer<Client> {

        @Override
        public Client deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            return new Client(null,
                    null,
                    null,
                    null,
                    p.readValueAs(String.class),
                    null,
                    null,
                    null,
                    null,
                    null);
        }
    }
}