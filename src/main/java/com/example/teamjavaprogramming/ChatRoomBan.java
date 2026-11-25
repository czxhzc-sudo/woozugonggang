
package com.example.teamjavaprogramming;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatRoomBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 추방된 채팅방 번호
    private Long roomId;
    // 추방된 회원 아이디
    private String username;
    // 추방된 시간
    private LocalDateTime bannedAt;
    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getBannedAt() {
        return bannedAt;
    }

    public void setBannedAt(LocalDateTime bannedAt) {
        this.bannedAt = bannedAt;
    }
}
