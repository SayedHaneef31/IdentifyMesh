package com.moonrider.Smart.Detection.System.Entity.DTO;
import lombok.*;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class IdentifyResponseDto {
    private UUID primaryContactId;
    private List<String> emails;
    private List<String> phoneNumbers;
    private List<UUID> secondaryContactIds;
}