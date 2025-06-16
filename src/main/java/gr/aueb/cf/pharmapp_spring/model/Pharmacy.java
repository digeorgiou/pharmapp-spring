package gr.aueb.cf.pharmapp_spring.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "pharmacies")
@SQLDelete(sql = "UPDATE pharmacies SET is_active = false WHERE id = ?") //
// Soft delete
@FilterDef(name = "activePharmacyFilter",
        parameters = @ParamDef(name = "isActive", type = Boolean.class))
@Filter(name = "activePharmacyFilter", condition = "is_active = :isActive")
public class Pharmacy extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255, unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" )
    private User user;

    @OneToMany(mappedBy = "giver")
    @Getter(AccessLevel.PRIVATE)
    private Set<TradeRecord> recordsGiver = new HashSet<>();

    @OneToMany(mappedBy = "receiver")
    @Getter(AccessLevel.PRIVATE)
    private Set<TradeRecord> recordsReceiver = new HashSet<>();

    @OneToMany(mappedBy = "pharmacy")
    @Getter(AccessLevel.PRIVATE)
    private Set<PharmacyContact> contactReferences = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Set<TradeRecord> getAllRecordsGiver(){
        if(recordsGiver == null) recordsGiver = new HashSet<>();
        return Collections.unmodifiableSet(recordsGiver);
    }

    public Set<TradeRecord> getAllRecordsReceiver(){
        if(recordsReceiver == null) recordsReceiver = new HashSet<>();
        return Collections.unmodifiableSet(recordsReceiver);
    }

    public Set<PharmacyContact> getAllContactReferences(){
        if(contactReferences == null) contactReferences = new HashSet<>();
        return Collections.unmodifiableSet(contactReferences);
    }


    public void addRecordGiver(TradeRecord tradeRecord){
        if (recordsGiver == null) recordsGiver = new HashSet<>();
        recordsGiver.add(tradeRecord);
        tradeRecord.setGiver(this);
    }

    public void removeRecordGiver(TradeRecord tradeRecord){
        if (recordsGiver == null) return;
        recordsGiver.remove(tradeRecord);
        tradeRecord.setGiver(null);
    }

    public void addRecordReceiver(TradeRecord tradeRecord){
        if (recordsReceiver == null) recordsReceiver = new HashSet<>();
        recordsReceiver.add(tradeRecord);
        tradeRecord.setReceiver(this);
    }

    public void removeRecordReceiver(TradeRecord tradeRecord){
        if (recordsReceiver == null) return;
        recordsReceiver.remove(tradeRecord);
        tradeRecord.setReceiver(null);
    }

    public void addContactReference(PharmacyContact contact){
        if (contactReferences == null) contactReferences = new HashSet<>();
        contactReferences.add(contact);
        contact.setPharmacy(this);
    }

    public void removeContactReference(PharmacyContact contact){
        if (contactReferences == null) return;
        contactReferences.remove(contact);
        contact.setPharmacy(null);
    }
}
