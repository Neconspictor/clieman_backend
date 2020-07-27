package de.necon.dateman_backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.Valid;
import java.io.Serializable;
import java.util.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL) //This ensures that only non null fields get serialized
public class Event implements Serializable  {

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
    private Set<Client> clients = new HashSet<>();

    @Basic
    private String color;

    @Basic
    private String details;
    @Basic
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
    private Date start;

    private static final long serialVersionUID = 0L;

    public Event() {
    }

    public Event(@JsonProperty("id") ID id, @JsonProperty("clients") Set<Client> clients) {
        // ensure that id is not null
        this.id = id;
        this.clients = clients;
        if (this.id == null) this.id = new ID();
        if (this.clients == null) this.clients = new HashSet<>();
    }

    public Event(String details,
                 Date start,
                 Date end,
                 Set<Client> clients,
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

    public Set<Client> getClients() {
        return clients;
    }

    public void setClients(Set<Client> clients) {
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

    public void setEnd(Date start) {
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
}