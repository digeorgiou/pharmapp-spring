package gr.aueb.cf.pharmapp_spring.dto;

import java.time.LocalDateTime;

public record ContactReadOnlyDTO(

        Long id,
        String username,
        String contactName,
        String pharmacyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String updatedBy
) {}
