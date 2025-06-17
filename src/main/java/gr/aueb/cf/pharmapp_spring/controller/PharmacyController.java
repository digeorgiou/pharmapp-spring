package gr.aueb.cf.pharmapp_spring.controller;


import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.service.PharmacyContactService;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.TradeRecordService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String showAddPharmacyForm(Model model) {
        model.addAttribute("name", "");
        return "add-pharmacy";
    }

    @PostMapping("/add")
    public String addPharmacy(
            @RequestParam String name,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        try {
            UserReadOnlyDTO user = userService.getUserByUsername(username);
            PharmacyInsertDTO dto = new PharmacyInsertDTO(user.getId(), name);
            PharmacyReadOnlyDTO pharmacy = pharmacyService.createPharmacy(dto);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Pharmacy " + pharmacy.name() + " created successfully!");
            return "redirect:/dashboard";
        } catch (EntityAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("name", name);
            redirectAttributes.addFlashAttribute("nameMessage", e.getMessage());
            return "redirect:/pharmacies/add";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pharmacies/add";
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
