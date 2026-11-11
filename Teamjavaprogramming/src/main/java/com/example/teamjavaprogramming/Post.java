package com.example.teamjavaprogramming;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //게시글 번호
    private String title;  // 게시글 제목
    private String content; // 게시글 내용
    private String author; // 작성자

    @ElementCollection  //파일 따로 안만들어도 테이블 생성
    private List<String> hashtags; // 콘텐츠 태그

    private String category;  // 주요 콘텐츠
    private LocalDate createDate; // 작성 날자
    private int viewCount; // 조회수
    private String recruitType; // 남녀 구분 할것인지?
    private int maxMembers;  //전체 모집 인원
    private int currentMembers; // 현재 참여 인원
    private int maleMaxMembers; // 남자 최대 수
    private int maleCurrentMembers; // 현재 남자 인원
    private int femaleMaxMembers; // 여자 최대 수
    private int femaleCurrentMembers; // 현재 여자 인원
    private String joinType;  // 가입 방법(공개,심사)
    private String meetingDay;       // 모임 요일
    private LocalTime startTime;     // 시작 시간
    private LocalTime endTime;  //종료 시간
    private String recruitMajor;      // 모집 전공
    private Integer recruitStartYear;  // 시작 학번
    private Integer recruitEndYear;    // 끝 학번

    public Long getId() {
        return id;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getAuthor(){
        return author;
    }
    public void setAuthor(String author){
        this.author = author;
    }
    public List<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
    public LocalDate getCreateDate(){
        return createDate;
    }
    public void setCreateDate(LocalDate createDate){
        this.createDate = createDate;
    }
    public int getViewCount(){
        return viewCount;
    }
    public void setViewCount(int viewCount){
        this.viewCount = viewCount;
    }

    public int getMaxMembers(){
        return maxMembers;
    }
    public void setMaxMembers(int maxMembers){
        this.maxMembers = maxMembers;
    }
    public int getCurrentMembers(){
        return currentMembers;
    }
    public void setCurrentMembers(int currentMembers){
        this.currentMembers = currentMembers;
    }
    public int getMaleMaxMembers(){
        return maleMaxMembers;
    }
    public void setMaleMaxMembers(int maleMaxMembers){
        this.maleMaxMembers = maleMaxMembers;
    }
    public int getMaleCurrentMembers(){
        return maleCurrentMembers;
    }
    public void setMaleCurrentMembers(int maleCurrentMembers){
        this.maleCurrentMembers = maleCurrentMembers;
    }
    public int getFemaleMaxMembers(){
        return femaleMaxMembers;
    }
    public void setFemaleMaxMembers(int femaleMaxMembers){
        this.femaleMaxMembers = femaleMaxMembers;
    }
    public int getFemaleCurrentMembers(){
        return femaleCurrentMembers;
    }
    public void setFemaleCurrentMembers(int femaleCurrentMembers){
        this.femaleCurrentMembers = femaleCurrentMembers;
    }
    public String getJoinType(){
        return joinType;
    }
    public void setJoinType(String joinType){
        this.joinType = joinType;
    }
    public String getCategory(){
        return category;
    }
    public void setCategory(String category){
        this.category = category;
    }
    public void setMeetingDay(String meetingDay){
        this.meetingDay = meetingDay;
    }
    public String getMeetingDay(){
        return meetingDay;
    }
    public void setStartTime(LocalTime startTime){
        this.startTime = startTime;
    }
    public LocalTime getStartTime(){
        return startTime;
    }
    public void setEndTime(LocalTime endTime){
        this.endTime = endTime;
    }
    public LocalTime getEndTime(){
        return endTime;
    }
    public void setRecruitType(String recruitType){
        this.recruitType = recruitType;
    }
    public String getRecruitType(){
        return recruitType;
    }

    public void setRecruitMajor(String recruitMajor){
        this.recruitMajor = recruitMajor;
    }
    public String getRecruitMajor(){
        return recruitMajor;
    }
    public void setRecruitStartYear(Integer recruitStartYear){
        this.recruitStartYear = recruitStartYear;
    }
    public Integer getRecruitStartYear(){
        return recruitStartYear;
    }
    public void setRecruitEndYear(Integer recruitEndYear){
        this.recruitEndYear = recruitEndYear;
    }
    public Integer getRecruitEndYear(){
        return recruitEndYear;
    }
}
