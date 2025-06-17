package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.ContactInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.ContactReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.ContactUpdateDTO;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.model.Pharmacy;
import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import gr.aueb.cf.pharmapp_spring.model.User;
import gr.aueb.cf.pharmapp_spring.repository.PharmacyContactRepository;
import gr.aueb.cf.pharmapp_spring.repository.PharmacyRepository;
import gr.aueb.cf.pharmapp_spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PharmacyContactService implements IPharmacyContactService{

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PharmacyContactService.class);

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PharmacyContactRepository contactRepository;
    private final Mapper mapper;

    @Autowired
    public PharmacyContactService(PharmacyRepository pharmacyRepository, UserRepository userRepository,
                                  PharmacyContactRepository contactRepository, Mapper mapper) {
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ContactReadOnlyDTO createContact(ContactInsertDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException {

        User user = userRepository.findById(dto.userId()).orElseThrow(()-> new EntityNotFoundException("User",
                "User with id " + dto.userId() + " was not found"));

        Pharmacy pharmacy = pharmacyRepository.findById(dto.pharmacyId()).orElseThrow(()-> new EntityNotFoundException("Pharmacy",
                "Pharmacy with id " + dto.pharmacyId() + " was " +
                        "not found"));

        if (contactRepository.existsByUserIdAndPharmacyId(dto.userId(), pharmacy.getId())){
            throw new EntityAlreadyExistsException("Contact",
                    "Contact already exists");
        }

        PharmacyContact contact = mapper.mapPharmacyContactInsertToModel(dto);

        pharmacy.addContactReference(contact);
        user.addContact(contact);

        pharmacyRepository.save(pharmacy);
        userRepository.save(user);

        PharmacyContact insertedContact = contactRepository.save(contact);

        LOGGER.info("Contact created with ID: {}", insertedContact.getId());

        return mapper.mapToPharmacyContactReadOnlyDTO(insertedContact);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ContactReadOnlyDTO updateContact(ContactUpdateDTO dto) throws EntityNotFoundException {

        PharmacyContact existingContact = contactRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Contact", "Contact with id " + dto.id() + " not found"));

        PharmacyContact updatedContact =
                mapper.mapPharmacyContactUpdateToModel(dto, existingContact);

        PharmacyContact savedContact = contactRepository.save(updatedContact);

        LOGGER.info("Contact {} updated", savedContact.getId());

        return mapper.mapToPharmacyContactReadOnlyDTO(savedContact);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteContact(Long contactId) throws EntityNotFoundException {

        PharmacyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact", "Contact with id " + contactId + " not found"));

        if(contact.getPharmacy() != null){
            contact.getPharmacy().removeContactReference(contact);
        }
        if(contact.getUser() != null){
            contact.getUser().removeContact(contact);
        }

        contactRepository.deleteById(contactId);

        LOGGER.info("Contact {} deleted", contactId);

    }

    @Override
    @Transactional
    public ContactReadOnlyDTO findById(Long contactId) throws EntityNotFoundException {

        PharmacyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new EntityNotFoundException("Contact", "Contact with id " + contactId + " not found"));

        return mapper.mapToPharmacyContactReadOnlyDTO(contact);
    }

    @Override
    @Transactional
    public boolean contactExists(Long userId, Long pharmacyId) {

        return contactRepository.existsByUserIdAndPharmacyId(userId,
                pharmacyId);
    }
}
