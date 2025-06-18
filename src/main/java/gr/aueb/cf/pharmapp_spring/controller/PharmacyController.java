package gr.aueb.cf.pharmapp_spring.controller;


import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
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
            return "add-pharmacy";
        }

        try{
            String username = authentication.getName();
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            insertDTO = new PharmacyInsertDTO(user.getId(), insertDTO.name());
            PharmacyReadOnlyDTO pharmacy =
                    pharmacyService.createPharmacy(insertDTO);

            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy " + pharmacy.name()
            + " created successfully!");
            return "redirect:/dashboard";

        }   catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "User not found. Log in again");
            return "redirect:/logout";

        }   catch (EntityAlreadyExistsException e) {
            bindingResult.rejectValue("name", "pharmacy.exists", e.getMessage());
            return "add-pharmacy";
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

            pharmacyService.deletePharmacy(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy deleted successfully");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

}
