package com.example.projecmntserver.dto.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class WorkLogDto {

  private int maxResults;
  private int total;
  private List<Log> worklogs;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @AllArgsConstructor
  @NoArgsConstructor
  public static final class Log {

    private int id;
    private Author author;
    private Author updateAuthor;
    private Date created;
    private Date updated;
    private Date started;
    private Integer timeSpentSeconds;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Author {

      private String accountId;
    }
  }
}
