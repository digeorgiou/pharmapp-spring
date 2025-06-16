package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.AppServerException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.BalanceDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyInsertDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyReadOnlyDTO;
import gr.aueb.cf.pharmapp_spring.dto.PharmacyUpdateDTO;

import java.util.List;
import java.util.Map;

public interface IPharmacyService {

    PharmacyReadOnlyDTO createPharmacy(PharmacyInsertDTO dto) throws EntityAlreadyExistsException, EntityNotFoundException, AppServerException;
    PharmacyReadOnlyDTO updatePharmacy(PharmacyUpdateDTO dto) throws EntityAlreadyExistsException, EntityNotAuthorizedException,
            EntityNotFoundException, AppServerException;
    void deletePharmacy(Long id, Long deleterUserId) throws EntityNotFoundException;
    boolean nameExists(String name) throws AppServerException;
    PharmacyReadOnlyDTO getById(Long id) throws EntityNotFoundException;
    PharmacyReadOnlyDTO getPharmacyByName(String name) throws EntityNotFoundException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByName(String name) throws AppServerException;
    List<PharmacyReadOnlyDTO> searchPharmaciesByUser(String username) throws AppServerException;
    List<PharmacyReadOnlyDTO> getAllPharmacies() throws AppServerException;
    List<BalanceDTO> getBalanceList(Long pharmacyId, String sortBy) throws EntityNotFoundException;
}
