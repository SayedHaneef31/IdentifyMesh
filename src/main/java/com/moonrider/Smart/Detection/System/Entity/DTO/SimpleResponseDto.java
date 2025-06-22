package com.moonrider.Smart.Detection.System.Entity.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpleResponseDto {
    private String email;
    private String phoneNumber;
}
