package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.ChatMessage;
import com.example.teamjavaprogramming.repository.ChatMessageRepository;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              ChatRoomMemberRepository chatRoomMemberRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    // 메시지 저장
    public ChatMessage sendMessage(Long roomId,
                                   String username,
                                   String senderName,
                                   String message) {

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setRoomId(roomId);
        chatMessage.setUsername(username);
        chatMessage.setSenderName(senderName);
        chatMessage.setMessage(message);
        chatMessage.setSentAt(LocalDateTime.now());

        return chatMessageRepository.save(chatMessage);
    }

    // 특정 채팅방의 메시지 가져오기
    public List<ChatMessage> findMessages(Long roomId) {
        return chatMessageRepository
                .findByRoomIdOrderBySentAtAsc(roomId);
    }
    // 채팅방 메시지 읽음 처리
    public void markAsRead(Long roomId, String username) {

        ChatRoomMember member = chatRoomMemberRepository
                .findByRoomIdAndUsername(roomId, username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "채팅방 회원 정보를 찾을 수 없습니다."
                        )
                );
        // 마지막으로 읽은 시간 갱신
        member.setLastReadAt(LocalDateTime.now());
        chatRoomMemberRepository.save(member);
    }
}