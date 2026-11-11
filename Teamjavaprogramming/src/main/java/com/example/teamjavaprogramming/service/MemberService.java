package com.example.teamjavaprogramming.service;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.repository.MemberRepository;
import com.example.teamjavaprogramming.Timetable;
import com.example.teamjavaprogramming.repository.TimetableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;


@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final TimetableRepository timetableRepository;
    public MemberService(MemberRepository memberRepository,
                         TimetableRepository timetableRepository) {
        this.memberRepository = memberRepository;
        this.timetableRepository = timetableRepository;
    }
    //회원가입
    public MemberSet join(MemberSet member) {
        member.setStdSchool("백석대학교");
        member.setCreateDate(LocalDate.now());
        MemberSet savedMember = memberRepository.save(member); //회원 정보 먼저 저장
        return savedMember;
    }
    //로그인
    public boolean login(String userName, String password) {

        Optional<MemberSet> member = memberRepository.findByUserName(userName);

        if (member.isPresent()) {
            return member.get().getPassword().equals(password);
        }
        return false;
    }
    // 아이디 확인
    public boolean checkUserName(String userName){
        return !memberRepository.existsByUserName(userName);//똑같은 아이디가 존재하는지?
    }
    //아이디 찾기
    public Optional<MemberSet> findByUserName(String userName){
        return memberRepository.findByUserName(userName);
    }
    //사용자 정보 업데이트
    public MemberSet update(MemberSet member){
        return memberRepository.save(member);
    }
    //시간표 저장 및 수정
    @Transactional
    public void saveTimetable(MemberSet member, String stdTime){
        // 기존 시간표 삭제
        timetableRepository.deleteByMember_Id(member.getId());
        // 선택한 시간표가 없다면 종료
        if(stdTime == null || stdTime.isBlank()){
            return;
        }
        String[] timeSlots = stdTime.split(",");
        for(String timeSlot:timeSlots){
            String day = timeSlot.substring(0,1);
            Integer period = Integer.parseInt(timeSlot.substring(1));
            Timetable timetable = new Timetable();
            timetable.setMember(member);
            timetable.setDay_of_week(day); //요일 저장
            timetable.setSlot_index(period);//교시 저장
            timetableRepository.save(timetable);
        }
    }
    //회원 시간표 조회
    public List<Timetable> findTimetableByMemberId(Long memberId){
        return timetableRepository.findByMember_Id(memberId);
    }
}
