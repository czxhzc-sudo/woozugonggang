package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.repository.ChatMessageRepository;
import com.example.teamjavaprogramming.service.PostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.example.teamjavaprogramming.ChatRoom;
import org.springframework.stereotype.Controller;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.Timetable;
import com.example.teamjavaprogramming.repository.MemberRepository;
import com.example.teamjavaprogramming.repository.TimetableRepository;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import com.example.teamjavaprogramming.ChatRoomBan;
import com.example.teamjavaprogramming.repository.ChatRoomBanRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MyGroupController {
    private final PostService postService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final TimetableRepository timetableRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomBanRepository chatRoomBanRepository;
    public MyGroupController(ChatRoomMemberRepository chatRoomMemberRepository,
                             ChatRoomRepository chatRoomRepository,
                             PostService postService,
                             ChatMessageRepository chatMessageRepository,
                             MemberRepository memberRepository,
                             TimetableRepository timetableRepository,
                             ChatRoomBanRepository chatRoomBanRepository) {
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.postService = postService;
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
        this.timetableRepository = timetableRepository;
        this.chatRoomBanRepository = chatRoomBanRepository;
    }
    @GetMapping("/my-group")
    public String myGroup(HttpSession session, Model model) {
        // 현재 로그인한 사용자의 username
        String userName = (String) session.getAttribute("userName");
        // 내가 가입한 모든 모임 정보 가져오기
        List<ChatRoomMember> members =
                chatRoomMemberRepository.findByUsername(userName);
        // ChatRoomMember에서 roomId만 뽑기
        List<Long> roomIds = members.stream()
                .map(ChatRoomMember::getRoomId)
                .toList();
        // roomId를 이용해서 실제 ChatRoom 가져오기
        List<ChatRoom> myGroups =
                chatRoomRepository.findByIdIn(roomIds);
        // 채팅방별 안 읽은 메시지 수 저장
        Map<Long, Long> unreadCounts = new HashMap<>();
        for (ChatRoomMember member : members) {
            // 마지막으로 읽은 시간 가져오기
            LocalDateTime lastReadAt = member.getLastReadAt();
            // 기존 가입자의 lastReadAt이 null이면 가입 시간을 사용
            if (lastReadAt == null) {
                lastReadAt = member.getJoinedAt();
            }
            // 읽은 시간 정보가 있는 경우에만 계산
            if (lastReadAt != null) {
                long count = chatMessageRepository
                        .countByRoomIdAndSentAtAfterAndUsernameNot(
                                member.getRoomId(),
                                lastReadAt,
                                userName
                        );
                unreadCounts.put(member.getRoomId(), count);
            }
        }
        // HTML로 전달
        model.addAttribute("myGroups", myGroups);
        // 채팅방별 안 읽은 메시지 수 전달
        model.addAttribute("unreadCounts", unreadCounts);
        return "MyGroup";
    }
    // 채팅방 탈퇴
    @PostMapping("/my-group/{roomId}/leave")
    @ResponseBody
    public ResponseEntity<String> leaveChatRoom(
            @PathVariable Long roomId,
            HttpSession session) {
        String username = (String) session.getAttribute("userName");

        // 로그인 여부 확인
        if (username == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        // 서비스에서 탈퇴 처리
        String result = postService.leaveChatRoom(roomId, username);
        switch (result) {

            case "SUCCESS":
                return ResponseEntity.ok("탈퇴했습니다.");
            case "ROOM_DELETED":
                return ResponseEntity.ok("채팅방과 게시글이 모두 삭제되었습니다.");
            case "NOT_MEMBER":
                return ResponseEntity.status(403)
                        .body("채팅방 참여자가 아닙니다.");
            case "OWNER_HAS_MEMBERS":
                return ResponseEntity.status(403)
                        .body("방장 권한을 다른 참여자에게 먼저 이전해야 탈퇴할 수 있습니다.");
            default:
                return ResponseEntity.internalServerError()
                        .body("채팅방 탈퇴 중 오류가 발생했습니다.");
        }
    }
    // 채팅방 참여자 목록 조회
    @GetMapping("/my-group/{roomId}/members")
    @ResponseBody
    public ResponseEntity<?> getRoomMembers(
            @PathVariable Long roomId,
            HttpSession session) {

        String username = (String) session.getAttribute("userName");

        if (username == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 현재 로그인한 사용자가 이 채팅방의 참여자인지 확인
        if (chatRoomMemberRepository
                .findByRoomIdAndUsername(roomId, username).isEmpty()) {
            return ResponseEntity.status(403).body("채팅방 참여자가 아닙니다.");
        }

        List<ChatRoomMember> roomMembers =
                chatRoomMemberRepository.findByRoomId(roomId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (ChatRoomMember roomMember : roomMembers) {

            Optional<MemberSet> optionalMember =
                    memberRepository.findByUserName(roomMember.getUsername());

            if (optionalMember.isEmpty()) {
                continue;
            }
            MemberSet member = optionalMember.get();
            Map<String, Object> profile = new LinkedHashMap<>();

            // 프로필에 필요한 정보만 전달
            profile.put("username", member.getUserName());
            profile.put("name", member.getStdName());
            // 채팅방 권한 정보 전달
            profile.put("memberRole", roomMember.getMemberRole());
            // 공개 설정에 따라 프로필 정보 전달
            profile.put("gender",
                    member.isShowGender() ? member.getStdGender() : null);
            profile.put("school",
                    member.isShowSchool() ? member.getStdSchool() : null);
            profile.put("studentNumber",
                    member.isShowStudentNumber() ? member.getStdIDN() : null);
            profile.put("interests",
                    member.isShowInterests() ? member.getStdCon() : null);
            profile.put("major",
                    member.isShowMajor() ? member.getMajor() : null);
            result.add(profile);
        }
        return ResponseEntity.ok(result);
    }

    // 특정 채팅방 참여자의 시간표 조회
    @GetMapping("/my-group/{roomId}/members/{targetUsername}/timetable")
    @ResponseBody
    public ResponseEntity<?> getMemberTimetable(
            @PathVariable Long roomId,
            @PathVariable String targetUsername,
            HttpSession session) {

        String username = (String) session.getAttribute("userName");

        if (username == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        // 요청한 사람이 채팅방 참여자인지 확인
        if (chatRoomMemberRepository
                .findByRoomIdAndUsername(roomId, username).isEmpty()) {
            return ResponseEntity.status(403).body("채팅방 참여자가 아닙니다.");
        }
        // 시간표를 조회할 대상도 해당 채팅방 참여자인지 확인
        if (chatRoomMemberRepository
                .findByRoomIdAndUsername(roomId, targetUsername).isEmpty()) {
            return ResponseEntity.status(403).body("해당 회원은 채팅방 참여자가 아닙니다.");
        }
        Optional<MemberSet> targetMember =
                memberRepository.findByUserName(targetUsername);

        if (targetMember.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Timetable> timetables =
                timetableRepository.findByMember_Id(targetMember.get().getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (Timetable timetable : timetables) {
            Map<String, Object> slot = new LinkedHashMap<>();

            slot.put("day", timetable.getDay_of_week());
            slot.put("period", timetable.getSlot_index());

            result.add(slot);
        }
        return ResponseEntity.ok(result);
    }
    // 채팅방 소유권 이전
    @PostMapping("/my-group/{roomId}/transfer-owner")
    @ResponseBody
    public ResponseEntity<String> transferOwnership(
            @PathVariable Long roomId,
            @RequestParam String targetUsername,
            HttpSession session) {
        String username = (String) session.getAttribute("userName");
        // 로그인 여부 확인
        if (username == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }
        // 서비스에서 소유권 이전 처리
        String result = postService.transferOwnership(
                roomId,
                username,
                targetUsername
        );
        switch (result) {
            case "SUCCESS":
                return ResponseEntity.ok("채팅방 소유권이 이전되었습니다.");
            case "ROOM_NOT_FOUND":
                return ResponseEntity.status(404)
                        .body("채팅방을 찾을 수 없습니다.");
            case "NOT_OWNER":
                return ResponseEntity.status(403)
                        .body("방장만 소유권을 이전할 수 있습니다.");
            case "TARGET_NOT_MEMBER":
                return ResponseEntity.badRequest()
                        .body("새 방장은 해당 채팅방의 참여자여야 합니다.");
            case "SAME_USER":
                return ResponseEntity.badRequest()
                        .body("자기 자신에게는 소유권을 이전할 수 없습니다.");
            case "ROOM_DELETED":
                return ResponseEntity.ok("채팅방과 게시글이 모두 삭제되었습니다.");
            default:
                return ResponseEntity.internalServerError()
                        .body("소유권 이전 중 오류가 발생했습니다.");
        }
    }

    // 채팅방 참여자 추방
    @PostMapping("/my-group/{roomId}/kick")
    @ResponseBody
    public ResponseEntity<String> kickMember(
            @PathVariable Long roomId,
            @RequestParam String targetUsername,
            HttpSession session) {

        // 로그인한 회원 확인
        String username = (String) session.getAttribute("userName");

        if (username == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        // 추방 처리
        String result = postService.kickMember(
                roomId,
                username,
                targetUsername
        );

        // 처리 결과 반환
        switch (result) {

            case "SUCCESS":
                return ResponseEntity.ok("회원을 추방했습니다.");

            case "ROOM_NOT_FOUND":
                return ResponseEntity.status(404)
                        .body("채팅방을 찾을 수 없습니다.");

            case "NOT_OWNER":
                return ResponseEntity.status(403)
                        .body("방장만 회원을 추방할 수 있습니다.");

            case "CANNOT_KICK_SELF":
                return ResponseEntity.badRequest()
                        .body("자기 자신은 추방할 수 없습니다.");

            case "TARGET_NOT_MEMBER":
                return ResponseEntity.badRequest()
                        .body("해당 회원은 채팅방 참여자가 아닙니다.");

            case "TARGET_IS_OWNER":
                return ResponseEntity.badRequest()
                        .body("방장은 추방할 수 없습니다.");

            case "TARGET_NOT_FOUND":
                return ResponseEntity.status(404)
                        .body("회원 정보를 찾을 수 없습니다.");

            default:
                return ResponseEntity.internalServerError()
                        .body("회원 추방 중 오류가 발생했습니다.");
        }
    }

    // 채팅방 추방 회원 목록 조회
    @GetMapping("/my-group/{roomId}/bans")
    @ResponseBody
    public ResponseEntity<?> getBannedMembers(
            @PathVariable Long roomId,
            HttpSession session) {

        // 1. 로그인 회원 확인
        String username = (String) session.getAttribute("userName");

        if (username == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        // 2. 현재 사용자가 방장인지 확인
        ChatRoomMember owner =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, username)
                        .orElse(null);

        if (owner == null ||
                !Integer.valueOf(1).equals(owner.getMemberRole())) {
            return ResponseEntity.status(403)
                    .body("방장만 추방 회원 목록을 확인할 수 있습니다.");
        }

        // 3. 해당 채팅방의 추방 기록 조회
        List<ChatRoomBan> bans =
                chatRoomBanRepository.findByRoomId(roomId);

        // 4. 화면에 전달할 데이터 생성
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChatRoomBan ban : bans) {

            Map<String, Object> bannedMember = new LinkedHashMap<>();

            bannedMember.put("username", ban.getUsername());
            bannedMember.put("bannedAt", ban.getBannedAt());

            // 회원 이름 조회
            Optional<MemberSet> member =
                    memberRepository.findByUserName(ban.getUsername());

            if (member.isPresent()) {
                bannedMember.put("name", member.get().getStdName());
            } else {
                bannedMember.put("name", ban.getUsername());
            }

            result.add(bannedMember);
        }

        return ResponseEntity.ok(result);
    }

    // 채팅방 추방 해제
    @PostMapping("/my-group/{roomId}/unban")
    @ResponseBody
    public ResponseEntity<String> unbanMember(
            @PathVariable Long roomId,
            @RequestParam String targetUsername,
            HttpSession session) {

        // 1. 로그인 회원 확인
        String username = (String) session.getAttribute("userName");

        if (username == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        // 2. 서비스에서 추방 해제 처리
        String result = postService.unbanMember(
                roomId,
                username,
                targetUsername
        );

        // 3. 처리 결과 반환
        switch (result) {

            case "SUCCESS":
                return ResponseEntity.ok(
                        "추방이 해제되었습니다. 해당 회원은 직접 다시 가입할 수 있습니다."
                );

            case "ROOM_NOT_FOUND":
                return ResponseEntity.status(404)
                        .body("채팅방을 찾을 수 없습니다.");

            case "NOT_OWNER":
                return ResponseEntity.status(403)
                        .body("방장만 추방을 해제할 수 있습니다.");

            case "BAN_NOT_FOUND":
                return ResponseEntity.status(404)
                        .body("추방 기록을 찾을 수 없습니다.");

            default:
                return ResponseEntity.internalServerError()
                        .body("추방 해제 중 오류가 발생했습니다.");
        }
    }

    // 방장이 채팅방 전체 삭제
    @PostMapping("/my-group/{roomId}/destroy")
    @ResponseBody
    public ResponseEntity<String> destroyChatRoom(
            @PathVariable Long roomId,
            HttpSession session) {

        // 1. 로그인 회원 확인
        String username = (String) session.getAttribute("userName");

        if (username == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        // 2. 서비스에서 채팅방 전체 삭제
        String result = postService.destroyChatRoom(
                roomId,
                username
        );

        // 3. 처리 결과 반환
        switch (result) {

            case "SUCCESS":
                return ResponseEntity.ok(
                        "채팅방, 게시글, 참여자 및 추방 기록이 모두 삭제되었습니다."
                );

            case "ROOM_NOT_FOUND":
                return ResponseEntity.status(404)
                        .body("채팅방을 찾을 수 없습니다.");

            case "NOT_OWNER":
                return ResponseEntity.status(403)
                        .body("방장만 채팅방을 전체 삭제할 수 있습니다.");

            default:
                return ResponseEntity.internalServerError()
                        .body("채팅방 삭제 중 오류가 발생했습니다.");
        }
    }

}
