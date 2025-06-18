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

            List<PharmacyReadOnlyDTO> sortedPharmacies = user.getPharmacies().stream()
                            .sorted(Comparator.comparing(PharmacyReadOnlyDTO::name))
                                    .collect(Collectors.toList());

            user.setPharmacies(sortedPharmacies);

            model.addAttribute("user", user);

            // If no pharmacy selected and user has pharmacies, select first one
            if (pharmacyId == null && !user.getPharmacies().isEmpty()) {
                pharmacyId = user.getPharmacies().get(0).id();
            }


            // If a pharmacy is selected, verify the user owns it
            if (pharmacyId != null) {
                if (!pharmacyService.isPharmacyOwnedByUser(pharmacyId, user.getId())) {
                    // If user doesn't own the pharmacy, reset to their first pharmacy
                    if (!user.getPharmacies().isEmpty()) {
                        pharmacyId = user.getPharmacies().get(0).id();
                    } else {
                        pharmacyId = null;
                    }
                }
            }


            if (pharmacyId != null) {

                PharmacyReadOnlyDTO selectedPharmacy = pharmacyService.getById(pharmacyId);
                model.addAttribute("selectedPharmacy", selectedPharmacy);

                // Get balance list with optional search and sort
                List<BalanceDTO> balanceList = pharmacyService.getBalanceList(pharmacyId, sort);


                model.addAttribute("balanceList", balanceList);
                model.addAttribute("currentSort", sort);

            }

        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "User not found");
        }


        return "dashboard";

    }
}
