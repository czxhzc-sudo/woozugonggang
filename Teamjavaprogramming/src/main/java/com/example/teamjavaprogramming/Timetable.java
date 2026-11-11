package com.example.teamjavaprogramming;

import jakarta.persistence.*;

@Entity
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 시간표 테이블의 고유번호
    @ManyToOne
    @JoinColumn(name = "user_id")
    private MemberSet member;// 시간표에 대응한 사용자 테이블에서 사용자의 id를 가져오기
    private String day_of_week;  // 요일 표시
    private Integer slot_index; // 5*9형태의 월금 블록

    public Long getId(){
        return id;
    }
    public MemberSet getMember(){
        return member;
    }
    public void setMember(MemberSet member){
        this.member = member;
    }
    public String getDay_of_week(){
        return day_of_week;
    }
    public void setDay_of_week(String day_of_week){
        this.day_of_week = day_of_week;
    }
    public Integer getSlot_index(){
        return slot_index;
    }
    public void setSlot_index(Integer slot_index){
        this.slot_index = slot_index;
    }

}
