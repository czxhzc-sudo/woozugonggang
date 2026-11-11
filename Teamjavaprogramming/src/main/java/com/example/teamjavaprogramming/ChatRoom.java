package com.example.teamjavaprogramming;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //고유번호
    private String roomName; // 방이름
    private LocalDateTime createdAt; //생성시간
    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;// 어떤 게시글에 연결 됬는지?

    public Long getId(){
        return id;
    }
    public String getRoomName(){
        return roomName;
    }
    public void setRoomName(String roomName){
        this.roomName = roomName;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public Post getPost() {
        return post;
    }
    public void setPost(Post post) {
        this.post = post;
    }
}
