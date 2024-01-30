package com.example.projecmntserver.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    @NotBlank
    private String name;
    @NotNull
    private Long teamId;
    @NotBlank
    private String jiraMemberId;
}
