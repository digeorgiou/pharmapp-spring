package gr.aueb.cf.pharmapp_spring.repository;

import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import gr.aueb.cf.pharmapp_spring.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyContactRepository extends JpaRepository<PharmacyContact, Long>,
        JpaSpecificationExecutor<PharmacyContact> {

    boolean existsByUserIdAndPharmacyId(Long userId, Long pharmacyId);
    boolean existsByUserIdAndContactName(Long userId, String contactName);

    List<PharmacyContact> findByUserId(Long userId);

    Page<PharmacyContact> findByUser(User user, Pageable pageable);
    Page<PharmacyContact> findByUserId(Long userId, Pageable pageable);

    Optional<PharmacyContact> findByUserIdAndPharmacyId(Long userId, Long pharmacyId);

}
