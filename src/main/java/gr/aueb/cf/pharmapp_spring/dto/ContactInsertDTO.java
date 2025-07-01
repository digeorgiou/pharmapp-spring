package gr.aueb.cf.pharmapp_spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactInsertDTO(
        Long userId,
        Long pharmacyId,
        @NotBlank(message = "Το Όνομα της επαφής δεν μπορει να ειναι κενό")
        @Size(min = 4, max = 55,message = "Το Όνομα της επαφής πρέπει να " +
                "έχει από 4 μεχρι 55 χαρακτήρες")
        String contactName
) {}
