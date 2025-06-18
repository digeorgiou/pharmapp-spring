package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.AppServerException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.BalanceDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IPharmacyService {

    PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto) throws EntityAlreadyExistsException, EntityNotFoundException;
    PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto) throws EntityAlreadyExistsException, EntityNotAuthorizedException,
            EntityNotFoundException;
    void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException;
    boolean nameExists(String name) throws AppServerException;
    PharmacyReadOnlyDTO getById(Long id) throws EntityNotFoundException;
    PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByName(String name);
    Page<PharmacyReadOnlyDTO> searchPharmaciesByNamePaginated(String name,
                                                              int page,
                                                              int size);
    List<PharmacyReadOnlyDTO> searchPharmaciesByUser(String username);
    Page<PharmacyReadOnlyDTO> searchPharmaciesByUserPaginated(String username
            , int page, int size);
    List<PharmacyReadOnlyDTO> getAllPharmacies() throws AppServerException;
    Page<PharmacyReadOnlyDTO> getAllPharmaciesPaginated(int page, int size);
    List<BalanceDTO> getBalanceList(Long pharmacyId, String sortBy) throws EntityNotFoundException;
    Page<BalanceDTO> getBalanceListPaginated(Long pharmacyId, String sortBy,
                                             int page, int size) throws EntityNotFoundException;
    boolean isPharmacyOwnedByUser(Long pharmacyId, Long userId) throws EntityNotFoundException;
}
