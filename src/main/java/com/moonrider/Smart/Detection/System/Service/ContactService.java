package com.moonrider.Smart.Detection.System.Service;

import com.moonrider.Smart.Detection.System.Entity.DTO.IdentifyRequestDto;
import com.moonrider.Smart.Detection.System.Entity.DTO.IdentifyResponseDto;
import com.moonrider.Smart.Detection.System.Entity.DTO.SimpleResponseDto;

import java.util.List;
import java.util.UUID;

public interface ContactService {
    IdentifyResponseDto identifyUser(IdentifyRequestDto identifyRequestDto);

    List<SimpleResponseDto> getAllContacts();

    SimpleResponseDto getContactById(UUID id);
}
