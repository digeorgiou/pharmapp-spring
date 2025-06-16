package gr.aueb.cf.pharmapp_spring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record PharmacyReadOnlyDTO(

        Long id,
        String name,
        LocalDateTime createdAt,
        String ownerUsername,
        LocalDateTime updatedAt,
        String updatedBy,
        boolean isActive,
        LocalDateTime deletedAt
) {}
