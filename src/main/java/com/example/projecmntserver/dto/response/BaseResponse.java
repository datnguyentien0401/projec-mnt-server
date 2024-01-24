package com.example.projecmntserver.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BaseResponse {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
