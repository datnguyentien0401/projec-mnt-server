package com.example.projecmntserver.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JiraPathConstant {
    public static final String SEARCH = "/rest/api/3/search";
    public static final String GET_ALL = "/rest/api/3/project";
    public static final String PROJECT_SEARCH = "/rest/api/3/project/search";
    public static final String USER_SEARCH = "/rest/api/3/user/search";

}
