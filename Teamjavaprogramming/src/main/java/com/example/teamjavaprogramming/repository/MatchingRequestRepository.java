package com.example.teamjavaprogramming.repository;
import com.example.teamjavaprogramming.MatchingRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface MatchingRequestRepository extends JpaRepository<MatchingRequest,Long> {
    List<MatchingRequest> findByStatus(String status);
    MatchingRequest findByUsernameAndStatus(
            String username,
            String status
    );
    MatchingRequest findTopByUsernameOrderByCreatedAtDesc(
            String username
    );
    // 특정 사용자의 대기 중인 신청을 잠금 상태로 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchingRequest m WHERE m.username = :username AND m.status = :status")
    MatchingRequest findByUsernameAndStatusForUpdate(
            @Param("username") String username,
            @Param("status") String status
    );
    // 특정 상태의 신청 목록을 잠금 상태로 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchingRequest m WHERE m.status = :status ORDER BY m.createdAt ASC")
    List<MatchingRequest> findByStatusForUpdate(
            @Param("status") String status
    );
}
