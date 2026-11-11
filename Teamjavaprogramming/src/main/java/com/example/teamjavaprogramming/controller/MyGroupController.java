package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.repository.ChatMessageRepository;
import com.example.teamjavaprogramming.service.PostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.example.teamjavaprogramming.ChatRoom;
import org.springframework.stereotype.Controller;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
public class MyGroupController {
    private final PostService postService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    public MyGroupController(ChatRoomMemberRepository chatRoomMemberRepository,
                             ChatRoomRepository chatRoomRepository,
                             PostService postService,
                             ChatMessageRepository chatMessageRepository) {
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.postService = postService;
        this.chatMessageRepository = chatMessageRepository;
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
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        boolean result = postService.leaveChatRoom(roomId, username);
        if (!result) {
            return ResponseEntity.status(403).body("채팅방 참여자가 아닙니다.");
        }
        return ResponseEntity.ok("탈퇴했습니다.");
    }
}
