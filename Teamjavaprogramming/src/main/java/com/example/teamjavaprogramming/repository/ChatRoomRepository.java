package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
    List<ChatRoom> findByIdIn(List<Long> roomIds);
    ChatRoom findByPostId(Long postId);
}
