package com.example.projecmntserver.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectSearchType {
    TIME_SPENT_MD,
    TIME_SPENT_MM,
    RESOLVED_ISSUE,
    STORY_POINT
    ;

    public boolean isTimeSpent() {
        return this == TIME_SPENT_MD || this == TIME_SPENT_MM;
    }

    public boolean isStoryPoint() {
        return this == STORY_POINT;
    }

}
