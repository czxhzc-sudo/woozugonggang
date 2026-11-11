package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember,Long>{
    List<ChatRoomMember> findByUsername(String username);
    List<ChatRoomMember> findByRoomId(Long roomId);
    Optional<ChatRoomMember> findByRoomIdAndUsername(Long roomId, String username);
}
