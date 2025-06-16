package gr.aueb.cf.pharmapp_spring.repository;

import gr.aueb.cf.pharmapp_spring.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long>,
        JpaSpecificationExecutor<Pharmacy>{
    //Το JpaSpecificationExecutor μας δινει την δυνατοητα να κανουμε filtering

    //findByRegionId -- ειναι Path. πχ o teacher εχει region και το region
    // εχει id


    //checks only active pharmacies
    boolean existsByName(String name);

    Optional<Pharmacy> findByName(String name);

    List<Pharmacy> findByUserId(Long id);

    List<Pharmacy> findByNameStartingWithIgnoreCase(String name);
    List<Pharmacy> findByUserUsernameStartingWithIgnoreCase(String username);

}
