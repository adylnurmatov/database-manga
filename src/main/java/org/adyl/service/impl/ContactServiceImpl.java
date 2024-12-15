package org.adyl.service.impl;

import org.springframework.stereotype.Service;
import org.adyl.mapper.abstraction.AbstractMapper;
import org.adyl.model.Contact;
import org.adyl.model.dto.ContactDTO;
import org.adyl.repository.ContactRepository;
import org.adyl.service.DefaultService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements DefaultService<ContactDTO, Contact, Integer> {
    private final ContactRepository contactRepository;
    private final AbstractMapper mapper;

    public ContactServiceImpl(ContactRepository contactRepository, AbstractMapper mapper) {
        this.contactRepository = contactRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ContactDTO> findAll() {
        List<Contact> contacts = contactRepository.findAll();
        return contacts.stream().map(contact -> mapper.toDTO(contact, ContactDTO.class)).collect(Collectors.toList());
    }

    @Override
    public ContactDTO findByKey(Integer key) {
        return mapper.toDTO(contactRepository.findById(key), ContactDTO.class);
    }

    @Override
    public ContactDTO save(ContactDTO obj) {
        Contact contact = contactRepository.save(mapper.toEntity(obj, Contact.class));
        return mapper.toDTO(contact, ContactDTO.class);
    }

    @Override
    public ContactDTO update(Integer key, ContactDTO obj) {
        Contact contact = mapper.toEntity(obj, Contact.class);
        contact.setId(key);
        contactRepository.save(contact);
        return mapper.toDTO(contact, ContactDTO.class);
    }

    @Override
    public void delete(Integer key) {
        contactRepository.deleteById(key);
    }
}
