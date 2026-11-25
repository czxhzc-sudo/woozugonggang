
package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.ChatRoomBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomBanRepository extends JpaRepository<ChatRoomBan, Long> {

    // 특정 채팅방에서 특정 회원이 추방되었는지 확인
    boolean existsByRoomIdAndUsername(Long roomId, String username);

    // 특정 채팅방의 특정 회원 추방 기록 조회
    Optional<ChatRoomBan> findByRoomIdAndUsername(Long roomId, String username);

    // 특정 채팅방의 추방 회원 목록 조회
    List<ChatRoomBan> findByRoomId(Long roomId);

    // 특정 회원의 추방 기록 삭제 (추방 해제)
    void deleteByRoomIdAndUsername(Long roomId, String username);

    // 특정 채팅방의 모든 추방 기록 삭제
    void deleteByRoomId(Long roomId);
    // 특정 회원이 추방된 모든 채팅방 기록 조회
    List<ChatRoomBan> findByUsername(String username);
}
