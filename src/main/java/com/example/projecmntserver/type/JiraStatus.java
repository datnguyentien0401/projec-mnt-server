package com.example.projecmntserver.type;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JiraStatus {
    //OPEN GROUP
    OPEN("open"),
    REOPEN("reopen"),
    LATER("later"),
    ASSIGNED("assigned"),
    ADD_INFO_REQUIRED("additional information required"),
    POSTPONED("postponed"),

    //IN PROGRESS GROUP
    SW_ASSIGNED("sw assigned"),
    MAKE_SOLUTION("make solution"),
    REVIEW_SOLUTION("review solution"),
    IMPLEMENT("implement"),
    REVIEW_CODE("review code"),
    IN_PROGRESS("in progress"),
    REPLY("reply"),
    OPENED("opened"),

    //RESOLVED GROUP
    CHECKIN("checkin"),
    PREFIXED("prefixed"),
    KILLED("killed"),
    WONT_FIX("won't fix"),
    Fixed("fixed"),
    DONE("done"),
    CLOSED("closed");
    private final String value;

    public static final List<String> OPEN_STATUS_LIST = List.of(ADD_INFO_REQUIRED.getValue(), OPEN.getValue(),
                                                                ASSIGNED.getValue(), POSTPONED.getValue(),
                                                                REOPEN.getValue(), LATER.getValue());

    public static final List<String> IN_PROGRESS_STATUS_LIST = List.of(SW_ASSIGNED.getValue(),
                                                                       IMPLEMENT.getValue(),
                                                                       MAKE_SOLUTION.getValue(),
                                                                       REVIEW_SOLUTION.getValue(),
                                                                       REVIEW_CODE.getValue(),
                                                                       IN_PROGRESS.getValue(),
                                                                       OPENED.getValue(), REPLY.getValue());

    public static final List<String> DONE_STATUS_LIST = List.of(Fixed.getValue(), DONE.getValue(),
                                                                CLOSED.getValue(), KILLED.getValue(),
                                                                CHECKIN.getValue(), PREFIXED.getValue(),
                                                                WONT_FIX.getValue());

}
