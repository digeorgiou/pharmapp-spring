package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.*;

import java.util.List;

public interface IUserService {

    UserReadOnlyDTO createUser(UserInsertDTO dto) throws EntityAlreadyExistsException;
    UserReadOnlyDTO updateUser(UserUpdateDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException;;
    void deleteUser(Long userIdToDelete, Long loggedInUserId) throws EntityNotAuthorizedException, EntityNotFoundException;
    UserReadOnlyDTO getUserById(Long id) throws EntityNotFoundException;
    List<UserReadOnlyDTO> getAllUsers();
    UserReadOnlyDTO getUserByUsername(String username) throws EntityNotFoundException;
    List<PharmacyReadOnlyDTO> getUserPharmacies(Long userId) throws EntityNotFoundException;
    List<ContactReadOnlyDTO> getUserContacts(Long userId) throws EntityNotFoundException;
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
