package com.example.teamjavaprogramming;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MemberSet user;  // 친구 요청을 보낸 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private MemberSet friend_user;    // 친구 요청을 받은 회원
    private LocalDateTime created_at; // 친구추가 시각
    private Integer friend_check; // 친구추가 되었는지 확인.
    public Long getId(){
        return id;
    }
    public MemberSet getUser(){
        return user;
    }
    public void setUser(MemberSet user){
        this.user = user;
    }
    public MemberSet getFriend_user(){
        return friend_user;
    }
    public void setFriend_user(MemberSet friend_user){
        this.friend_user = friend_user;
    }
    public LocalDateTime getCreated_at(){
        return created_at;
    }
    public void setCreated_at(LocalDateTime created_at){
        this.created_at = created_at;
    }
    public Integer getFriend_check(){
        return friend_check;
    }
    public void setFriend_check(Integer friend_check){
        this.friend_check = friend_check;
    }
}
