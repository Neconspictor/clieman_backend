package de.necon.dateman_backend.unit.network;

import de.necon.dateman_backend.network.EmailDto;

public class TestDtoSubclass extends EmailDto {
    private String testField;

    TestDtoSubclass(String email, String testField) {
        super(email);
        this.testField = testField;
    }

    public String getTestField() {
        return testField;
    }

    public void setTestField(String testField) {
        this.testField = testField;
    }
}
