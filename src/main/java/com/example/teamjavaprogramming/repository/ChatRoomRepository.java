package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    List<ChatRoom> findByIdIn(List<Long> roomIds);
    ChatRoom findByPostId(Long postId);
    // 소유권 변경 시 채팅방을 잠금 상태로 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatRoom c WHERE c.id = :roomId")
    Optional<ChatRoom> findByIdForUpdate(
            @Param("roomId") Long roomId
    );
}
