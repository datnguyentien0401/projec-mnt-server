package com.example.projecmntserver.util;

import com.example.projecmntserver.constant.Constant;
import com.example.projecmntserver.dto.jira.IssueDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Helper {

    public static List<IssueDto> getResolvedIssuesInRange(List<IssueDto> issues, LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(issues);
        return issues.stream().filter(
            issue -> {
                String resolvedAt = issue.getFields().getResolvedAt();
                if (resolvedAt == null) {
                    return false;
                }
                LocalDate resolvedDate = DatetimeUtils.parse(resolvedAt, Constant.DATE_TIME_PATTERN);
                return resolvedDate.isAfter(fromDate) && resolvedDate.isBefore(toDate);
            }
        ).toList();
    }
}
