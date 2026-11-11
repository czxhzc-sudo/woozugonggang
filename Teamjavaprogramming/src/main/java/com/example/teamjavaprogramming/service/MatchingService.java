package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatRoom;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.MatchingRequest;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import com.example.teamjavaprogramming.repository.MatchingRequestRepository;
import com.example.teamjavaprogramming.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final MatchingRequestRepository matchingRequestRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public MatchingService(
            MatchingRequestRepository matchingRequestRepository,
            MemberRepository memberRepository,
             ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository
    ) {
        this.matchingRequestRepository = matchingRequestRepository;
        this.memberRepository = memberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    // 매칭 신청을 저장하고 상대를 찾는다.
    @Transactional
    public MatchingRequest startMatching(
            String username,
            String targetGender,
            String targetMajor,
            Integer minStudentYear,
            Integer maxStudentYear,
            String targetContents
    ) {
        // 현재 회원 정보 조회 및 잠금
        MemberSet me = memberRepository
                .findByUserNameForUpdate(username)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
        // 이미 매칭 중인지 확인
        MatchingRequest existingRequest =
                matchingRequestRepository.findByUsernameAndStatusForUpdate(
                        username,
                        "WAITING"
                );
        // 대기 중인 신청이 있으면 새로 만들지 않는다.
        if (existingRequest != null) {
            return existingRequest;
        }

        // 새로운 매칭 신청 생성
        MatchingRequest request = new MatchingRequest();

        request.setUsername(username);
        request.setTargetGender(targetGender);
        request.setTargetMajor(targetMajor);
        request.setMinStudentYear(minStudentYear);
        request.setMaxStudentYear(maxStudentYear);
        request.setTargetContents(targetContents);
        request.setStatus("WAITING");
        request.setCreatedAt(LocalDateTime.now());
        // DB에 저장
        matchingRequestRepository.save(request);
        // 상대방 찾기
        findMatch(request,me);
        return request;
    }
    // 매칭 취소
    @Transactional
    public MatchingRequest cancelMatching(String username) {
        // 현재 대기 중인 신청을 잠금 상태로 조회
        MatchingRequest request =
                matchingRequestRepository.findByUsernameAndStatusForUpdate(
                        username, "WAITING"
                );
        if (request == null) {
            MatchingRequest latestRequest =
                    matchingRequestRepository
                            .findTopByUsernameOrderByCreatedAtDesc(username);
            if (latestRequest == null) {
                throw new RuntimeException("취소할 매칭 신청이 없습니다.");
            }
            if ("MATCHED".equals(latestRequest.getStatus())) {
                throw new RuntimeException("이미 매칭이 완료되어 취소할 수 없습니다.");
            }
            // 이미 취소된 요청이면 그대로 반환
            return latestRequest;
        }
        // 대기 중인 요청만 취소
        request.setStatus("CANCELLED");
        return matchingRequestRepository.save(request);
    }

    // 현재 WAITING 중인 사람 중 나와 맞는 사람을 찾는다.
    private void findMatch(MatchingRequest myRequest,
                           MemberSet me) {

        List<MatchingRequest> waitingRequests =
                matchingRequestRepository.findByStatusForUpdate("WAITING");
        for (MatchingRequest otherRequest : waitingRequests) {

            // 자기 자신은 제외
            if (otherRequest.getUsername()
                    .equals(myRequest.getUsername())) {
                continue;
            }
            // 상대방 회원정보 가져오기
            MemberSet other = memberRepository
                    .findByUserName(otherRequest.getUsername())
                    .orElse(null);

            if (other == null) {
                continue;
            }

            // 서로의 조건을 확인
            boolean myCondition = isMatch(other, myRequest, otherRequest);
            boolean otherCondition = isMatch(me, otherRequest, myRequest);
            System.out.println("================================");
            System.out.println("매칭 확인");
            System.out.println("나 : " + me.getStdName());
            System.out.println("상대 : " + other.getStdName());
            System.out.println("내 조건 → 상대 : " + myCondition);
            System.out.println("상대 조건 → 나 : " + otherCondition);
            System.out.println("================================");

            // 양쪽 모두 만족하면 매칭 성공
            if (myCondition && otherCondition) {
                // 채팅방 생성
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setRoomName(
                        me.getStdName() + ", " + other.getStdName()
                );
                chatRoom.setCreatedAt(LocalDateTime.now());
                chatRoom.setPost(null);
                chatRoomRepository.save(chatRoom);
                // 내 채팅방 참여 정보
                ChatRoomMember myMember = new ChatRoomMember();

                myMember.setRoomId(chatRoom.getId());
                myMember.setUsername(me.getUserName());
                myMember.setJoinedAt(LocalDateTime.now());
                myMember.setLastReadAt(myMember.getJoinedAt());

                chatRoomMemberRepository.save(myMember);
                // 상대방 채팅방 참여 정보
                ChatRoomMember otherMember = new ChatRoomMember();
                otherMember.setRoomId(chatRoom.getId());
                otherMember.setUsername(other.getUserName());
                otherMember.setJoinedAt(LocalDateTime.now());
                otherMember.setLastReadAt(otherMember.getJoinedAt());

                chatRoomMemberRepository.save(otherMember);
                // 채팅방과 참여자 저장이 끝난 뒤 매칭 완료 처리
                myRequest.setStatus("MATCHED");
                otherRequest.setStatus("MATCHED");
                myRequest.setChatRoomId(chatRoom.getId());
                otherRequest.setChatRoomId(chatRoom.getId());

                matchingRequestRepository.save(myRequest);
                matchingRequestRepository.save(otherRequest);

                System.out.println(
                        "매칭 성공 : "
                                + me.getStdName()
                                + " ↔ "
                                + other.getStdName() );
                // 한 명과 매칭되면 더 이상 찾지 않는다.
                break;
            }
        }
    }

    // 한 사람이 원하는 조건에 상대방이 맞는지 확인
    private boolean isMatch(
            MemberSet target,
            MatchingRequest request,
            MatchingRequest targetRequest
    ) {
        boolean genderMatch = isGenderMatch(
                target.getStdGender(),
                request.getTargetGender()
        );

        boolean majorMatch = isMajorMatch(
                target.getMajor(),
                request.getTargetMajor()
        );

        boolean yearMatch = isStudentYearMatch(
                target.getStdIDN(),
                request.getMinStudentYear(),
                request.getMaxStudentYear()
        );

        // 두 사람이 빠른 매칭에서 선택한 콘텐츠 비교
        boolean contentMatch = isContentMatch(
                request.getTargetContents(),
                targetRequest.getTargetContents()
        );

        System.out.println("========== 상세 매칭 검사 ==========");
        System.out.println("상대방: " + target.getUserName());
        System.out.println("성별: " + genderMatch);
        System.out.println("전공: " + majorMatch);
        System.out.println("학번: " + yearMatch);
        System.out.println("콘텐츠: " + contentMatch);
        System.out.println("최종 결과: "
                + (genderMatch && majorMatch && yearMatch && contentMatch));
        System.out.println("===================================");

        return genderMatch && majorMatch && yearMatch && contentMatch;
    }
    // 성별 비교
    private boolean isGenderMatch(
            String targetGender,
            String wantedGender
    ) {
        // 무관이면 누구든 가능
        if ("무관".equals(wantedGender)) {
            return true;
        }
        return wantedGender != null
                && wantedGender.equals(targetGender);
    }

    // 전공 비교
    private boolean isMajorMatch(
            String targetMajor,
            String wantedMajor
    ) {
        // 아무 전공도 선택하지 않았으면 무관
        if (wantedMajor == null
                || wantedMajor.isBlank()
                || "무관".equals(wantedMajor)) {
            return true;
        }
        // 상대방 전공이 없으면 실패
        if (targetMajor == null
                || targetMajor.isBlank()) {
            return false;
        }
        String[] wantedList = wantedMajor.split(",");
        // 내가 선택한 전공 중 하나라도 상대방 전공과 같으면 OK
        for (String wanted : wantedList) {

            if (wanted.trim().equals(targetMajor.trim())) {
                return true;
            }
        }
        return false;
    }

    // 학번 비교
    private boolean isStudentYearMatch(
            Integer studentId,
            Integer minYear,
            Integer maxYear
    ) {
        // 최소/최대 학번을 모두 선택하지 않았다면 무관
        if (minYear == null && maxYear == null) {
            return true;
        }
        // 학번이 없으면 실패
        if (studentId == null) {
            return false;
        }
        // 학번의 앞 4자리를 학번 연도로 사용
        int studentYear = studentId / 10000;
        // 최소 학번 확인
        if (minYear != null && studentYear < minYear) {
            return false;
        }
        // 최대 학번 확인
        if (maxYear != null && studentYear > maxYear) {
            return false;
        }

        return true;
    }
    // 콘텐츠 비교
    private boolean isContentMatch(
            String myContents,
            String targetContents
    ) {
        // 내가 콘텐츠를 선택하지 않았다면 콘텐츠 조건은 무관
        if (myContents == null || myContents.isBlank()) {
            return true;
        }

        // 상대방이 콘텐츠를 선택하지 않았다면 콘텐츠 조건은 무관
        if (targetContents == null || targetContents.isBlank()) {
            return true;
        }

        Set<String> mySet = Arrays.stream(myContents.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> targetSet = Arrays.stream(targetContents.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        // 두 사람이 선택한 콘텐츠 중 하나라도 겹치면 true
        return mySet.stream().anyMatch(targetSet::contains);
    }
    // 가장 최근 매칭 상태 조회
    @Transactional(readOnly = true)
    public MatchingRequest getMatchingStatus(String username) {

        return matchingRequestRepository
                .findTopByUsernameOrderByCreatedAtDesc(username);
    }
}
