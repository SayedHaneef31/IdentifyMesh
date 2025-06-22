package com.moonrider.Smart.Detection.System.Service.Impl;

import com.moonrider.Smart.Detection.System.Entity.Contact;
import com.moonrider.Smart.Detection.System.Entity.DTO.IdentifyRequestDto;
import com.moonrider.Smart.Detection.System.Entity.DTO.IdentifyResponseDto;
import com.moonrider.Smart.Detection.System.Entity.DTO.SimpleResponseDto;
import com.moonrider.Smart.Detection.System.Entity.LinkPrecedence;
import com.moonrider.Smart.Detection.System.Repository.ContactRepo;
import com.moonrider.Smart.Detection.System.Service.ContactService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {
    private final ContactRepo contactRepo;

    public ContactServiceImpl(ContactRepo contactRepo) {
        this.contactRepo = contactRepo;
    }


    @Override
    public IdentifyResponseDto identifyUser(IdentifyRequestDto identifyRequestDto) {
        List<Contact> matchedContacts = contactRepo.findByEmailOrPhoneNumber(identifyRequestDto.getEmail(), identifyRequestDto.getPhoneNumber());
        if(matchedContacts.isEmpty())
        {
            //Creating a new Entry
            Contact newUSer=Contact.builder()
                    .email(identifyRequestDto.getEmail())
                    .phoneNumber((identifyRequestDto.getPhoneNumber()))
                    .linkPrecedence(LinkPrecedence.PRIMARY)
                    .linkedId(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            contactRepo.save(newUSer);

            return IdentifyResponseDto.builder()
                    .primaryContactId(newUSer.getId())
                    .emails(Collections.singletonList(newUSer.getEmail()))
                    .phoneNumbers(Collections.singletonList(newUSer.getPhoneNumber()))
                    .secondaryContactIds(Collections.emptyList())
                    .build();
        }
        else
        {
            //We have something matching here --- either mail or number
            //merge it


            Set<Contact> identityGroup = new HashSet<>(matchedContacts);

            //Looking inside of each contact from matchedContacts to find the ultimate primary contracts...If any secondary contact is found...map it to it's primary contact
            for (Contact contact : matchedContacts) {
                if (contact.getLinkedId() != null) {
                    // This is a secondary → fetch the primary
                    contactRepo.findById(contact.getLinkedId()).ifPresent(identityGroup::add);
                }
            }

            // Getting all the UUId's of all primary contacts present in identityGroup
            Set<UUID> primaryIds = identityGroup.stream()
                    .map(c -> c.getLinkPrecedence() == LinkPrecedence.PRIMARY ? c.getId() : c.getLinkedId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            //fetching more contacts from the database that are linked to our primaryIds
            List<Contact> allLinkedContacts = contactRepo.findAll().stream()
                    .filter(c -> primaryIds.contains(c.getLinkedId()) || primaryIds.contains(c.getId()))
                    .toList();
            //Adding them to the identityGroup
            identityGroup.addAll(allLinkedContacts);

            /*   *********************
            Now we have all the matching contracts(either email or phone or both),there linked primaries and other secondaries linked to the above primaries
                 *********************
             */

            //Finding the oldest contacts that is primary
            Contact primary = identityGroup.stream()
                    .min(Comparator.comparing(Contact::getCreatedAt))
                    .orElseThrow();

            //Checking if identifyRequest.email is present in db or not
            boolean emailExists = identityGroup.stream()
                    .anyMatch(c -> Objects.equals(c.getEmail(), identifyRequestDto.getEmail()));
            //Checking if identifyRequest.phone is present in db or not
            boolean phoneExists = identityGroup.stream()
                    .anyMatch(c -> Objects.equals(c.getPhoneNumber(), identifyRequestDto.getPhoneNumber()));

            //if both email and phone already exist → nothing to save....We could carry on
            //but if either one is new, that means the user used a new contact method → we have to store it...Though marked as secondary.
            if (!emailExists || !phoneExists) {
                Contact newSecondary = Contact.builder()
                        .email(identifyRequestDto.getEmail())
                        .phoneNumber(identifyRequestDto.getPhoneNumber())
                        .linkedId(primary.getId())    //linking it to the primary
                        .linkPrecedence(LinkPrecedence.SECONDARY)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                contactRepo.save(newSecondary);
                identityGroup.add(newSecondary);
            }

            //Collecting the data from identityGroup for our final response
            Set<String> emails = identityGroup.stream()
                    .map(Contact::getEmail)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<String> phones = identityGroup.stream()
                    .map(Contact::getPhoneNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<UUID> secondaryIds = identityGroup.stream()
                    .map(Contact::getId)
                    .filter(id -> !id.equals(primary.getId()))
                    .toList();

            //final response
            return IdentifyResponseDto.builder()
                    .primaryContactId(primary.getId())
                    .emails(new ArrayList<>(emails))
                    .phoneNumbers(new ArrayList<>(phones))
                    .secondaryContactIds(secondaryIds)
                    .build();
        }
    }

    @Override
    public List<SimpleResponseDto> getAllContacts() {
        List<Contact> contacts = contactRepo.findAll();
        return contacts.stream()
                .map(contact -> SimpleResponseDto.builder()
                        .email(contact.getEmail())
                        .phoneNumber(contact.getPhoneNumber())
                        .build())
                .toList();
    }

    @Override
    public SimpleResponseDto getContactById(UUID id) {
        Optional<Contact> contactOpt= contactRepo.findById(id);
        if (contactOpt.isPresent())
        {
            Contact contact = contactOpt.get();
            return SimpleResponseDto.builder()
                    .email(contact.getEmail())
                    .phoneNumber(contact.getPhoneNumber())
                    .build();

        }
        else
        {
            return SimpleResponseDto.builder()
                    .email(null)
                    .phoneNumber(null)
                    .build();

        }

    }
}
