package com.example.teamjavaprogramming.repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.teamjavaprogramming.MemberSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberSet, Long> {
    Optional<MemberSet> findByUserName(String userName);
    boolean existsByUserName(String userName);
    // 사용자 정보를 잠금 상태로 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberSet m WHERE m.userName = :userName")
    Optional<MemberSet> findByUserNameForUpdate(
            @Param("userName") String userName
    );
}
