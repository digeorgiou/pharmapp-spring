package gr.aueb.cf.pharmapp_spring.repository;

import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PharmacyContactRepository extends JpaRepository<PharmacyContact, Long>,
        JpaSpecificationExecutor<PharmacyContact> {

    boolean existsByUserIdAndPharmacyId(Long userId, Long pharmacyId);

    List<PharmacyContact> findByUserId(Long userId);
}
