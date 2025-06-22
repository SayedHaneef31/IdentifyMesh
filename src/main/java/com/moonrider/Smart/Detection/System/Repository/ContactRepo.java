package com.moonrider.Smart.Detection.System.Repository;

import com.moonrider.Smart.Detection.System.Entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepo extends JpaRepository<Contact, UUID> {
    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);
}
