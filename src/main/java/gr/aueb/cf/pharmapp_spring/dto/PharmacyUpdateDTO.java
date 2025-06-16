package gr.aueb.cf.pharmapp_spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PharmacyUpdateDTO(

        Long id,
        @NotBlank(message = "Το Όνομα του φαρμακείου δεν μπορει να ειναι κενό")
        @Size(max = 55,message = "Το Όνομα του φαρμακείου πρέπει να ειναι " +
                "μεχρι 55 χαρακτήρες")
        String name,
        Long userId
) {}
