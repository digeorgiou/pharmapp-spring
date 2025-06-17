package gr.aueb.cf.pharmapp_spring.model;

import gr.aueb.cf.pharmapp_spring.core.enums.Role;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE id = ?")
// Soft delete
@FilterDef(name = "activeUserFilter",
        parameters = @ParamDef(name = "isActive", type = Boolean.class))
@Filter(name = "activeUserFilter", condition = "is_active = :isActive")
public class User extends AbstractEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    private String password;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "email", nullable = false)
    private String email;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
       return this.getIsActive() == null || this.getIsActive();
    }

    @OneToMany(mappedBy = "user")
    @Getter(AccessLevel.PRIVATE)
    private Set<Pharmacy> pharmacies = new HashSet<>();

    @OneToMany(mappedBy = "recorder")
    @Getter(AccessLevel.PRIVATE)
    private Set<TradeRecord> recordsRecorder;

    @OneToMany(mappedBy = "user")
    @Getter(AccessLevel.PRIVATE)
    private Set<PharmacyContact> contacts = new HashSet<>();

    public Set<Pharmacy> getAllPharmacies(){
        if(pharmacies == null) pharmacies = new HashSet<>();
        return Collections.unmodifiableSet(pharmacies);
    }

    public Set<TradeRecord> getAllRecordsRecorder(){
        if(recordsRecorder == null) recordsRecorder = new HashSet<>();
        return Collections.unmodifiableSet(recordsRecorder);
    }

    public Set<PharmacyContact> getAllContacts(){
        if(contacts == null) contacts = new HashSet<>();
        return Collections.unmodifiableSet(contacts);
    }

    public void addRecordRecorder(TradeRecord tradeRecord){
        if (recordsRecorder == null) recordsRecorder = new HashSet<>();
        recordsRecorder.add(tradeRecord);
        tradeRecord.setRecorder(this);
    }

    public void removeRecordRecorder(TradeRecord tradeRecord){
        if (recordsRecorder == null) return;
        recordsRecorder.remove(tradeRecord);
        tradeRecord.setRecorder(null);
    }

    public void addPharmacy(Pharmacy pharmacy) {
        if(pharmacies == null) pharmacies = new HashSet<>();
        pharmacies.add(pharmacy);
        pharmacy.setUser(this);
    }

    public void removePharmacy(Pharmacy pharmacy) {
        if(pharmacies == null) return;
        pharmacies.remove(pharmacy);
        pharmacy.setUser(null);
    }

    public void addContact(PharmacyContact contact){
        if (contacts == null) contacts = new HashSet<>();
        contacts.add(contact);
        contact.setUser(this);
    }

    public void removeContact(PharmacyContact contact){
        if (contacts == null) return;
        contacts.remove(contact);
        contact.setUser(null);
    }
}
