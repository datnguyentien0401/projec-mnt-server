package com.example.projecmntserver.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusCategory {
    NEW("new"),
    INDETERMINATE("indeterminate"),
    DONE("done");

    private final String value;

    public boolean isNew() {
        return this == NEW;
    }

    public boolean isIndeterminate() {
        return this == INDETERMINATE;
    }

    @JsonCreator
    public StatusCategory fromValue(String value) {
        return Enum.valueOf(StatusCategory.class, value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
