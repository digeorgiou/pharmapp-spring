package gr.aueb.cf.pharmapp_spring.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserReadOnlyDTO{
        Long id;
        String username;
        String password;
        String email;
        String role;
        boolean isActive;
        LocalDateTime deletedAt;
        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        String lastUpdater;
        Set<PharmacyReadOnlyDTO> pharmacies;

}
