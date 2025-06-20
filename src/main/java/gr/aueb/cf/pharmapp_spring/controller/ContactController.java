package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.*;
import gr.aueb.cf.pharmapp_spring.service.PharmacyContactService;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    private final PharmacyContactService contactService;
    private final PharmacyService pharmacyService;
    private final UserService userService;

    public ContactController(PharmacyContactService contactService,
                             PharmacyService pharmacyService,
                             UserService userService) {
        this.contactService = contactService;
        this.pharmacyService = pharmacyService;
        this.userService = userService;
    }

    @GetMapping("/add")
    public String showAddContactForm(
            @RequestParam(required = false) String nameSearch,
            @RequestParam(required = false) String userSearch,
            Authentication authentication,
            Model model) {

        try {
            String username = authentication.getName();

            UserReadOnlyDTO user = userService.getUserByUsername(username);

            model.addAttribute("user", user);
            model.addAttribute("username", username);
            model.addAttribute("contactInsertDTO", new ContactInsertDTO(null,
                    null, null));

            if (nameSearch != null && !nameSearch.isEmpty()) {
                List<PharmacyReadOnlyDTO> searchResults = pharmacyService.searchPharmaciesByName(nameSearch);
                model.addAttribute("searchResults", searchResults);
                model.addAttribute("searchType", "name");
                model.addAttribute("nameSearch", nameSearch);
            } else if (userSearch != null && !userSearch.isEmpty()) {
                List<PharmacyReadOnlyDTO> searchResults = pharmacyService.searchPharmaciesByUser(userSearch);
                model.addAttribute("searchResults", searchResults);
                model.addAttribute("searchType", "user");
                model.addAttribute("userSearch", userSearch);
            }

            return "add-contact";
        }catch (EntityNotFoundException e){
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/add")
    public String addContact(
            @RequestParam Long pharmacyId,
            @RequestParam String contactName,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        try {
            UserReadOnlyDTO user = userService.getUserByUsername(username);
            PharmacyReadOnlyDTO pharmacy = pharmacyService.getById(pharmacyId);

            ContactInsertDTO dto = new ContactInsertDTO(
                    user.getId(),
                    pharmacyId,
                    contactName
            );

            ContactReadOnlyDTO contact = contactService.createContact(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Contact with " + pharmacy.name() + " added successfully!");
            redirectAttributes.addFlashAttribute("pharmacy",pharmacy);
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("contact", contact);
            return "redirect:/contacts/add-success";
        } catch (EntityNotFoundException | EntityAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/update")
    public String showUpdateContactForm(
            @RequestParam Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);
            ContactReadOnlyDTO contact = contactService.findById(id);

            // Verify that the contact belongs to the current user
            if (!contact.username().equals(username)) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/dashboard";
            }

            ContactUpdateDTO updateDTO = new ContactUpdateDTO(
                    contact.id(),
                    user.getId(),
                    contact.pharmacyId(),
                    contact.contactName()
            );

            model.addAttribute("contactUpdateDTO", updateDTO);
            model.addAttribute("contact", contact);
            model.addAttribute("user", user);

            return "update-contact";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/update")
    public String updateContact(
            @Valid @ModelAttribute("contactUpdateDTO") ContactUpdateDTO updateDTO,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            try {
                String username = authentication.getName();
                UserReadOnlyDTO user = userService.getUserByUsername(username);
                ContactReadOnlyDTO contact = contactService.findById(updateDTO.id());

                model.addAttribute("contact", contact);
                model.addAttribute("user", user);
                return "update-contact";
            } catch (EntityNotFoundException e) {
                redirectAttributes.addFlashAttribute("error", "Contact not found");
                return "redirect:/dashboard";
            }
        }

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            ContactUpdateDTO dto = new ContactUpdateDTO(
                    updateDTO.id(),
                    user.getId(),
                    updateDTO.pharmacyId(),
                    updateDTO.contactName()
            );

            ContactReadOnlyDTO updatedContact = contactService.updateContact(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Contact '" + updatedContact.contactName() + "' updated successfully!");
            redirectAttributes.addFlashAttribute("contact", updatedContact);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/contacts/update-success";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/delete")
    public String deleteContact(
            @RequestParam Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            // Verify ownership before deletion
            ContactReadOnlyDTO contact = contactService.findById(id);
            if (!contact.username().equals(username)) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/dashboard";
            }

            contactService.deleteContact(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contact deleted successfully");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/add-success")
    public String showAddContactSuccess(Model model) {
        return "add-contact-success";
    }

    @GetMapping("/update-success")
    public String showUpdateContactSuccess(Model model) {
        return "update-contact-success";
    }
}