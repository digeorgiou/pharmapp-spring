package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.dto.UserInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

//    @GetMapping("/login")
//    public String showLoginForm(
//            @RequestParam(required = false) String error,
//            Model model) {
//
//        if (error != null) {
//            model.addAttribute("error", "Invalid username or password");
//        }
//        return "login";
//    }

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
            if(e.getMessage().contains("Email")){
                bindingResult.rejectValue("email","email.exists", e.getMessage());
            } else {
                bindingResult.rejectValue("username", "user.exists", e.getMessage());
            }
            return "register";
        }
    }

    @GetMapping("/registration-success")
    public String showRegistrationSuccess() {
        return "registration-success";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }
}
