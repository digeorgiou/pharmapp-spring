package gr.aueb.cf.pharmapp_spring.core.specifications;

import gr.aueb.cf.pharmapp_spring.model.PharmacyContact;
import gr.aueb.cf.pharmapp_spring.model.Pharmacy;
import gr.aueb.cf.pharmapp_spring.model.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class PharmacyContactSpecification {

    public static Specification<PharmacyContact> hasUser(User user) {
        return (root, query, criteriaBuilder) -> {
            if (user == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    public static Specification<PharmacyContact> hasActivePharmacy() {
        return (root, query, criteriaBuilder) -> {
            Join<PharmacyContact, Pharmacy> pharmacyJoin = root.join("pharmacy", JoinType.INNER);
            return criteriaBuilder.isTrue(pharmacyJoin.get("active"));
        };
    }

    public static Specification<PharmacyContact> contactNameContains(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("contactName")),
                    "%" + searchTerm.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PharmacyContact> pharmacyNameContains(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<PharmacyContact, Pharmacy> pharmacyJoin = root.join("pharmacy", JoinType.INNER);
            return criteriaBuilder.like(
                    criteriaBuilder.lower(pharmacyJoin.get("name")),
                    "%" + searchTerm.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PharmacyContact> searchByTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Predicate contactNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("contactName")),
                    "%" + searchTerm.toLowerCase() + "%"
            );

            Join<PharmacyContact, Pharmacy> pharmacyJoin = root.join("pharmacy", JoinType.INNER);
            Predicate pharmacyNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(pharmacyJoin.get("name")),
                    "%" + searchTerm.toLowerCase() + "%"
            );

            return criteriaBuilder.or(contactNamePredicate, pharmacyNamePredicate);
        };
    }

//    public static Specification<PharmacyContact> orderByContactName(boolean ascending) {
//        return (root, query, criteriaBuilder) -> {
//            if (ascending) {
//                query.orderBy(criteriaBuilder.asc(root.get("contactName")));
//            } else {
//                query.orderBy(criteriaBuilder.desc(root.get("contactName")));
//            }
//            return criteriaBuilder.conjunction();
//        };
//    }
//
//    public static Specification<PharmacyContact> orderByPharmacyName(boolean ascending) {
//        return (root, query, criteriaBuilder) -> {
//            Join<PharmacyContact, Pharmacy> pharmacyJoin = root.join("pharmacy", JoinType.INNER);
//            if (ascending) {
//                query.orderBy(criteriaBuilder.asc(pharmacyJoin.get("name")));
//            } else {
//                query.orderBy(criteriaBuilder.desc(pharmacyJoin.get("name")));
//            }
//            return criteriaBuilder.conjunction();
//        };
//    }
}
