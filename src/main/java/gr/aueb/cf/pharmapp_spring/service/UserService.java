package gr.aueb.cf.pharmapp_spring.service;

import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotAuthorizedException;
import gr.aueb.cf.pharmapp_spring.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.pharmapp_spring.dto.*;
import gr.aueb.cf.pharmapp_spring.mapper.Mapper;
import gr.aueb.cf.pharmapp_spring.model.Pharmacy;
import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import gr.aueb.cf.pharmapp_spring.model.User;
import gr.aueb.cf.pharmapp_spring.repository.PharmacyContactRepository;
import gr.aueb.cf.pharmapp_spring.repository.PharmacyRepository;
import gr.aueb.cf.pharmapp_spring.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PharmacyContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;
    private final Mapper mapper;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserReadOnlyDTO createUser(UserInsertDTO dto) throws EntityAlreadyExistsException{

        if(userRepository.existsByEmail(dto.getEmail())){
            throw new EntityAlreadyExistsException("User",
                    "Το email " + dto.getEmail() + " χρησιμοποιείται ήδη");
        }

        if(userRepository.existsByUsername(dto.getUsername())){
            throw new EntityAlreadyExistsException("User",
                    "Το όνομα χρήστη " + dto.getUsername() + " " +
                            "χρησιμοποιείται ήδη");
        }

        User user = mapper.mapUserInsertToModel(dto);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLastUpdater(user);

        User insertedUser = userRepository.save(user);

        LOGGER.info("User with username= {} inserted", dto.getUsername());

        return mapper.mapToUserReadOnlyDTO(insertedUser);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserReadOnlyDTO updateUser(UserUpdateDTO dto) throws EntityNotFoundException, EntityAlreadyExistsException {

        User existingUser = userRepository.findById(dto.getId()).orElseThrow(()-> new EntityNotFoundException("User",
            "User with id " + dto.getId() + " was not found"));

        if(userRepository.existsByEmail(dto.getEmail())){
            throw new EntityAlreadyExistsException("User",
                    "Email " + dto.getEmail() + " already exists");
        }

        if(userRepository.existsByUsername(dto.getUsername())){
            throw new EntityAlreadyExistsException("User",
                    "Username " + dto.getUsername() + " already exists");
        }

        User updatedUser = mapper.mapUserUpdateToModel(dto, existingUser);
        User savedUser = userRepository.save(updatedUser);

        LOGGER.info("User with id={}, username={}, email={}, " +
                        "updated.",
                savedUser.getId(), savedUser.getUsername(),
                savedUser.getEmail());

        return mapper.mapToUserReadOnlyDTO(savedUser);

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteUser(Long userId, Long deleterUserId) throws EntityNotFoundException, EntityNotAuthorizedException {

        User deleterUser = userRepository.findById(deleterUserId)
                .orElseThrow(() -> new EntityNotFoundException("User", "User " +
                        "with id " + deleterUserId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "User " +
                        "with id " + userId + " not found"));


        if(!user.getId().equals(deleterUserId) && ! userRepository.isAdmin(deleterUserId)){
            throw new EntityNotAuthorizedException("User", "User not " +
                    "authorized to delete users");
        }

        user.setIsActive(false);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }



    @Override
    @Transactional
    public UserReadOnlyDTO getUserById(Long id) throws EntityNotFoundException {

        User user = userRepository.findById(id).orElseThrow
                (() -> new EntityNotFoundException("User", "User with id " + id + " not found"));

        return mapper.mapToUserReadOnlyDTO(user);
    }

    @Override
    @Transactional
    public List<UserReadOnlyDTO> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(mapper::mapToUserReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserReadOnlyDTO> getAllUsersPaginated(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(mapper::mapToUserReadOnlyDTO);
    }

    @Override
    @Transactional
    public UserReadOnlyDTO getUserByUsername(String username) throws EntityNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User", "User " +
                        "with username " + username + " not found"));

        return mapper.mapToUserReadOnlyDTO(user);
    }

    @Override
    @Transactional
    public List<PharmacyReadOnlyDTO> getUserPharmacies(Long userId) throws EntityNotFoundException {
        User user = userRepository.findById(userId).orElseThrow
                (() -> new EntityNotFoundException("User",
                        "User with id " + userId + " not found"));

        List<Pharmacy> pharmacies = pharmacyRepository.findByUserId(userId);

        return pharmacies.stream()
                .map(mapper::mapToPharmacyReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PharmacyReadOnlyDTO> getUserPharmaciesPaginated(Long userId, int page, int size) throws EntityNotFoundException{

        User user = userRepository.findById(userId).orElseThrow
                (() -> new EntityNotFoundException("User",
                        "User with id " + userId + " not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Pharmacy> pharmacyPage = pharmacyRepository.findByUserId(userId,
                pageable);

        return pharmacyPage.map(mapper::mapToPharmacyReadOnlyDTO);
    }

    @Override
    @Transactional
    public List<ContactReadOnlyDTO> getUserContacts(Long userId) throws EntityNotFoundException {

        User user = userRepository.findById(userId).orElseThrow
                (() -> new EntityNotFoundException("User",
                        "User with id " + userId + " not found"));

        List<PharmacyContact> contacts = contactRepository.findByUserId(userId);

        return contacts.stream()
                .map(mapper::mapToPharmacyContactReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ContactReadOnlyDTO> getUserContactsPaginated(Long userId,
                                                             int page, int size) throws EntityNotFoundException {

        User user = userRepository.findById(userId).orElseThrow
                (() -> new EntityNotFoundException("User",
                        "User with id " + userId + " not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<PharmacyContact> contactPage =
                contactRepository.findByUserId(userId,
                pageable);

        return contactPage.map(mapper::mapToPharmacyContactReadOnlyDTO);
    }

    @Override
    @Transactional
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
