
package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatRoom;
import com.example.teamjavaprogramming.Post;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import com.example.teamjavaprogramming.repository.PostRepository;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class PostCleanupService {

    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;

    public PostCleanupService(
            PostRepository postRepository,
            ChatRoomRepository chatRoomRepository) {

        this.postRepository = postRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    // 1월 1일과 7월 1일 한국 시간 자정에 실행
    @Scheduled(
            cron = "0 0 0 1 1,7 *",
            zone = "Asia/Seoul"
    )
    @Transactional
    public void deleteExpiredPosts() {

        cleanupExpiredPosts();
    }

    // 서버가 시작된 후에도 만료 게시글 검사
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanupOnStartup(ApplicationReadyEvent event) {

        cleanupExpiredPosts();
    }

    // 실제 게시글 삭제 처리
    private void cleanupExpiredPosts() {

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Seoul")
        );

        LocalDate semesterStart;

        // 현재 학기 시작일 계산
        if (today.getMonthValue() >= 7) {

            semesterStart = LocalDate.of(
                    today.getYear(), 7, 1
            );

        } else {

            semesterStart = LocalDate.of(
                    today.getYear(), 1, 1
            );
        }

        LocalDateTime cutoff =
                semesterStart.atStartOfDay();
        // 현재 학기 시작일 이전 게시글 조회
        List<Post> expiredPosts =
                postRepository.findByCreateDateBefore(cutoff);
        for (Post post : expiredPosts) {
            // 게시글에 연결된 채팅방 조회
            ChatRoom chatRoom =
                    chatRoomRepository.findByPostId(
                            post.getId()
                    );
            // 채팅방과 게시글의 연결만 해제
            if (chatRoom != null) {
                chatRoom.setPost(null);
            }
            // 게시글만 삭제
            postRepository.delete(post);
        }
        System.out.println(
                "[게시글 자동 삭제] 기준 날짜: "
                        + cutoff
                        + ", 삭제된 게시글 수: "
                        + expiredPosts.size()
        );
    }
}
