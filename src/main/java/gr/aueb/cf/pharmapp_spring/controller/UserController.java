package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.UserInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserUpdateDTO;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final Mapper mapper;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userInsertDTO", new UserInsertDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userInsertDTO") UserInsertDTO userInsertDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        LOGGER.info("Attempting to register user: {}", userInsertDTO.getUsername());

        if(bindingResult.hasErrors()) {
            LOGGER.warn("Registration form has errors: {}", bindingResult.getAllErrors());
            return "register";
        }

        try {
            UserReadOnlyDTO newUser = userService.createUser(userInsertDTO);
            LOGGER.info("Successfully registered user: {}", userInsertDTO.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful!");
            redirectAttributes.addFlashAttribute("user", newUser);
            return "redirect:/users/registration-success";
        } catch (EntityAlreadyExistsException e) {
            if(e.getMessage().contains("email")){
                bindingResult.rejectValue("email","email.exists", e.getMessage());
            } else {
                bindingResult.rejectValue("username", "user.exists", e.getMessage());
            }
            return "register";
        }
    }

    @GetMapping("/update")
    public String showUpdateUserForm(
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setId(user.getId());
            updateDTO.setUsername(user.getUsername());
            updateDTO.setEmail(user.getEmail());
            // Don't set passwords - user will enter new ones

            model.addAttribute("userUpdateDTO", updateDTO);
            model.addAttribute("user", user);

            return "update-user";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found. Log in again");
            return "redirect:/logout";
        }
    }

    @PostMapping("/update")
    public String updateUser(
            @Valid @ModelAttribute("userUpdateDTO") UserUpdateDTO updateDTO,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        // Custom validation for password confirmation
        if (!updateDTO.getPassword().equals(updateDTO.getConfirmedPassword())) {
            bindingResult.rejectValue("confirmedPassword", "password.mismatch",
                    "Οι κωδικοί δεν ταιριάζουν");
        }

        if (bindingResult.hasErrors()) {
            try {
                String username = authentication.getName();
                UserReadOnlyDTO user = userService.getUserByUsername(username);
                model.addAttribute("user", user);
                return "update-user";
            } catch (EntityNotFoundException e) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/logout";
            }
        }

        try {
            String currentUsername = authentication.getName();
            UserReadOnlyDTO currentUser = userService.getUserByUsername(currentUsername);

            // Check if username is being changed
            boolean usernameChanged = !currentUser.getUsername().equals(updateDTO.getUsername());

            // Ensure the user is updating their own profile
            updateDTO.setId(currentUser.getId());

            UserReadOnlyDTO updatedUser = userService.updateUser(updateDTO);

            // If username changed, force logout and redirect to login
            if (usernameChanged) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Το προφίλ σας ενημερώθηκε επιτυχώς! Παρακαλώ συνδεθείτε ξανά με το νέο όνομα χρήστη.");

                // Invalidate current session
                request.getSession().invalidate();
                return "redirect:/login?updated";
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Το προφίλ σας ενημερώθηκε επιτυχώς!");
            redirectAttributes.addFlashAttribute("user", updatedUser);
            return "redirect:/users/update-success";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/logout";
        } catch (EntityAlreadyExistsException e) {
            if (e.getMessage().contains("Email") || e.getMessage().contains("email")) {
                bindingResult.rejectValue("email", "email.exists", e.getMessage());
            } else {
                bindingResult.rejectValue("username", "username.exists", e.getMessage());
            }

            String username = authentication.getName();
            try {
                UserReadOnlyDTO user = userService.getUserByUsername(username);
                model.addAttribute("user", user);
                return "update-user";
            } catch (EntityNotFoundException ex) {
                redirectAttributes.addFlashAttribute("error", "User not found. Log in again");
                return "redirect:/logout";
            }
        }
    }

    @GetMapping("/registration-success")
    public String showRegistrationSuccess() {
        return "registration-success";
    }

    @GetMapping("/update-success")
    public String showUpdateSuccess() {
        return "update-user-success";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }
}