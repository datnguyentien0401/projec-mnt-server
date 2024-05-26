package com.example.projecmntserver.type;

import java.util.List;

public enum JiraIssueType {
    INITIATIVE,
    EPIC,
    STORY,
    ;

    public static final List<String> IGNORE_SEARCH_ISSUE_TYPE = List.of(INITIATIVE.name(), EPIC.name(), STORY.name());
}
