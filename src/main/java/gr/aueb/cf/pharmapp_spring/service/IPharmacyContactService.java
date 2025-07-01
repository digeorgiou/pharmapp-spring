package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.ContactInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.ContactReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.ContactUpdateDTO;

public interface IPharmacyContactService {

    ContactReadOnlyDTO createContact(ContactInsertDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException;
    ContactReadOnlyDTO updateContact(ContactUpdateDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException;
    void deleteContact(Long contactId) throws EntityNotFoundException;
    ContactReadOnlyDTO findById(Long contactId) throws EntityNotFoundException;
    boolean contactExists(Long userId, Long pharmacyId);
    String getContactName(Long userId, Long pharmacyId) throws EntityNotFoundException;
}
