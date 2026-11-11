package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByMember_Id(Long memberId); // 시간표 테이블을 리스트형태로 맴버의 id와 연결

    // 특정 회원의 기존 시간표 삭제
    void deleteByMember_Id(Long memberId);
}
