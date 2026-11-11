package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.ChatMessage;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.service.ChatMessageService;
import com.example.teamjavaprogramming.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageService chatMessageService;
    private final MemberService memberService;
    public ChatMessageController(ChatMessageService chatMessageService,
                                 MemberService memberService,
                                 ChatRoomMemberRepository chatRoomMemberRepository) {
        this.chatMessageService = chatMessageService;
        this.memberService = memberService;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    // 특정 채팅방의 메시지 가져오기
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long roomId,
            HttpSession session) {
        String username = (String) session.getAttribute("userName");
        // 로그인 여부 확인
        if (username == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        // 채팅방 참여 여부 확인
        boolean isMember = chatRoomMemberRepository
                .findByRoomIdAndUsername(roomId, username)
                .isPresent();
        if (!isMember) {
            return ResponseEntity.status(403).body("채팅방 참여자가 아닙니다.");
        }
        List<ChatMessage> messages =
                chatMessageService.findMessages(roomId);
        chatMessageService.markAsRead(roomId, username);// 메시지를 조회했으므로 읽음 처리
        return ResponseEntity.ok(messages);
    }

    // 메시지 보내기
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long roomId,
            @RequestParam String message,
            HttpSession session) {
        String username = (String) session.getAttribute("userName");
        // 로그인 여부 확인
        if (username == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        // 채팅방 참여 여부 확인
        boolean isMember = chatRoomMemberRepository
                .findByRoomIdAndUsername(roomId, username)
                .isPresent();
        if (!isMember) {
            return ResponseEntity.status(403).body("채팅방 참여자가 아닙니다.");
        }
        // 회원 정보 가져오기
        MemberSet member = memberService
                .findByUserName(username)
                .orElseThrow();
        String senderName = member.getStdName();
        ChatMessage savedMessage = chatMessageService.sendMessage(
                roomId,
                username,
                senderName,
                message
        );
        return ResponseEntity.ok(savedMessage);
    }
}

