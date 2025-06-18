package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.AppServerException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordUpdateDTO;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.model.Pharmacy;
import gr.aueb.cf.pharmapp_spring.model.TradeRecord;
import gr.aueb.cf.pharmapp_spring.model.User;
import gr.aueb.cf.pharmapp_spring.repository.PharmacyRepository;
import gr.aueb.cf.pharmapp_spring.repository.TradeRecordRepository;
import gr.aueb.cf.pharmapp_spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TradeRecordService implements ITradeRecordService{

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TradeRecordService.class);

    private final TradeRecordRepository tradeRecordRepository;
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Autowired
    public TradeRecordService(TradeRecordRepository tradeRecordRepository,
                              PharmacyRepository pharmacyRepository,
                              UserRepository userRepository, Mapper mapper){
        this.tradeRecordRepository = tradeRecordRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TradeRecordReadOnlyDTO createRecord(TradeRecordInsertDTO dto) throws EntityNotAuthorizedException, EntityNotFoundException{
        User recorderUser =
                userRepository.findById(dto.recorderUserId()).orElseThrow(()-> new EntityNotFoundException("User",
                        "User with id " + dto.recorderUserId() + " was not found"));

        Pharmacy giver = pharmacyRepository.findById(dto.giverPharmacyId()).orElseThrow(()-> new EntityNotFoundException("Pharmacy",
                "Pharmacy with id " + dto.giverPharmacyId() + " was not " +
                        "found"));

        Pharmacy receiver = pharmacyRepository.findById(dto.receiverPharmacyId()).orElseThrow(()-> new EntityNotFoundException("Pharmacy",
                "Pharmacy with id " + dto.giverPharmacyId() + " was not " +
                        "found"));

        boolean isGiverUser =
                giver.getUser() != null && giver.getUser().getId().equals(dto.recorderUserId());
        boolean isReceiverUser =
                receiver.getUser() != null && receiver.getUser().getId().equals(dto.recorderUserId());

        if(!userRepository.isAdmin(dto.recorderUserId()) && !isGiverUser && !isReceiverUser){
            throw new EntityNotAuthorizedException("User", "User with " +
                    "id=" + dto.recorderUserId() + "not authorized to " +
                    "create this trade record");
        }

        TradeRecord record = mapper.mapTradeRecordInsertToModel(dto);

        giver.addRecordGiver(record);
        receiver.addRecordReceiver(record);
        recorderUser.addRecordRecorder(record);
        record.setLastUpdater(recorderUser);

        pharmacyRepository.save(giver);
        pharmacyRepository.save(receiver);
        userRepository.save(recorderUser);

        TradeRecord savedRecord = tradeRecordRepository.save(record);

        LOGGER.info("TradeRecord created successfully with ID: {}",
                savedRecord.getId());

        return mapper.mapToTradeRecordReadOnlyDTO(savedRecord);

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public TradeRecordReadOnlyDTO updateRecord(TradeRecordUpdateDTO dto) throws EntityNotFoundException, EntityNotAuthorizedException, AppServerException {

        TradeRecord existingRecord = tradeRecordRepository.findById(dto.id())
                .orElseThrow(()-> new EntityNotFoundException("TradeRecord",
                        "TradeRecord with id " + dto.id() + " was not " +
                                "found"));

        User updaterUser = userRepository.findById(dto.updaterUserId()).orElseThrow(()-> new EntityNotFoundException("User",
                "User with id " + dto.updaterUserId() + " was not found"));

        // Verify updater is giver, receiver, or admin
        boolean isAdmin = userRepository.isAdmin(dto.updaterUserId());
        boolean isGiverUser = existingRecord.getGiver().getUser() != null &&
                existingRecord.getGiver().getUser().getId().equals(dto.updaterUserId());
        boolean isReceiverUser =
                existingRecord.getReceiver().getUser() != null &&
                        existingRecord.getReceiver().getUser().getId().equals(dto.updaterUserId());

        if (!isAdmin && !isGiverUser && !isReceiverUser) {
            throw new EntityNotAuthorizedException("User", "Only giver, " +
                    "receiver,  or admin can " +
                    "update records");
        }

        TradeRecord updatedRecord = mapper.mapTradeRecordUpdateToModel(dto,
                existingRecord);
        updatedRecord.setLastUpdater(updaterUser);

        TradeRecord savedRecord = tradeRecordRepository.save(updatedRecord);

        LOGGER.info("TradeRecord with id={} " +
                "updated by user with id={}", dto.id(), dto.updaterUserId());

        return mapper.mapToTradeRecordReadOnlyDTO(savedRecord);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteRecord(Long id, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException {

        TradeRecord record = tradeRecordRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("TradeRecord",
                "TradeRecord with id " + id + " was not found"));

        User deleterUser = userRepository.findById(deleterUserId).orElseThrow(()-> new EntityNotFoundException("User",
                "User with id " + deleterUserId + " was not found"));

        // Verify deleter is giver or receiver
        boolean isGiverUser = record.getGiver().getUser() != null &&
                record.getGiver().getUser().getId().equals(deleterUserId);
        boolean isReceiverUser =
                record.getReceiver().getUser() != null &&
                        record.getReceiver().getUser().getId().equals(deleterUserId);

        if (!isGiverUser && !isReceiverUser) {
            throw new EntityNotAuthorizedException("User", "Only giver, " +
                    "or receiver can delete records");
        }

        // Two-phase deletion logic
        if (isGiverUser) {
            record.setDeletedByGiver(true);
        } else {
            record.setDeletedByReceiver(true);
        }

        // Check if both parties have marked for deletion
        if (record.isDeletedByGiver() && record.isDeletedByReceiver()) {
            // Actually delete the record
            tradeRecordRepository.deleteById(id);
        } else {
            // Just update the deletion flags
            tradeRecordRepository.save(record);
        }

        LOGGER.info("TradeRecord with id={} " +
                        "deleted by user with id={}", id,
                deleterUserId);
    }

    @Override
    @Transactional
    public TradeRecordReadOnlyDTO restoreTradeRecord(Long recordId,
                                                     Long restorerUserId) throws EntityNotFoundException, EntityNotAuthorizedException{

        TradeRecord record = tradeRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("TradeRecord",
                        "TradeRecord with id " + recordId + " was not found"));

        User restorerUser = userRepository.findById(restorerUserId)
                .orElseThrow(() -> new EntityNotFoundException("User",
                        "User with id " + restorerUserId + " was not found"));

        // Verify restorer is giver or receiver
        boolean isGiverUser = record.getGiver().getUser() != null &&
                record.getGiver().getUser().getId().equals(restorerUserId);
        boolean isReceiverUser = record.getReceiver().getUser() != null &&
                record.getReceiver().getUser().getId().equals(restorerUserId);

        if (!isGiverUser && !isReceiverUser) {
            throw new EntityNotAuthorizedException("User", "Only giver or receiver can restore records");
        }

        record.setDeletedByGiver(false);
        record.setDeletedByReceiver(false);

        TradeRecord savedRecord = tradeRecordRepository.save(record);

        LOGGER.info("TradeRecord with id={} partially restored by user with id={}",
                recordId, restorerUserId);

        return mapper.mapToTradeRecordReadOnlyDTO(savedRecord);
    }

    @Override
    @Transactional
    public TradeRecordReadOnlyDTO getById(Long id) throws EntityNotFoundException {

        TradeRecord record = tradeRecordRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("TradeRecord",
                "TradeRecord with id " + id + " was not " +
                        "found"));
        return mapper.mapToTradeRecordReadOnlyDTO(record);
    }

    @Override
    @Transactional
    public List<TradeRecordReadOnlyDTO> getAll() throws AppServerException {

        List<TradeRecord> records = tradeRecordRepository.findAll();

        return records.stream().map(mapper::mapToTradeRecordReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TradeRecordReadOnlyDTO> getAllRecordsPaginated(int page, int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<TradeRecord> tradeRecordPage =
                tradeRecordRepository.findAll(pageable);

        return tradeRecordPage.map(mapper::mapToTradeRecordReadOnlyDTO);
    }

    @Override
    @Transactional
    public List<TradeRecordReadOnlyDTO> getRecentTradesForPharmacy(Long pharmacyId, int limit) throws EntityNotFoundException {

        pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacyId + " was not found"));

        List<TradeRecord> tradeRecords = tradeRecordRepository.findByGiverIdOrReceiverIdOrderByTransactionDateDesc(
                pharmacyId,
                pharmacyId,
                PageRequest.of(0, limit)
                //pageNumber : 0
                //limit : number of items in page
        );

        return tradeRecords.stream()
                .map(mapper::mapToTradeRecordReadOnlyDTO)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public List<TradeRecordReadOnlyDTO> getTradesBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id,
                                                                   LocalDateTime startDate, LocalDateTime endDate) throws EntityNotFoundException {
        pharmacyRepository.findById(pharmacy1Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy1Id + " was not found"));
        pharmacyRepository.findById(pharmacy2Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy2Id + " was not found"));

        // Get trades in both directions within date range
        List<TradeRecord> direction1 = tradeRecordRepository.findByGiverIdAndReceiverIdAndTransactionDateBetween(
                pharmacy1Id, pharmacy2Id, startDate, endDate);

        List<TradeRecord> direction2 = tradeRecordRepository.findByGiverIdAndReceiverIdAndTransactionDateBetween(
                pharmacy2Id, pharmacy1Id, startDate, endDate);

        List<TradeRecord> allRecords = new ArrayList<>();
        allRecords.addAll(direction1);
        allRecords.addAll(direction2);

        allRecords.sort((r1,r2)-> r2.getTransactionDate().compareTo(r1.getTransactionDate()));

        return allRecords.stream().map(mapper::mapToTradeRecordReadOnlyDTO)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public Page<TradeRecordReadOnlyDTO> getTradesBetweenPharmaciesPaginated(Long pharmacy1Id, Long pharmacy2Id,
                                                                            LocalDateTime startDate, LocalDateTime endDate, int page, int size)
            throws EntityNotFoundException {

        pharmacyRepository.findById(pharmacy1Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy1Id + " was not found"));
        pharmacyRepository.findById(pharmacy2Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy2Id + " was not found"));

        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();

        PageRequest pageable = PageRequest.of(page,size, Sort.by(
                "transactionDate").descending());

        // Get trades in both directions within date range
        Page<TradeRecord> direction1 =
                tradeRecordRepository.findByGiverIdAndReceiverIdAndTransactionDateBetween(
                pharmacy1Id, pharmacy2Id, effectiveStartDate, effectiveEndDate, pageable);

        Page<TradeRecord> direction2 =
                tradeRecordRepository.findByGiverIdAndReceiverIdAndTransactionDateBetween(
                pharmacy2Id, pharmacy1Id, effectiveStartDate, effectiveEndDate, pageable);

        List<TradeRecord> allRecords = new ArrayList<>();
        allRecords.addAll(direction1.getContent());
        allRecords.addAll(direction2.getContent());

        allRecords.sort(Comparator.comparing(TradeRecord::getTransactionDate).reversed());

        return new PageImpl<>(
                allRecords.stream()
                        .map(mapper::mapToTradeRecordReadOnlyDTO)
                        .collect(Collectors.toList()),
                pageable,
                direction1.getTotalElements() + direction2.getTotalElements()
        );
    }

    @Override
    @Transactional
    public Double calculateBalanceBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id) throws EntityNotFoundException {
        // Validate both pharmacies exist
        pharmacyRepository.findById(pharmacy1Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy1Id + " was not found"));
        pharmacyRepository.findById(pharmacy2Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy2Id + " was not found"));

        // Calculate totals in both directions
        Double given =
                tradeRecordRepository.sumAmountByGiverIdAndReceiverIdAndNotDeleted(pharmacy1Id, pharmacy2Id);
        Double received =
                tradeRecordRepository.sumAmountByGiverIdAndReceiverIdAndNotDeleted(pharmacy2Id, pharmacy1Id);

        // Handle null values
        given = given != null ? given : 0.0;
        received = received != null ? received : 0.0;

        return received - given;
    }

    @Override
    @Transactional
    public Integer getTradeCountBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id) throws EntityNotFoundException {

        // Validate both pharmacies exist
        pharmacyRepository.findById(pharmacy1Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy1Id + " was not found"));
        pharmacyRepository.findById(pharmacy2Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy2Id + " was not found"));

        // Get count in both directions
        long count1 = tradeRecordRepository.countByGiverIdAndReceiverId(pharmacy1Id, pharmacy2Id);
        long count2 = tradeRecordRepository.countByGiverIdAndReceiverId(pharmacy2Id, pharmacy1Id);

        return (int) (count1 + count2);
    }

    @Override
    @Transactional
    public List<TradeRecordReadOnlyDTO> getRecentTradesBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id, int limit)
            throws EntityNotFoundException {

        // Validate both pharmacies exist
        pharmacyRepository.findById(pharmacy1Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy1Id + " was not found"));
        pharmacyRepository.findById(pharmacy2Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy2Id + " was not found"));


        // Get recent trades in both directions
        List<TradeRecord> direction1 = tradeRecordRepository
                .findByGiverIdAndReceiverIdOrderByTransactionDateDesc(
                        pharmacy1Id, pharmacy2Id, PageRequest.of(0, limit));

        List<TradeRecord> direction2 = tradeRecordRepository
                .findByGiverIdAndReceiverIdOrderByTransactionDateDesc(
                        pharmacy2Id, pharmacy1Id, PageRequest.of(0, limit));

        // Combine, sort and limit
        return Stream.concat(direction1.stream(), direction2.stream())
                .sorted(Comparator.comparing(TradeRecord::getTransactionDate).reversed())
                .limit(limit)
                .map(mapper::mapToTradeRecordReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<TradeRecordReadOnlyDTO> getTradesForPharmacyPaginated(Long pharmacyId,
                                                                      LocalDateTime startDate,
                                                                      LocalDateTime endDate,
                                                                      int page,
                                                                      int size
    ) throws EntityNotFoundException {

        pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacyId + " was not found"));

        LocalDateTime effectiveStartDate = startDate != null ? startDate : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime effectiveEndDate = endDate != null ? endDate : LocalDateTime.now();

        PageRequest pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        Page<TradeRecord> tradesPage = tradeRecordRepository
                .findTradesBetweenPharmaciesAndDateRange(
                        pharmacyId,
                        pharmacyId,
                        effectiveStartDate,
                        effectiveEndDate,
                        pageable
                );

        return tradesPage.map(mapper::mapToTradeRecordReadOnlyDTO);
    }

    @Override
    public TradeRecordReadOnlyDTO settleBalance(Long pharmacy1Id, Long pharmacy2Id, Long userId, String description)
            throws EntityNotFoundException, EntityNotAuthorizedException,
            EntityInvalidArgumentException {

        Pharmacy pharmacy1 = pharmacyRepository.findById(pharmacy1Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy1Id + " not found"));

        Pharmacy pharmacy2 = pharmacyRepository.findById(pharmacy2Id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy",
                        "Pharmacy with id " + pharmacy2Id + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User",
                        "User with id " + userId + " not found"));


        // 2. Verify user owns at least one of the pharmacies
        boolean isPharmacy1Owner = pharmacy1.getUser() != null && pharmacy1.getUser().getId().equals(userId);
        boolean isPharmacy2Owner = pharmacy2.getUser() != null && pharmacy2.getUser().getId().equals(userId);

        if (!isPharmacy1Owner && !isPharmacy2Owner) {
            throw new EntityNotAuthorizedException("User", "User not " +
                    "authorized to settle balance between these pharmacies");
        }

        double balance = calculateBalanceBetweenPharmacies(pharmacy1Id,
                pharmacy2Id);

        if(Math.abs(balance) < 0.01){
            throw new EntityInvalidArgumentException("TradeRecord", "Balance is " +
                    "already zero");
        }

        TradeRecordInsertDTO dto = new TradeRecordInsertDTO(
                description != null? description : "Balance settlement",
                Math.abs(balance),
                LocalDateTime.now(),
                (balance > 0) ? pharmacy1Id : pharmacy2Id, //giver
                (balance > 0) ? pharmacy2Id : pharmacy1Id, //receiver
                userId
        );

        return createRecord(dto);
    }
}
