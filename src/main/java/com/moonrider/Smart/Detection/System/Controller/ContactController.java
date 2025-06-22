package com.moonrider.Smart.Detection.System.Controller;

import com.moonrider.Smart.Detection.System.Entity.Contact;
import com.moonrider.Smart.Detection.System.Entity.DTO.IdentifyRequestDto;
import com.moonrider.Smart.Detection.System.Entity.DTO.IdentifyResponseDto;
import com.moonrider.Smart.Detection.System.Entity.DTO.SimpleResponseDto;
import com.moonrider.Smart.Detection.System.Repository.ContactRepo;
import com.moonrider.Smart.Detection.System.Service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController
{
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponseDto> identifyTheUser(@RequestBody IdentifyRequestDto identifyRequestDto)
    {
        IdentifyResponseDto response = contactService.identifyUser(identifyRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/fetch-all")
    public List<SimpleResponseDto> getAllContactsList()
    {
        return contactService.getAllContacts();
    }

    @GetMapping("/{id}")
    public SimpleResponseDto getContactByItsId(@PathVariable UUID id) {
        return contactService.getContactById(id);
    }
}
