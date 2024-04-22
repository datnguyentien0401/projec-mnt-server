package com.example.projecmntserver.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamResponse extends BaseResponse {
    private String name;
    private Long numberOfMembers;

    public TeamResponse(Long id, String name, Long numberOfMembers, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.numberOfMembers = numberOfMembers;
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }
}
