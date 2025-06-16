package gr.aueb.cf.pharmapp_spring.repository;

import gr.aueb.cf.pharmapp_spring.model.TradeRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRecordRepository extends JpaRepository<TradeRecord,
        Long>, JpaSpecificationExecutor<TradeRecord> {

    List<TradeRecord> findByGiverIdOrReceiverIdOrderByTransactionDateDesc(Long giverId, Long receiverId, Pageable pageable);
    List<TradeRecord> findByGiverIdAndReceiverIdAndTransactionDateBetween(Long giverId, Long receiverId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM TradeRecord t WHERE t.giver.id = " +
            ":giverId AND t.receiver.id = :receiverId")
    Double sumAmountByGiverIdAndReceiverId(@Param("giverId") Long giverId,
                                           @Param("receiverId") Long receiverId);

    long countByGiverIdAndReceiverId(Long giverId, Long receiverId);

    List<TradeRecord> findByGiverIdAndReceiverIdOrderByTransactionDateDesc(
            Long giverId, Long receiverId, Pageable pageable);

}
