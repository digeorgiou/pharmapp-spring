package gr.aueb.cf.pharmapp_spring.dto;


import java.time.LocalDateTime;

public record TradeRecordReadOnlyDTO(

        Long id,
        String description,
        Double amount,
        String giverName,
        String receiverName,
        String recorderUsername,
        LocalDateTime transactionDate,
        Boolean deletedByGiver,
        Boolean deletedByReceiver,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String updatedBy
) {}
