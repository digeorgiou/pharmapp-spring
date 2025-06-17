package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.UserReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.TradeRecordService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/trades")
public class TradeController {

    private final TradeRecordService tradeRecordService;
    private final PharmacyService pharmacyService;
    private final UserService userService;

    public TradeController(TradeRecordService tradeRecordService,
                           PharmacyService pharmacyService,
                           UserService userService) {
        this.tradeRecordService = tradeRecordService;
        this.pharmacyService = pharmacyService;
        this.userService = userService;
    }

    @GetMapping("/record")
    public String showRecordTradeForm(
            @RequestParam Long giverId,
            @RequestParam Long receiverId,
            Authentication authentication,
            Model model) {

        try {
            PharmacyReadOnlyDTO giverPharmacy = pharmacyService.getById(giverId);
            PharmacyReadOnlyDTO receiverPharmacy = pharmacyService.getById(receiverId);

            // Get contact names if available
            String fromContactName = "Your Pharmacy"; // Default
            String contactName = receiverPharmacy.name(); // Default

            model.addAttribute("giverPharmacy", giverPharmacy);
            model.addAttribute("receiverPharmacy", receiverPharmacy);
            model.addAttribute("fromContactName", fromContactName);
            model.addAttribute("contactName", contactName);

            return "record-trade";
        } catch (EntityNotFoundException e) {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/record")
    public String recordTrade(
            @RequestParam Long giverId,
            @RequestParam Long receiverId,
            @RequestParam Double amount,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime transactionDate,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        try {
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            TradeRecordInsertDTO dto = new TradeRecordInsertDTO(
                    description,
                    amount,
                    transactionDate,
                    giverId,
                    receiverId,
                    user.getId()
            );

            tradeRecordService.createRecord(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Trade recorded successfully");
        } catch (EntityNotFoundException | EntityNotAuthorizedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard?pharmacyId=" + giverId;
    }

    @GetMapping("/view")
    public String viewTrades(
            @RequestParam Long pharmacy1,
            @RequestParam Long pharmacy2,
            Model model) {

        try {
            List<TradeRecordReadOnlyDTO> trades = tradeRecordService.getTradesBetweenPharmacies(
                    pharmacy1, pharmacy2, null, null);

            model.addAttribute("trades", trades);
            return "view-trades";
        } catch (EntityNotFoundException e) {
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/settle")
    public String settleBalance(
            @RequestParam Long pharmacy1,
            @RequestParam Long pharmacy2,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        try {
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            tradeRecordService.settleBalance(pharmacy1, pharmacy2, user.getId(), "Balance settlement");
            redirectAttributes.addFlashAttribute("successMessage", "Balance settled successfully");
        } catch (EntityNotFoundException | EntityNotAuthorizedException |
                 EntityInvalidArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard?pharmacyId=" + pharmacy1;
    }
}
