package com.example.teamjavaprogramming;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MatchingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username; // 현재 매칭을 신청한 사용자
    private String targetGender;// 찾는 상대의 성별
    private String targetMajor; // 찾는 상대의 전공
    private Integer minStudentYear; // 찾는 상대의 학번 최소/최대
    private Integer maxStudentYear;
    private String targetContents; // 찾는 상대의 콘텐츠
    private String status; // WAITING / MATCHED / CANCELLED
    private LocalDateTime createdAt; // 매칭 시작 시간
    private Long chatRoomId;  //방 아이디

    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getTargetGender() {
        return targetGender;
    }
    public void setTargetGender(String targetGender) {
        this.targetGender = targetGender;
    }
    public String getTargetMajor() {
        return targetMajor;
    }
    public void setTargetMajor(String targetMajor) {
        this.targetMajor = targetMajor;
    }
    public Integer getMinStudentYear() {
        return minStudentYear;
    }
    public void setMinStudentYear(Integer minStudentYear) {
        this.minStudentYear = minStudentYear;
    }
    public Integer getMaxStudentYear() {
        return maxStudentYear;
    }
    public void setMaxStudentYear(Integer maxStudentYear) {
        this.maxStudentYear = maxStudentYear;
    }
    public String getTargetContents() {
        return targetContents;
    }
    public void setTargetContents(String targetContents) {
        this.targetContents = targetContents;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getChatRoomId() {
        return chatRoomId;
    }
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}