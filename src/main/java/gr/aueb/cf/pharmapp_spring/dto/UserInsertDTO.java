package gr.aueb.cf.pharmapp_spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserInsertDTO {
    @NotBlank(message = "Παρακαλώ εισάγεται username")
    @Size(min = 4, max = 55, message = "Το username πρέπει να έχει 4 ως 55 χαρακτήρες")
    private String username;

    @NotBlank(message = "Παρακαλώ εισάγεται κωδικό")
    @Size(min = 4, max = 30, message = "Ο κωδικός πρέπει να έχει 4 ως 30 χαρακτήρες")
    private String password;

    @NotBlank(message = "Παρακαλώ εισάγεται κωδικό")
    @Size(min = 4, max = 30, message = "Ο κωδικός πρέπει να έχει 4 ως 30 χαρακτήρες")
    private String confirmedPassword;

    @NotBlank(message = "Το email δεν μπορεί να ειναι κενό")
    @Email(message = "Παρακαλώ εισάγετε έγκυρο email")
    private String email;

    @NotEmpty(message = "Ο ρόλος δεν μπορεί να είναι κενός")
    private String role;
}
