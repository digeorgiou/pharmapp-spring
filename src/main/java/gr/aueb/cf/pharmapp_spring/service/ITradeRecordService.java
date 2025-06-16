package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.AppServerException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ITradeRecordService {

    TradeRecordReadOnlyDTO createRecord(TradeRecordInsertDTO dto) throws EntityNotAuthorizedException, EntityNotFoundException, AppServerException;
    TradeRecordReadOnlyDTO updateRecord(TradeRecordUpdateDTO dto)throws EntityNotFoundException, EntityNotAuthorizedException, AppServerException;
    void deleteRecord(Long id, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException;
    TradeRecordReadOnlyDTO getById(Long id) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getAll() throws AppServerException;
    List<TradeRecordReadOnlyDTO> getRecentTradesForPharmacy(Long pharmacyId,
                                                            int limit) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getTradesBetweenPharmacies(
            Long pharmacy1Id, Long pharmacy2Id, LocalDateTime startDate,
            LocalDateTime endDate) throws EntityNotFoundException;
    Double calculateBalanceBetweenPharmacies(Long pharmacy1Id,
                                             Long pharmacy2Id) throws EntityNotFoundException;
    Integer getTradeCountBetweenPharmacies(Long pharmacy1Id, Long pharmacy2Id) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getRecentTradesBetweenPharmacies(Long pharmacy1Id,
                                                                  Long pharmacy2Id, int limit) throws EntityNotFoundException;

}
