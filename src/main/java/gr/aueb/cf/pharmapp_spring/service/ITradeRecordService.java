package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.AppServerException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.TradeRecordUpdateDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ITradeRecordService {

    TradeRecordReadOnlyDTO createRecord(TradeRecordInsertDTO dto) throws EntityNotAuthorizedException, EntityNotFoundException;
    TradeRecordReadOnlyDTO updateRecord(TradeRecordUpdateDTO dto)throws EntityNotFoundException, EntityNotAuthorizedException, AppServerException;
    void deleteRecord(Long id, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException;
    TradeRecordReadOnlyDTO restoreTradeRecord(Long recordId,
                                              Long restorerUserId) throws EntityNotFoundException, EntityNotAuthorizedException;
    TradeRecordReadOnlyDTO getById(Long id) throws EntityNotFoundException;
    List<TradeRecordReadOnlyDTO> getAll() throws AppServerException;
    Page<TradeRecordReadOnlyDTO> getAllRecordsPaginated(int page, int size);
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
    Page<TradeRecordReadOnlyDTO> getTradesBetweenPharmaciesPaginated(
            Long pharmacy1Id, Long pharmacy2Id, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size) throws EntityNotFoundException;

    Page<TradeRecordReadOnlyDTO> getTradesForPharmacyPaginated(
            Long pharmacyId, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size
    )throws EntityNotFoundException;

    TradeRecordReadOnlyDTO settleBalance(Long pharmacy1Id, Long pharmacy2Id,
                                         Long userId, String description) throws EntityNotFoundException, EntityNotAuthorizedException, EntityInvalidArgumentException;

}
