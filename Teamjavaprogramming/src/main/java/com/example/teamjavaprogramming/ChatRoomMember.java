package com.example.teamjavaprogramming;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 고유번호
    private Long roomId;  // 어떤 채팅방인지
    private String username;  // 어떤 사용자인지
    private LocalDateTime joinedAt;  // 참여 시간
    private LocalDateTime lastReadAt;// 마지막으로 메시지를 읽은 시간

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
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }
    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
