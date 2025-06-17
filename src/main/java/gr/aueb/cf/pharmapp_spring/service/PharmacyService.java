package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.AppServerException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PharmacyService implements IPharmacyService{

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PharmacyService.class);

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final ITradeRecordService tradeRecordService;
    private final PharmacyContactRepository contactRepository;
    private final Mapper mapper;

    @Autowired
    public PharmacyService(PharmacyRepository pharmacyRepository, UserRepository userRepository,
                           ITradeRecordService tradeRecordService,
                           PharmacyContactRepository contactRepository,
                           Mapper mapper) {
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
        this.tradeRecordService = tradeRecordService;
        this.contactRepository = contactRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto) throws EntityAlreadyExistsException, EntityNotFoundException{

        if(pharmacyRepository.existsByName(dto.name())){
            throw new EntityAlreadyExistsException("Pharmacy", "Name " + dto.name() + " already exists");
        }

        User creator = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User", "Creator not found"));

        Pharmacy pharmacy = mapper.mapPharmacyInsertToModel(dto);

        creator.addPharmacy(pharmacy);
        userRepository.save(creator);

        Pharmacy insertedPharmacy = pharmacyRepository.save(pharmacy);

        LOGGER.info("Pharmacy created with ID: {}", insertedPharmacy.getId());

        return mapper.mapToPharmacyReadOnlyDTO(insertedPharmacy);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto) throws EntityAlreadyExistsException,
            EntityNotAuthorizedException, EntityNotFoundException{

        Pharmacy existingPharmacy = pharmacyRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy"
                        ,  + dto.id() + "not found"));

        if(!userRepository.isAdmin(dto.userId()) && !Objects.equals(existingPharmacy.getUser().getId(),dto.userId())){
            throw new EntityNotAuthorizedException("User", "User is not " +
                    "authorized to update Pharmacy with id=" + dto.id());
        }

        if(!existingPharmacy.getName().equals(dto.name()) && pharmacyRepository.existsByName(dto.name())){
            throw new EntityAlreadyExistsException("Pharmacy", "Name " + dto.name() + " already exists");
        }

        Pharmacy updatedPharmacy = mapper.mapPharmacyUpdateToModel(dto,
                existingPharmacy);
        Pharmacy savedPharmacy = pharmacyRepository.save(updatedPharmacy);

        LOGGER.info("Pharmacy {} updated by user {}", dto.id(),
                dto.userId());

        return mapper.mapToPharmacyReadOnlyDTO(savedPharmacy);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException {

        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy", id + " not found"));

        pharmacy.setActive(false);
        pharmacy.setDeletedAt(LocalDateTime.now());

        if(pharmacy.getUser() != null) {
            pharmacy.getUser().removePharmacy(pharmacy);
        }

        pharmacyRepository.save(pharmacy);

        LOGGER.info("Pharmacy {} soft-deleted by user {}", id, deleterUserId);
    }

    @Override
    @Transactional
    public boolean nameExists(String name) throws AppServerException {
        return pharmacyRepository.existsByName(name);
    }

    @Override
    @Transactional
    public PharmacyReadOnlyDTO getById(Long id) throws EntityNotFoundException {

        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy", id + " not found"));

        return mapper.mapToPharmacyReadOnlyDTO(pharmacy);
    }

    @Override
    @Transactional
    public PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException {

        Pharmacy pharmacy = pharmacyRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy", "Name " + name + " not found"));

        return mapper.mapToPharmacyReadOnlyDTO(pharmacy);
    }

    @Override
    @Transactional
    public List<PharmacyReadOnlyDTO> searchPharmaciesByName(String name){
        if(name == null || name.trim().isEmpty()){
            return Collections.emptyList();
        }
        List<Pharmacy> pharmacies =
                pharmacyRepository.findByNameStartingWithIgnoreCase(name);

        return pharmacies.stream()
                .map(mapper::mapToPharmacyReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<PharmacyReadOnlyDTO> searchPharmaciesByNamePaginated(String name, int page, int size) {
        if(name == null || name.trim().isEmpty()){
            return Page.empty();
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<Pharmacy> pharmacyPage =
                pharmacyRepository.findByNameStartingWithIgnoreCase(name,
                        pageable);

        return pharmacyPage.map(mapper::mapToPharmacyReadOnlyDTO);
    }

    @Override
    @Transactional
    public List<PharmacyReadOnlyDTO> searchPharmaciesByUser(String username){
        if(username == null || username.trim().isEmpty()){
            return Collections.emptyList();
        }
        List<Pharmacy> pharmacies =
                pharmacyRepository.findByUserUsernameStartingWithIgnoreCase(username);

        return pharmacies.stream()
                .map(mapper::mapToPharmacyReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PharmacyReadOnlyDTO> searchPharmaciesByUserPaginated(String username, int page, int size) {
        if (username == null || username.trim().isEmpty()) {
            return Page.empty();
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<Pharmacy> pharmacyPage = pharmacyRepository
                .findByUserUsernameStartingWithIgnoreCase(username,pageable);

        return pharmacyPage.map(mapper::mapToPharmacyReadOnlyDTO);
    }

    @Override
    @Transactional
    public List<PharmacyReadOnlyDTO> getAllPharmacies() throws AppServerException {

        List<Pharmacy> pharmacies = pharmacyRepository.findAll();

        return pharmacies.stream()
                .map(mapper::mapToPharmacyReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PharmacyReadOnlyDTO> getAllPharmaciesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pharmacy> pharmacyPage = pharmacyRepository.findAll(pageable);
        return pharmacyPage.map(mapper::mapToPharmacyReadOnlyDTO);
    }

    @Override
    @Transactional
    public List<BalanceDTO> getBalanceList(Long pharmacyId, String sortBy) throws EntityNotFoundException {

        Pharmacy selectedPharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id = " + pharmacyId + " not found"));

        List<PharmacyContact> contacts =
                contactRepository.findByUserId(selectedPharmacy.getUser().getId());

        List<BalanceDTO> balanceList = new ArrayList<>();

        for(PharmacyContact contact : contacts) {
            Pharmacy contactPharmacy = contact.getPharmacy();
            if(contactPharmacy == null) continue;

            double balance = tradeRecordService.calculateBalanceBetweenPharmacies(
                    selectedPharmacy.getId(),
                    contactPharmacy.getId()
            );

            Integer tradeCount =
                    tradeRecordService.getTradeCountBetweenPharmacies(
                            selectedPharmacy.getId(),
                            contactPharmacy.getId()
                    );

            List<TradeRecordReadOnlyDTO> recentTrades =
                    tradeRecordService.getRecentTradesBetweenPharmacies(
                            selectedPharmacy.getId(),
                            contactPharmacy.getId(),
                            5
                    );

            balanceList.add(new BalanceDTO(
                    contact.getContactName() != null ?
                            contact.getContactName() : "Contact",
                    contactPharmacy.getName() != null ?
                            contactPharmacy.getName() : "Pharmacy",
                    balance,
                    recentTrades,
                    tradeCount,
                    contactPharmacy.isActive()
            ));
        }

        return sortBalanceList(balanceList, sortBy);
    }

    @Override
    public Page<BalanceDTO> getBalanceListPaginated(Long pharmacyId,
                                                    String sortBy, int page,
                                                    int size) throws EntityNotFoundException{

        Pharmacy selectedPharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id = " + pharmacyId + " not found"));

        PageRequest pageable = PageRequest.of(page, size);

        Page<PharmacyContact> contactPage =
                contactRepository.findByUser(selectedPharmacy.getUser(),
                        pageable);

        List<PharmacyContact> contacts = contactPage.getContent();
        List<BalanceDTO> balanceList = new ArrayList<>();

        for(PharmacyContact contact : contacts) {
            Pharmacy contactPharmacy = contact.getPharmacy();
            if(contactPharmacy == null) continue;

            double balance = tradeRecordService.calculateBalanceBetweenPharmacies(
                    selectedPharmacy.getId(),
                    contactPharmacy.getId()
            );

            Integer tradeCount =
                    tradeRecordService.getTradeCountBetweenPharmacies(
                            selectedPharmacy.getId(),
                            contactPharmacy.getId()
                    );

            List<TradeRecordReadOnlyDTO> recentTrades =
                    tradeRecordService.getRecentTradesBetweenPharmacies(
                            selectedPharmacy.getId(),
                            contactPharmacy.getId(),
                            5
                    );

            balanceList.add(new BalanceDTO(
                    contact.getContactName() != null ?
                            contact.getContactName() : "Contact",
                    contactPharmacy.getName() != null ?
                            contactPharmacy.getName() : "Pharmacy",
                    balance,
                    recentTrades,
                    tradeCount,
                    contactPharmacy.isActive()
            ));
        }

        // Sort if needed
        if (sortBy != null && !sortBy.isEmpty()) {
            balanceList = sortBalanceList(balanceList, sortBy);
        }

        return new PageImpl<>(
                balanceList,
                pageable,
                contactPage.getTotalElements()
        );
    }

    private List<BalanceDTO> sortBalanceList(List<BalanceDTO> balanceList,
                                             String sortBy){

        if(sortBy == null || sortBy.isEmpty()){
            return balanceList; // Return unsorted if no sorting specified
        }

        boolean ascending = !sortBy.startsWith("-");
        String sortField = ascending ? sortBy : sortBy.substring(1);

        Comparator<BalanceDTO> comparator = switch (sortField.toLowerCase()){
            case "contactname" -> Comparator.comparing(BalanceDTO::contactName,String.CASE_INSENSITIVE_ORDER);
            case "tradecount" -> Comparator.comparing(BalanceDTO::tradeCount);
            case "balance" -> Comparator.comparingDouble(BalanceDTO::balance);
            default -> (a,b) -> 0; //No sorting if field is invalid
        };

        // Reverse for descending order
        if (!ascending) {
            comparator = comparator.reversed();
        }

        return balanceList.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

}
