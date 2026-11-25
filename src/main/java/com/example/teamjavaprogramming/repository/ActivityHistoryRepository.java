
package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.ActivityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface ActivityHistoryRepository
        extends JpaRepository<ActivityHistory, Long> {

    // 같은 게시글의 활동 기록이 이미 있는지 확인
    boolean existsByUsernameAndPostId(String username, Long postId);

    // 같은 채팅방의 활동 기록이 이미 있는지 확인
    boolean existsByUsernameAndRoomId(String username, Long roomId);

    // 회원의 활동 기록을 가입 시간 최신순으로 조회
    List<ActivityHistory> findByUsernameOrderByJoinedAtDesc(String username);

    // 본인이 소유한 특정 활동 기록 조회
    Optional<ActivityHistory> findByIdAndUsername(Long id, String username);

    // 본인이 소유한 특정 활동 기록 삭제
    void deleteByIdAndUsername(Long id, String username);

    // 본인의 활동 기록 전체 삭제
    @Transactional
    void deleteByUsername(String username);
}
