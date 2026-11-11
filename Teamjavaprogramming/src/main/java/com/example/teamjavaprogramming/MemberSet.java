package com.example.teamjavaprogramming;
import jakarta.persistence.*;
import java.time.LocalDate;// 날자 입력

@Entity
public class MemberSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //고유번호
    @Column(unique = true, nullable = false)  //중복불가능하고 꼭 있어야됨
    private String userName;  // 계정 아이디
    private String password;  //비밀번호
    private String stdSchool; //학생의 학교
    private String stdName; //학생 닉넴
    private LocalDate createDate; // 가입 날자
    private String stdGender; // 성별
    private LocalDate stdBirth ; // 생년월일
    private String major; //전공
    private Integer stdIDN; //학번
    private String stdCon; //관심콘텐츠
    private String stdCategory; // 주요 콘텐츠

    public Long getId() {
        return id;
    }
    public String getUserName(){
        return userName;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getStdSchool(){
        return stdSchool;
    }
    public void setStdSchool(String stdSchool){
        this.stdSchool = stdSchool;
    }
    public String getStdName(){
        return stdName;
    }
    public void setStdName(String stdName){
        this.stdName = stdName;
    }
    public LocalDate getCreateDate(){
        return createDate;
    }
    public void setCreateDate(LocalDate createDate){
        this.createDate = createDate;
    }
    public LocalDate getStdBirth (){
        return stdBirth ;
    }
    public void setStdBirth (LocalDate stdBirth ){
        this.stdBirth = stdBirth;
    }
    public String getStdGender(){
        return stdGender;
    }
    public void setStdGender(String stdGender){
        this.stdGender = stdGender;
    }
    public String getMajor(){
        return major;
    }
    public void setMajor(String major){
        this.major = major;
    }
    public Integer getStdIDN(){
        return stdIDN;
    }
    public void setStdIDN(Integer stdIDN){
        this.stdIDN = stdIDN;
    }
    public String getStdCon(){
        return stdCon;
    }
    public void setStdCon(String stdCon){
        this.stdCon = stdCon;
    }
    public String getStdCategory() {
        return stdCategory;
    }
    public void setStdCategory(String stdCategory) {
        this.stdCategory = stdCategory;
    }
}
