package gr.aueb.cf.pharmapp_spring.repository;

import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import gr.aueb.cf.pharmapp_spring.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PharmacyContactRepository extends JpaRepository<PharmacyContact, Long>,
        JpaSpecificationExecutor<PharmacyContact> {

    boolean existsByUserIdAndPharmacyId(Long userId, Long pharmacyId);

    List<PharmacyContact> findByUserId(Long userId);

    Page<PharmacyContact> findByUser(User user, Pageable pageable);
    Page<PharmacyContact> findByUserId(Long userId, Pageable pageable);
}
