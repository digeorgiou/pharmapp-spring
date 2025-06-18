package gr.aueb.cf.pharmapp_spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PharmacyInsertDTO(

        Long userId,

        @NotBlank(message = "Το Όνομα του φαρμακείου δεν μπορει να ειναι κενό")
        @Size(min = 4, max = 55,message = "Το Όνομα του φαρμακείου πρέπει να " +
                "ειναι από 4 μεχρι 55 χαρακτήρες")
        String name
) {}
