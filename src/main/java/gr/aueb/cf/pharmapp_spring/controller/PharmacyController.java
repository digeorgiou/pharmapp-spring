package gr.aueb.cf.pharmapp_spring.controller;


import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyUpdateDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.model.User;
import gr.aueb.cf.pharmapp_spring.service.PharmacyContactService;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.TradeRecordService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
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
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final Logger LOGGER =
            LoggerFactory.getLogger(PharmacyController.class);
    private final PharmacyService pharmacyService;
    private final UserService userService;
    private final TradeRecordService recordService;
    private final PharmacyContactService contactService;
    private final Mapper mapper;

    @GetMapping("/add")
    public String showAddPharmacyForm(Model model,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();

            UserReadOnlyDTO user = userService.getUserByUsername(username);
            model.addAttribute("pharmacyInsertDTO", new PharmacyInsertDTO(user.getId(),
                    null));
            model.addAttribute("user", user);
            return "add-pharmacy";
        } catch (EntityNotFoundException e){
            LOGGER.error("User not found while trying to show add pharmacy " +
                    "form", e);
            redirectAttributes.addFlashAttribute("error", "User not found. " +
                    "Log in again");
            return "redirect:/logout";
        }

    }

    @PostMapping("/add")
    public String addPharmacy(
            @Valid @ModelAttribute("pharmacyInsertDTO")  PharmacyInsertDTO insertDTO,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if(bindingResult.hasErrors()){

            String username = authentication.getName();
            try{
                UserReadOnlyDTO user = userService.getUserByUsername(username);
                model.addAttribute("user", user);
                return "add-pharmacy";
            } catch (EntityNotFoundException e) {
                redirectAttributes.addFlashAttribute("error", "User not found. Log in again");
                return "redirect:/logout";
            }
        }

        try{
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            insertDTO = new PharmacyInsertDTO(user.getId(), insertDTO.name());
            PharmacyReadOnlyDTO pharmacy =
                    pharmacyService.createPharmacy(insertDTO);

            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy " + pharmacy.name()
            + " created successfully!");
            redirectAttributes.addFlashAttribute("pharmacy", pharmacy);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/pharmacies/add-success";

        }   catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found. Log in again");
            return "redirect:/logout";

        }   catch (EntityAlreadyExistsException e) {
            bindingResult.rejectValue("name", "pharmacy.exists", e.getMessage());
            String username = authentication.getName();
            try{
                UserReadOnlyDTO user = userService.getUserByUsername(username);
                model.addAttribute("user", user);
                return "add-pharmacy";
            } catch (EntityNotFoundException ex) {
                redirectAttributes.addFlashAttribute("error", "User not found. Log in again");
                return "redirect:/logout";
            }
        }

    }

    @GetMapping("/update-form")
    public String showUpdatePharmacyForm(
            @RequestParam Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);
            PharmacyReadOnlyDTO pharmacy = pharmacyService.getById(id);

            // Verify that the pharmacy belongs to the current user
            if (!pharmacyService.isPharmacyOwnedByUser(id, user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/dashboard";
            }

            PharmacyUpdateDTO updateDTO = new PharmacyUpdateDTO(
                    pharmacy.id(),
                    pharmacy.name(),
                    user.getId()

            );

            model.addAttribute("pharmacyUpdateDTO", updateDTO);
            model.addAttribute("pharmacy", pharmacy);
            model.addAttribute("user", user);

            return "update-pharmacy";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/update")
    public String updatePharmacy(
            @Valid @ModelAttribute("pharmacyUpdateDTO") PharmacyUpdateDTO updateDTO,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            try {
                String username = authentication.getName();
                UserReadOnlyDTO user = userService.getUserByUsername(username);
                PharmacyReadOnlyDTO pharmacy = pharmacyService.getById(updateDTO.id());

                model.addAttribute("pharmacy", pharmacy);
                model.addAttribute("user", user);
                return "update-pharmacy";
            } catch (EntityNotFoundException e) {
                redirectAttributes.addFlashAttribute("error", "Pharmacy not found");
                return "redirect:/dashboard";
            }
        }

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            PharmacyUpdateDTO dto = new PharmacyUpdateDTO(
                    updateDTO.id(),
                    updateDTO.name(),
                    user.getId()
            );

            PharmacyReadOnlyDTO updatedPharmacy = pharmacyService.updatePharmacy(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Pharmacy '" + updatedPharmacy.name() + "' updated successfully!");
            redirectAttributes.addFlashAttribute("pharmacy", updatedPharmacy);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/pharmacies/update-success";
        } catch (EntityNotFoundException | EntityAlreadyExistsException | EntityNotAuthorizedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{id}/delete")
    public String deletePharmacy(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            // Verify ownership before deletion
            if (!pharmacyService.isPharmacyOwnedByUser(id, user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/dashboard";
            }

            pharmacyService.deletePharmacy(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy deleted successfully");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/add-success")
    public String showAddPharmacySuccess(Model model, Authentication authentication) {
        return "add-pharmacy-success";
    }

    @GetMapping("/update-success")
    public String showUpdatePharmacySuccess(Model model, Authentication authentication) {
        return "update-pharmacy-success";
    }
}
