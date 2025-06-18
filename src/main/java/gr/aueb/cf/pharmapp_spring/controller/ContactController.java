package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.ContactInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.service.PharmacyContactService;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

            contactService.createContact(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Contact with " + pharmacy.name() + " added successfully!");
        } catch (EntityNotFoundException | EntityAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/{id}/delete")
    public String deleteContact(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            contactService.deleteContact(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contact deleted successfully");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }
}
