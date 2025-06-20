package gr.aueb.cf.pharmapp_spring.controller;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.*;
import gr.aueb.cf.pharmapp_spring.service.PharmacyContactService;
import gr.aueb.cf.pharmapp_spring.service.PharmacyService;
import gr.aueb.cf.pharmapp_spring.service.TradeRecordService;
import gr.aueb.cf.pharmapp_spring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/trades")
public class TradeController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final TradeRecordService tradeRecordService;
    private final PharmacyService pharmacyService;
    private final UserService userService;
    private final PharmacyContactService contactService;

    public TradeController(TradeRecordService tradeRecordService,
                           PharmacyService pharmacyService,
                           UserService userService, PharmacyContactService contactService) {
        this.tradeRecordService = tradeRecordService;
        this.pharmacyService = pharmacyService;
        this.userService = userService;
        this.contactService = contactService;
    }

    @GetMapping("/record")
    public String showRecordTradeForm(
            @RequestParam Long giverId,
            @RequestParam Long receiverId,
            Authentication authentication,
            Model model) {

        try {
            UserReadOnlyDTO user =
                    userService.getUserByUsername(authentication.getName());

            PharmacyReadOnlyDTO giverPharmacy = pharmacyService.getById(giverId);
            PharmacyReadOnlyDTO receiverPharmacy = pharmacyService.getById(receiverId);

            String contactName;
            try {
                // Try to get the contact name if this is a contact
                contactName = contactService.getContactName(user.getId(), receiverId);
            } catch (EntityNotFoundException e) {
                // Fall back to pharmacy name if no contact exists
                contactName = receiverPharmacy.name();
            }

            // Get contact names if available
            String fromContactName;
            try {
                // Try to get the contact name if this is a contact
                fromContactName = contactService.getContactName(user.getId(),
                        giverId);
            } catch (EntityNotFoundException e) {
                // Fall back to pharmacy name if no contact exists
                fromContactName = giverPharmacy.name();
            }
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
            PharmacyReadOnlyDTO giverPharmacy = pharmacyService.getById(giverId);
            PharmacyReadOnlyDTO receiverPharmacy = pharmacyService.getById(receiverId);

            boolean isGiverOwner =
                    pharmacyService.isPharmacyOwnedByUser(giverId, user.getId());

            String contactName;
            try {
                // Try to get the contact name if this is a contact
                contactName = contactService.getContactName(user.getId(), receiverId);
            } catch (EntityNotFoundException e) {
                // Fall back to pharmacy name if no contact exists
                contactName = receiverPharmacy.name();
            }

            // Get contact names if available
            String fromContactName;
            try {
                // Try to get the contact name if this is a contact
                fromContactName = contactService.getContactName(user.getId(),
                        giverId);
            } catch (EntityNotFoundException e) {
                // Fall back to pharmacy name if no contact exists
                fromContactName = giverPharmacy.name();
            }



            TradeRecordInsertDTO dto = new TradeRecordInsertDTO(
                    description,
                    amount,
                    transactionDate,
                    giverId,
                    receiverId,
                    user.getId()
            );

            TradeRecordReadOnlyDTO trade = tradeRecordService.createRecord(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Trade recorded successfully");
            redirectAttributes.addFlashAttribute("trade", trade);
            redirectAttributes.addFlashAttribute("fromContactName", fromContactName);
            redirectAttributes.addFlashAttribute("contactName", contactName);
            redirectAttributes.addFlashAttribute("giverPharmacyId", giverId);
            redirectAttributes.addFlashAttribute("receiverPharmacyId",
                    receiverId);


            Long redirectPharmacyId = isGiverOwner ? giverId : receiverId;

            return "redirect:/trades/record-success";
        } catch (EntityNotFoundException | EntityNotAuthorizedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/view")
    public String viewTrades(
            @RequestParam(required = false) Long pharmacy1,
            @RequestParam(required = false) Long pharmacy2,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        try {
            UserReadOnlyDTO user = userService.getUserByUsername(authentication.getName());
            model.addAttribute("user", user);

            // Get user's contacts for the dropdown
            List<ContactReadOnlyDTO> contacts =
                    userService.getUserContacts(user.getId());
            model.addAttribute("contacts", contacts);

            if (pharmacy1 != null) {
                // Get pharmacy1 details
                PharmacyReadOnlyDTO pharmacy1DTO = pharmacyService.getById(pharmacy1);
                model.addAttribute("pharmacy1", pharmacy1);
                model.addAttribute("pharmacy1Name", pharmacy1DTO.name());

                // Convert LocalDate to LocalDateTime (let service handle nulls)
                LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime endDateTime = endDate != null ?
                        endDate.atTime(23, 59, 59) : null;

                if (pharmacy2 != null) {
                    // Get trades between two specific pharmacies
                    PharmacyReadOnlyDTO pharmacy2DTO = pharmacyService.getById(pharmacy2);
                    model.addAttribute("pharmacy2", pharmacy2);
                    model.addAttribute("pharmacy2Name", pharmacy2DTO.name());

                    Page<TradeRecordReadOnlyDTO> tradesPage = tradeRecordService.getTradesBetweenPharmaciesPaginated(
                            pharmacy1, pharmacy2, startDateTime, endDateTime, page, size);

                    model.addAttribute("trades", tradesPage.getContent());
                    model.addAttribute("currentPage", tradesPage.getNumber());
                    model.addAttribute("totalPages", tradesPage.getTotalPages());
                    model.addAttribute("pageSize", size);

                    Integer tradeCount = tradeRecordService.getTradeCountBetweenPharmacies(pharmacy1, pharmacy2);
                    Double balance = tradeRecordService.calculateBalanceBetweenPharmacies(pharmacy1, pharmacy2);
                    model.addAttribute("tradeCount", tradeCount);
                    model.addAttribute("balance", balance);
                } else {
                    Page<TradeRecordReadOnlyDTO> tradesPage = tradeRecordService.getTradesForPharmacyPaginated(
                            pharmacy1, startDateTime, endDateTime, page, size);

                    model.addAttribute("trades", tradesPage.getContent());
                    model.addAttribute("currentPage", tradesPage.getNumber());
                    model.addAttribute("totalPages", tradesPage.getTotalPages());
                    model.addAttribute("totalItems", tradesPage.getTotalElements());
                    model.addAttribute("pageSize", size);
                }
            }

            // Add date filters to model
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "view-trades";
        } catch (EntityNotFoundException e) {
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/settle")
    public String settleBalance(
            @RequestParam Long pharmacy1,
            @RequestParam Long pharmacy2,
            @RequestParam(required = false) String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        try {
            UserReadOnlyDTO user = userService.getUserByUsername(username);

            // Get current balance before settling
            double balance = tradeRecordService.calculateBalanceBetweenPharmacies(pharmacy1, pharmacy2);

            // Perform the settlement
            TradeRecordReadOnlyDTO settlement = tradeRecordService.settleBalance(
                    pharmacy1, pharmacy2, user.getId(),
                    description != null ? description : "Balance settlement");

            // Determine the message based on the original balance
            String message;
            if (balance > 0) {
                message = String.format("Settled balance of €%.2f successfully", balance);
            } else {
                message = String.format("Settled balance of €%.2f successfully", -balance);
            }

            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "One of the pharmacies was not found");
        } catch (EntityNotAuthorizedException e) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to settle this balance");
        } catch (EntityInvalidArgumentException e) {
            redirectAttributes.addFlashAttribute("info", "No balance to settle: " + e.getMessage());
        }

        return "redirect:/trades/view?pharmacy1=" + pharmacy1 + "&pharmacy2=" + pharmacy2;
    }

    @GetMapping("/record-success")
    public String showTradeRecordSuccess(){
        return "record-trade-success";
    }

    @PostMapping("/delete/{id}")
    public String deleteTrade(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            UserReadOnlyDTO user = userService.getUserByUsername(authentication.getName());
            tradeRecordService.deleteRecord(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Trade marked for deletion successfully");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Trade not found");
        } catch (EntityNotAuthorizedException e) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this trade");
        }

        return "redirect:/trades/view";
    }

    @PostMapping("/restore/{id}")
    public String restoreTrade(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            UserReadOnlyDTO user = userService.getUserByUsername(authentication.getName());
            tradeRecordService.restoreTradeRecord(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Trade restored successfully");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Trade not found");
        } catch (EntityNotAuthorizedException e) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to restore this trade");
        }

        return "redirect:/trades/view";
    }
}