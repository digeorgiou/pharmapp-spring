package gr.aueb.cf.pharmapp_spring.dto;

import java.util.List;

public record BalanceDTO (

        String contactName,
        String pharmacyName,
        Long ownerPharmacyId,
        Long contactPharmacyId,
        double balance,
        List<TradeRecordReadOnlyDTO> recentTrades,
        Integer tradeCount,
        Boolean isActive
)
{}
