package gr.aueb.cf.pharmapp_spring.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record TradeRecordInsertDTO(
        @NotBlank(message = "Παρακαλώ εισάγεται περιγραφή")
        @Size(min = 2, max = 255,
                message = "Η περιγραφή πρέπει να έχει 2 ως 255 χαρακτήρες")
        String description,

        @Positive(message = "Το ποσό πρέπει να είναι θετικός αριθμός")
        @NotNull(message = "Το ποσό δεν μπορεί να είναι κενό")
        @Max(value = 100000,
                message = "Το ποσό πρέπει να έχει μέχρι 100.000")
        Double amount,

        @NotNull(message = "Η ημερόμηνία δεν μπορεί να είναι κενή")
        @Past(message = "Η ημερομηνία δεν μπορεί να είναι μελλοντική")
        LocalDateTime transactionDate,

        Long giverPharmacyId,

        Long receiverPharmacyId,

        Long recorderUserId
){}
