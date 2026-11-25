
package com.example.teamjavaprogramming;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ActivityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기록을 소유한 회원
    private String username;

    // 중복 방지용 게시글 ID (화면에 표시하지 않음)
    private Long postId;

    // 중복 방지용 채팅방 ID (화면에 표시하지 않음)
    private Long roomId;

    // 가입 당시 게시글 제목
    @Column(columnDefinition = "TEXT")
    private String postTitle;

    // 가입 당시 주요 콘텐츠
    private String mainContent;

    // 가입 당시 세부 콘텐츠
    @Column(columnDefinition = "TEXT")
    private String detailContents;

    // 가입 당시 게시글 본문
    @Column(columnDefinition = "TEXT")
    private String postContent;

    // 실제 채팅방 가입 시간
    private LocalDateTime joinedAt;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getMainContent() {
        return mainContent;
    }

    public void setMainContent(String mainContent) {
        this.mainContent = mainContent;
    }

    public String getDetailContents() {
        return detailContents;
    }

    public void setDetailContents(String detailContents) {
        this.detailContents = detailContents;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
