package gr.aueb.cf.pharmapp_spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // αφου εχουμε γενικα
// ενεργοποιησει το auditing στο PharmApplication, το ενεργοποιουμε και εδω
//ειδικα για την συγκεκριμενη κλαση
public class AbstractEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    //Creation Timestamp
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    //Update Timestamp
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private User lastUpdater;

    @Column(unique = true, updatable = false, nullable = false, length = 36)
    private String uuid;

    @PrePersist
    public void initializeUUID(){
        if(uuid == null) uuid = UUID.randomUUID().toString();
    }
}
