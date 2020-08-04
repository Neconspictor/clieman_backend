package de.necon.clieman_backend.events;

import org.springframework.context.ApplicationEvent;

public class DtoEvent extends ApplicationEvent {

    private Object dto;

    public DtoEvent(Object dto) {
        super(dto);
        this.dto = dto;
    }

    public Object getDto() {
        return dto;
    }

    public void setDto(Object dto) {
        this.dto = dto;
    }
}
