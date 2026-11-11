package com.example.teamjavaprogramming.repository;
import com.example.teamjavaprogramming.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long>{
    // 채팅방 메시지 전체 조회
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
    // 특정 회원이 보낸 메시지를 제외하고 안 읽은 메시지 개수 조회
    long countByRoomIdAndSentAtAfterAndUsernameNot(
            Long roomId,
            LocalDateTime lastReadAt,
            String username
    );
}
