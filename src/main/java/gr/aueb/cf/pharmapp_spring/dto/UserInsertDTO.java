package gr.aueb.cf.pharmapp_spring.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserInsertDTO {
    @NotEmpty(message = "Παρακαλώ εισάγεται username")
    @Size(min = 4, max = 55, message = "Το username πρέπει να έχει 4 ως 55 χαρακτήρες")
    private String username;

    @NotEmpty(message = "Παρακαλώ εισάγεται κωδικό")
    @Size(min = 4, max = 30, message = "Ο κωδικός πρέπει να έχει 4 ως 30 χαρακτήρες")
    private String password;

    @NotEmpty(message = "Παρακαλώ εισάγεται κωδικό")
    @Size(min = 4, max = 30, message = "Ο κωδικός πρέπει να έχει 4 ως 30 χαρακτήρες")
    private String confirmedPassword;

    @NotEmpty(message = "Το email δεν μπορεί να ειναι κενό")
    @Email(message = "Παρακαλώ εισάγετε έγκυρο email")
    private String email;

    @NotNull(message = "Η εγγραφή απαιτεί αποδοχή των όρων")
    boolean termsAccepted;

    @AssertTrue(message = "Οι κωδικοί πρέπει να ταιριάζουν")
    public boolean isPasswordsMatch() {
        return password!= null && password.equals(confirmedPassword);
    }
}
