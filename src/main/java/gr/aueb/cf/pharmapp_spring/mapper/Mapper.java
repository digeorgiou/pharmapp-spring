package gr.aueb.cf.pharmapp_spring.mapper;

import gr.aueb.cf.pharmapp_spring.core.enums.Role;
import gr.aueb.cf.pharmapp_spring.dto.*;
import gr.aueb.cf.pharmapp_spring.model.Pharmacy;
import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import gr.aueb.cf.pharmapp_spring.model.TradeRecord;
import gr.aueb.cf.pharmapp_spring.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {


    public Pharmacy mapPharmacyInsertToModel(PharmacyInsertDTO dto){

        return Pharmacy.builder()
                .name(dto.name())
                .isActive(true)
                .build();

    }

    public Pharmacy mapPharmacyUpdateToModel(PharmacyUpdateDTO dto,
                                                    Pharmacy existingPharmacy){

        existingPharmacy.setName(dto.name());
        return existingPharmacy;
    }

    public PharmacyReadOnlyDTO mapToPharmacyReadOnlyDTO(Pharmacy pharmacy){

        return new PharmacyReadOnlyDTO(
                pharmacy.getId(),
                pharmacy.getName(),
                pharmacy.getCreatedAt(),
                pharmacy.getUser() != null ?
                        pharmacy.getUser().getUsername() : "deleted user",
                pharmacy.getUpdatedAt(),
                pharmacy.getLastUpdater().getUsername(),
                pharmacy.isActive(),
                pharmacy.getDeletedAt()
        );

    }


    public PharmacyContact mapPharmacyContactInsertToModel(ContactInsertDTO dto){
        return PharmacyContact.builder()
                .contactName(dto.contactName())
                .build();
    }

    // Update DTO → Entity
    public PharmacyContact mapPharmacyContactUpdateToModel(ContactUpdateDTO dto, PharmacyContact existingContact) {

        existingContact.setContactName(dto.contactName());
        return existingContact;

    }

    // Entity → ReadOnly DTO
    public ContactReadOnlyDTO mapToPharmacyContactReadOnlyDTO(PharmacyContact pharmacyContact) {
        return new ContactReadOnlyDTO(
                pharmacyContact.getId(),
                pharmacyContact.getUser().getUsername(),
                pharmacyContact.getContactName(),
                pharmacyContact.getPharmacy().getName(),
                pharmacyContact.getCreatedAt(),
                pharmacyContact.getUpdatedAt(),
                pharmacyContact.getLastUpdater().getUsername()
        );
    }


    // Insert DTO → Entity
    public TradeRecord mapTradeRecordInsertToModel(TradeRecordInsertDTO dto) {

        return TradeRecord.builder()
                .description(dto.description())
                .amount(dto.amount())
                .transactionDate(dto.transactionDate())
                .build();
    }

    // Update DTO → Entity
    public TradeRecord mapTradeRecordUpdateToModel(TradeRecordUpdateDTO dto, TradeRecord existingTradeRecord) {

        existingTradeRecord.setAmount(dto.amount());
        existingTradeRecord.setDescription(dto.description());
        existingTradeRecord.setTransactionDate(dto.transactionDate());

        return existingTradeRecord;

    }

    // Entity → ReadOnly DTO
    public TradeRecordReadOnlyDTO mapToTradeRecordReadOnlyDTO(TradeRecord tradeRecord) {

        return new TradeRecordReadOnlyDTO(
                tradeRecord.getId(),
                tradeRecord.getDescription(),
                tradeRecord.getAmount(),
                tradeRecord.getGiver().getName(),
                tradeRecord.getReceiver().getName(),
                tradeRecord.getRecorder().getUsername(),
                tradeRecord.getTransactionDate(),
                tradeRecord.isDeletedByGiver(),
                tradeRecord.isDeletedByReceiver(),
                tradeRecord.getCreatedAt(),
                tradeRecord.getUpdatedAt(),
                tradeRecord.getLastUpdater().getUsername()
        );
    }

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {

        return new UserReadOnlyDTO(
          user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole().toString(),
                user.getIsActive(),
                user.getDeletedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastUpdater().getUsername(),
                user.getAllPharmacies().stream()
                        .map(this::mapToPharmacyReadOnlyDTO)
                        .collect(Collectors.toSet())
        );
    }

    public User mapUserInsertToModel(UserInsertDTO dto){
        return User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    public User mapUserUpdateToModel(UserUpdateDTO dto, User existingUser){

        existingUser.setEmail(dto.getEmail());
        existingUser.setUsername(dto.getUsername());

        return existingUser;
    }

}
