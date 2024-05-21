package com.example.projecmntserver.type;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JiraStatus {
    RESOLVED("Resolved"),
    DONE("Done"),
    CLOSED("Closed");
    private final String value;

    public static final List<String> DONE_STATUS_LIST = List.of(RESOLVED.name(), DONE.name(), CLOSED.name());

}
