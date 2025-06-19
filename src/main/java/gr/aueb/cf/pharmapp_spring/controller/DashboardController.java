package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.BalanceDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.service.PharmacyContactService;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.TradeRecordService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final PharmacyService pharmacyService;
    private final PharmacyContactService contactService;
    private final TradeRecordService tradeRecordService;

    public DashboardController(UserService userService, PharmacyService pharmacyService,
                               PharmacyContactService contactService, TradeRecordService tradeRecordService) {
        this.userService = userService;
        this.pharmacyService = pharmacyService;
        this.contactService = contactService;
        this.tradeRecordService = tradeRecordService;
    }

    @GetMapping
    public String showDashboard(
            @RequestParam(required = false) Long pharmacyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model,
            Authentication authentication) {

        try {
            UserReadOnlyDTO user = userService.getUserByUsername(authentication.getName());

            model.addAttribute("user", user);

            // Only proceed with pharmacy selection if user has pharmacies
            if (user.getPharmacies() != null && !user.getPharmacies().isEmpty()) {
                // If no pharmacy selected, select first one
                if (pharmacyId == null) {
                    pharmacyId = user.getPharmacies().get(0).id();
                }

                // Verify the user owns the selected pharmacy
                if (pharmacyId != null && pharmacyService.isPharmacyOwnedByUser(pharmacyId, user.getId())) {
                    PharmacyReadOnlyDTO selectedPharmacy = pharmacyService.getById(pharmacyId);
                    model.addAttribute("selectedPharmacy", selectedPharmacy);

                    // Get balance list with optional search and sort
                    List<BalanceDTO> balanceList = pharmacyService.getBalanceList(pharmacyId, sort);
                    model.addAttribute("balanceList", balanceList);
                    model.addAttribute("currentSort", sort);
                } else {
                    // If invalid pharmacy selected, reset to first pharmacy
                    pharmacyId = user.getPharmacies().get(0).id();
                    PharmacyReadOnlyDTO selectedPharmacy = pharmacyService.getById(pharmacyId);
                    model.addAttribute("selectedPharmacy", selectedPharmacy);

                    List<BalanceDTO> balanceList = pharmacyService.getBalanceList(pharmacyId, sort);
                    model.addAttribute("balanceList", balanceList);
                    model.addAttribute("currentSort", sort);
                }
            }
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "User not found");
        }
        return "dashboard";

    }
}
