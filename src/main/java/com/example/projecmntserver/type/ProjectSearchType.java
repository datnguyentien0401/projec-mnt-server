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

}
