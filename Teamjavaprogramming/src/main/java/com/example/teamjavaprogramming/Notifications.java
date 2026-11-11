package com.example.teamjavaprogramming;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //알림 고유 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberSet user;  // 사용자 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post; // 계시글 고유 번호

    private String type; // 알림 유형
    private String title_snapshot; // 제목 사본
    private LocalDateTime created_at = LocalDateTime.now(); // 알림 생성시각
    private LocalDateTime read_at; // 읽은 시각
    private String content;    // 알림 상세 내용
    private Long related_id; // 관련 데이터의 고유번호 알림 클릭시 페이지 이동 용

    public Long getId(){
        return id;
    }
    public MemberSet getUser(){
        return user;
    }
    public void setUser(MemberSet user){
        this.user = user;
    }
    public Post getPost(){
        return post;
    }
    public void setPost(Post post){
        this.post = post;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getTitle_snapshot(){
        return title_snapshot;
    }
    public void setTitle_snapshot(String title_snapshot){
        this.title_snapshot = title_snapshot;
    }
    public LocalDateTime getCreated_at(){
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at){
        this.created_at = created_at;
    }
    public LocalDateTime getRead_at(){
        return read_at;
    }
    public void setRead_at(LocalDateTime read_at){
        this.read_at = read_at;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Long getRelated_id() {
        return related_id;
    }
    public void setRelated_id(Long related_id) {
        this.related_id = related_id;
    }
}
