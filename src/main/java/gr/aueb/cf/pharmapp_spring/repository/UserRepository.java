package gr.aueb.cf.pharmapp_spring.repository;

import gr.aueb.cf.pharmapp_spring.core.enums.Role;
import gr.aueb.cf.pharmapp_spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRole(Role role);
    Optional<User> findByUsername(String username);
    Long countByRole(Role role);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    default boolean isAdmin(Long userId) {
        return findById(userId)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }

    // For admin views that need to see inactive users
    @Query("SELECT u FROM User u WHERE u.id = ?1")
    Optional<User> findByIdIncludingInactive(Long id);

    // Soft delete count
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = false")
    long countInactiveUsers();

}
