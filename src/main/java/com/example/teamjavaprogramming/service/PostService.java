package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatRoom;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.Post;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import com.example.teamjavaprogramming.repository.PostRepository;
import com.example.teamjavaprogramming.ChatRoomBan;
import com.example.teamjavaprogramming.repository.ChatRoomBanRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.example.teamjavaprogramming.repository.ChatMessageRepository;
import com.example.teamjavaprogramming.ActivityHistory;
import com.example.teamjavaprogramming.repository.ActivityHistoryRepository;

@Service
public class PostService {
    private final ChatMessageRepository chatMessageRepository;
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomBanRepository chatRoomBanRepository;
    private final ActivityHistoryRepository activityHistoryRepository;
    public PostService(PostRepository postRepository,
                       ChatRoomRepository chatRoomRepository,
                       ChatRoomMemberRepository chatRoomMemberRepository,
                       ChatMessageRepository chatMessageRepository,
                       MemberService memberService,
                       ChatRoomBanRepository chatRoomBanRepository,
                       ActivityHistoryRepository activityHistoryRepository) {
        this.postRepository = postRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.memberService = memberService;
        this.chatRoomBanRepository = chatRoomBanRepository;
        this.activityHistoryRepository = activityHistoryRepository;
    }
    @Transactional
    public Post createPost(Post post, String username) {
        MemberSet member =
                memberService.findByUserName(username).orElseThrow();
        post.setCurrentMembers(1);
        // 성별별 모집인 경우
        if ("성별별".equals(post.getRecruitType())) {
            if ("남성".equals(member.getStdGender())) {
                if (post.getMaleMaxMembers() < 1) {
                    throw new IllegalArgumentException(
                            "남성 작성자는 남성 모집 인원을 최소 1명으로 설정해야 합니다."
                    );
                }
                post.setMaleCurrentMembers(1);
            }

            if ("여성".equals(member.getStdGender())) {
                if (post.getFemaleMaxMembers() < 1) {
                    throw new IllegalArgumentException(
                            "여성 작성자는 여성 모집 인원을 최소 1명으로 설정해야 합니다."
                    );
                }

                post.setFemaleCurrentMembers(1);
            }
        }
        post.setCreateDate(LocalDateTime.now());
        // 1. 게시글 저장
        Post savedPost = postRepository.save(post);
        // 2. 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        // 3. 게시글과 채팅방 연결
        chatRoom.setPost(savedPost);
        // 4. 채팅방 이름 = 게시글 제목
        chatRoom.setRoomName(savedPost.getTitle());
        // 5. 채팅방 생성 시간
        chatRoom.setCreatedAt(LocalDateTime.now());
        // 6. 채팅방 저장
        chatRoomRepository.save(chatRoom);
        // 7. 작성자를 채팅방에 자동 가입
        ChatRoomMember memberInfo = new ChatRoomMember();
        // 방 번호 저장
        memberInfo.setRoomId(chatRoom.getId());
        // 로그인한 사용자의 username 저장
        memberInfo.setUsername(username);
        // 작성자가 채팅방에 가입한 시간
        LocalDateTime joinedAt = LocalDateTime.now();
        memberInfo.setJoinedAt(joinedAt);
        // 마지막으로 읽은 시간 초기화
        memberInfo.setLastReadAt(joinedAt);
        // 채팅방 회원 저장
        memberInfo.setMemberRole(1);
        // 작성자를 방장으로 지정
        chatRoomMemberRepository.save(memberInfo);
        // 작성자의 활동 기록 저장
        saveActivityHistory(
                username,
                savedPost,
                chatRoom,
                joinedAt
        );
        return savedPost;
    }
    @Transactional
    public String joinPost(Long postId, String username) {

        // 게시글에 연결된 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findAll()
                .stream()
                .filter(room ->
                                room.getPost() != null
                                        && room.getPost().getId().equals(postId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("게시글에 연결된 채팅방을 찾을 수 없습니다."));
        Post post = chatRoom.getPost();
        // 현재 가입하려는 회원 정보
        MemberSet member =
                memberService.findByUserName(username).orElseThrow();
        // 추방된 회원인지 확인
        if (chatRoomBanRepository.existsByRoomIdAndUsername(chatRoom.getId(), username)) {
            return "BANNED";
        }
        //작성자 인지?
        if (post.getAuthor().equals(member.getStdName())){
            return "OWNER";
        }
        // 이미 가입했는지 확인
        List<ChatRoomMember> members =
                chatRoomMemberRepository.findByRoomId(chatRoom.getId());

        for (ChatRoomMember roomMember : members) {
            if (roomMember.getUsername().equals(username)) {
                return "ALREADY_JOINED";
            }
        }
        // 모집 전공 조건 확인
        if (!"무관".equals(post.getRecruitMajor())) {
            if (!post.getRecruitMajor().equals(member.getMajor())) {
                return "MAJOR_NOT_MATCH";
            }
        }
        // 모집 학번 조건 확인
        if (post.getRecruitStartYear() != null
                && post.getRecruitEndYear() != null) {
            // 8자리 학번에서 앞 4자리 추출
            // 예: 20231234 -> 2023
            if (member.getStdIDN() == null) {
                return "STUDENT_ID_NOT_MATCH";
            }
            Integer studentYear = member.getStdIDN() / 10000;
            if (studentYear < post.getRecruitStartYear()
                    || studentYear > post.getRecruitEndYear()) {
                return "STUDENT_ID_NOT_MATCH";
            }
        }
        // 현재 인원이 최대 인원에 도달했으면 가입 불가
        if (post.getCurrentMembers() >= post.getMaxMembers()) {
            return "FULL";
        }
        // 성별별 모집
        if ("성별별".equals(post.getRecruitType())) {
            String gender = member.getStdGender();

            // 남성 모집 인원이 가득 찬 경우
            if ("남성".equals(gender) &&
                    post.getMaleCurrentMembers() >= post.getMaleMaxMembers()) {
                return "MALE_FULL";
            }
            // 여성 모집 인원이 가득 찬 경우
            if ("여성".equals(gender) &&
                    post.getFemaleCurrentMembers() >= post.getFemaleMaxMembers()) {
                return "FEMALE_FULL";
            }
        }
        // 가입 정보 생성
        ChatRoomMember memberInfo = new ChatRoomMember();

        memberInfo.setRoomId(chatRoom.getId());
        memberInfo.setUsername(username);
        // 실제 가입 시간 저장
        LocalDateTime joinedAt = LocalDateTime.now();
        memberInfo.setJoinedAt(joinedAt);
        memberInfo.setLastReadAt(joinedAt);
        // 일반 참여자로 지정
        memberInfo.setMemberRole(0);
        chatRoomMemberRepository.save( memberInfo);
        // 모집 인원 증가
        post.setCurrentMembers(
                post.getCurrentMembers() + 1
        );
        // 성별별 모집이면 성별 인원도 증가
        if ("성별별".equals(post.getRecruitType())) {
            if ("남성".equals(member.getStdGender())) {
                post.setMaleCurrentMembers(
                        post.getMaleCurrentMembers() + 1
                );
            } else if ("여성".equals(member.getStdGender())) {
                post.setFemaleCurrentMembers(
                        post.getFemaleCurrentMembers() + 1
                );
            }
        }
        postRepository.save(post);
        // 가입 당시 게시글 정보를 활동 기록에 저장
        saveActivityHistory(
                username,
                post,
                chatRoom,
                joinedAt
        );

        return "SUCCESS";
    }
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }
    // 내가 작성한 게시글 조회
    public List<Post> findMyPosts(String author) {
        return postRepository.findByAuthor(author);
    }
    // 게시글 삭제 (채팅방은 유지)
    @Transactional
    public boolean deletePost(Long postId, String username) {

        // 로그인 회원 조회
        MemberSet member = memberService.findByUserName(username)
                .orElseThrow();
        // 삭제할 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow();
        // 작성자 확인
        if (!post.getAuthor().equals(member.getStdName())) {
            return false;
        }

        // 게시글에 연결된 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId);

        // 채팅방과 게시글의 연결만 해제
        if (chatRoom != null) {
            chatRoom.setPost(null);
            chatRoomRepository.save(chatRoom);
        }
        // 게시글만 삭제
        postRepository.delete(post);
        return true;
    }
    // 채팅방 탈퇴
    @Transactional
    public String leaveChatRoom(Long roomId, String username) {

        // 1. 채팅방 조회 및 잠금
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() ->
                        new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 2. 해당 회원이 채팅방에 참여 중인지 확인
        ChatRoomMember roomMember =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, username)
                        .orElse(null);

        // 참여자가 아닌 경우
        if (roomMember == null) {
            return "NOT_MEMBER";
        }

        // 3. 현재 채팅방 참여자 목록 조회
        List<ChatRoomMember> roomMembers =
                chatRoomMemberRepository.findByRoomId(roomId);

        // 4. 방장인지 확인
        boolean isOwner =
                Integer.valueOf(1).equals(roomMember.getMemberRole());

        // 방장이고 다른 참여자가 남아 있는 경우 탈퇴 불가
        if (isOwner && roomMembers.size() > 1) {
            return "OWNER_HAS_MEMBERS";
        }

        // 5. 탈퇴하는 회원 정보 조회
        MemberSet member = memberService.findByUserName(username)
                .orElseThrow();

        Post post = chatRoom.getPost();

        // 6. 마지막 방장이라면 채팅방 전체 삭제
        if (isOwner && roomMembers.size() == 1) {
            // 채팅방 메시지 삭제
            chatMessageRepository.deleteByRoomId(roomId);
            chatMessageRepository.flush();
            // 채팅방 참여 정보 삭제
            chatRoomMemberRepository.deleteAll(roomMembers);
            chatRoomMemberRepository.flush();
            // 채팅방 추방 기록 삭제
            chatRoomBanRepository.deleteByRoomId(roomId);
            chatRoomBanRepository.flush();
            // 연결된 게시글이 있다면 연결 해제 후 게시글 삭제
            if (post != null) {
                chatRoom.setPost(null);
                // 게시글을 참조하는 외래 키를 먼저 해제
                chatRoomRepository.saveAndFlush(chatRoom);
                // 게시글 삭제
                postRepository.delete(post);
            }
            // 채팅방 삭제
            chatRoomRepository.delete(chatRoom);

            return "ROOM_DELETED";
        }

        if (post != null) {
            // 전체 모집 인원 감소
            if (post.getCurrentMembers() > 0) {
                post.setCurrentMembers(post.getCurrentMembers() - 1);
            }

            // 성별별 모집인 경우 해당 성별 인원 감소
            if ("성별별".equals(post.getRecruitType())) {

                if ("남성".equals(member.getStdGender())
                        && post.getMaleCurrentMembers() > 0) {

                    post.setMaleCurrentMembers(
                            post.getMaleCurrentMembers() - 1
                    );

                } else if ("여성".equals(member.getStdGender())
                        && post.getFemaleCurrentMembers() > 0) {

                    post.setFemaleCurrentMembers(
                            post.getFemaleCurrentMembers() - 1
                    );
                }
            }

            postRepository.save(post);
        }

        // 7. 해당 회원의 채팅방 가입 정보 삭제
        chatRoomMemberRepository.delete(roomMember);
        return "SUCCESS";
    }
    // 채팅방 소유권 이전
    @Transactional
    public String transferOwnership(
            Long roomId,
            String currentUsername,
            String targetUsername) {

        // 1. 채팅방을 잠금 상태로 조회
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElse(null);

        if (chatRoom == null) {
            return "ROOM_NOT_FOUND";
        }

        // 2. 자기 자신에게 위임하는 것은 불가능
        if (currentUsername.equals(targetUsername)) {
            return "SAME_USER";
        }

        // 3. 현재 방장의 참여 정보 조회
        ChatRoomMember currentMember =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, currentUsername)
                        .orElse(null);

        // 현재 사용자가 방장이 맞는지 확인
        if (currentMember == null ||
                !Integer.valueOf(1).equals(currentMember.getMemberRole())) {
            return "NOT_OWNER";
        }

        // 4. 새 방장이 될 회원의 참여 정보 조회
        ChatRoomMember targetMember =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, targetUsername)
                        .orElse(null);

        // 해당 채팅방의 참여자가 아니면 위임 불가능
        if (targetMember == null) {
            return "TARGET_NOT_MEMBER";
        }
        // 5. 새 방장의 회원 정보 조회
        MemberSet newOwner = memberService.findByUserName(targetUsername)
                .orElse(null);
        if (newOwner == null) {
            return "TARGET_NOT_MEMBER";
        }
        // 6. 기존 방장을 일반 참여자로 변경
        currentMember.setMemberRole(0);
        // 7. 새 방장을 방장으로 변경
        targetMember.setMemberRole(1);
        chatRoomMemberRepository.save(currentMember);
        chatRoomMemberRepository.save(targetMember);
        // 8. 연결된 게시글이 있다면 작성자도 변경
        Post post = chatRoom.getPost();

        if (post != null) {
            post.setAuthor(newOwner.getStdName());
            postRepository.save(post);
        }
        return "SUCCESS";
    }
    // 채팅방 참여자 추방
    @Transactional
    public String kickMember(
            Long roomId,
            String ownerUsername,
            String targetUsername) {

        // 1. 채팅방 조회 및 잠금
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElse(null);

        if (chatRoom == null) {
            return "ROOM_NOT_FOUND";
        }

        // 2. 요청한 회원이 방장인지 확인
        ChatRoomMember owner =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, ownerUsername)
                        .orElse(null);

        if (owner == null ||
                !Integer.valueOf(1).equals(owner.getMemberRole())) {
            return "NOT_OWNER";
        }
        // 3. 자기 자신을 추방할 수 없도록 처리
        if (ownerUsername.equals(targetUsername)) {
            return "CANNOT_KICK_SELF";
        }
        // 4. 추방 대상이 채팅방 참여자인지 확인
        ChatRoomMember targetRoomMember =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, targetUsername)
                        .orElse(null);

        if (targetRoomMember == null) {
            return "TARGET_NOT_MEMBER";
        }
        // 5. 다른 방장을 추방할 수 없도록 처리
        if (Integer.valueOf(1).equals(targetRoomMember.getMemberRole())) {
            return "TARGET_IS_OWNER";
        }
        // 6. 추방 대상 회원 정보 조회
        MemberSet targetMember =
                memberService.findByUserName(targetUsername)
                        .orElse(null);
        if (targetMember == null) {
            return "TARGET_NOT_FOUND";
        }
        // 7. 게시글이 연결되어 있다면 모집 인원 감소
        Post post = chatRoom.getPost();
        if (post != null) {
            // 전체 모집 인원 감소
            if (post.getCurrentMembers() > 0) {
                post.setCurrentMembers(
                        post.getCurrentMembers() - 1
                );
            }
            // 성별별 모집인원 감소
            if ("성별별".equals(post.getRecruitType())) {
                if ("남성".equals(targetMember.getStdGender())
                        && post.getMaleCurrentMembers() > 0) {

                    post.setMaleCurrentMembers(
                            post.getMaleCurrentMembers() - 1
                    );
                } else if ("여성".equals(targetMember.getStdGender())
                        && post.getFemaleCurrentMembers() > 0) {

                    post.setFemaleCurrentMembers(
                            post.getFemaleCurrentMembers() - 1
                    );
                }
            }
            postRepository.save(post);
        }
        // 8. 추방 기록 생성
        ChatRoomBan ban = new ChatRoomBan();
        ban.setRoomId(roomId);
        ban.setUsername(targetUsername);
        ban.setBannedAt(LocalDateTime.now());
        chatRoomBanRepository.save(ban);
        // 9. 기존 채팅방 참여 정보 삭제
        chatRoomMemberRepository.delete(targetRoomMember);
        return "SUCCESS";
    }
    // 채팅방 추방 해제
    @Transactional
    public String unbanMember(
            Long roomId,
            String ownerUsername,
            String targetUsername) {

        // 1. 채팅방 조회 및 잠금
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElse(null);

        if (chatRoom == null) {
            return "ROOM_NOT_FOUND";
        }

        // 2. 요청한 회원이 방장인지 확인
        ChatRoomMember owner =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, ownerUsername)
                        .orElse(null);

        if (owner == null ||
                !Integer.valueOf(1).equals(owner.getMemberRole())) {
            return "NOT_OWNER";
        }

        // 3. 추방 기록 확인
        if (!chatRoomBanRepository.existsByRoomIdAndUsername(
                roomId, targetUsername)) {
            return "BAN_NOT_FOUND";
        }

        // 4. 추방 기록 삭제
        chatRoomBanRepository.deleteByRoomIdAndUsername(
                roomId, targetUsername
        );

        return "SUCCESS";
    }

    // 방장이 채팅방 전체 삭제
    @Transactional
    public String destroyChatRoom(Long roomId, String ownerUsername) {

        // 1. 채팅방 조회 및 잠금
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElse(null);

        if (chatRoom == null) {
            return "ROOM_NOT_FOUND";
        }

        // 2. 요청한 회원이 방장인지 확인
        ChatRoomMember owner =
                chatRoomMemberRepository
                        .findByRoomIdAndUsername(roomId, ownerUsername)
                        .orElse(null);

        if (owner == null ||
                !Integer.valueOf(1).equals(owner.getMemberRole())) {
            return "NOT_OWNER";
        }

        // 3. 채팅방 메시지 삭제
        chatMessageRepository.deleteByRoomId(roomId);
        chatMessageRepository.flush();

        // 4. 채팅방 참여 정보 삭제
        List<ChatRoomMember> members =
                chatRoomMemberRepository.findByRoomId(roomId);

        chatRoomMemberRepository.deleteAll(members);
        chatRoomMemberRepository.flush();

        // 5. 추방 기록 삭제
        chatRoomBanRepository.deleteByRoomId(roomId);
        chatRoomBanRepository.flush();

        // 6. 연결된 게시글 삭제
        Post post = chatRoom.getPost();
        if (post != null) {
            // 채팅방과 게시글 연결 해제
            chatRoom.setPost(null);
            chatRoomRepository.saveAndFlush(chatRoom);

            // 게시글 삭제
            postRepository.delete(post);
        }
        // 7. 채팅방 삭제
        chatRoomRepository.delete(chatRoom);
        return "SUCCESS";
    }

    // 활동 기록 저장
    private void saveActivityHistory(
            String username,
            Post post,
            ChatRoom chatRoom,
            LocalDateTime joinedAt) {

        // 1. 이미 같은 게시글 또는 채팅방 기록이 있는지 확인
        boolean alreadyExists =
                activityHistoryRepository.existsByUsernameAndPostId(
                        username, post.getId()
                ) ||
                        activityHistoryRepository.existsByUsernameAndRoomId(
                                username, chatRoom.getId()
                        );

        // 기존 기록이 있다면 중복 저장하지 않음
        if (alreadyExists) {
            return;
        }

        // 2. 활동 기록 생성
        ActivityHistory history = new ActivityHistory();

        history.setUsername(username);

        // 중복 확인용 ID
        history.setPostId(post.getId());
        history.setRoomId(chatRoom.getId());

        // 가입 당시 게시글 제목 복사
        history.setPostTitle(post.getTitle());

        // 가입 당시 주요 콘텐츠 복사
        String mainContent = post.getCategory();

        if (mainContent == null || mainContent.isBlank()) {
            mainContent = "무";
        }

        history.setMainContent(mainContent);

        // 가입 당시 세부 콘텐츠 복사
        List<String> hashtags = post.getHashtags();

        String detailContents = "무";

        if (hashtags != null && !hashtags.isEmpty()) {
            String joinedTags = String.join(", ", hashtags);

            if (!joinedTags.isBlank()) {
                detailContents = joinedTags;
            }
        }

        history.setDetailContents(detailContents);

        // 가입 당시 게시글 본문 복사
        history.setPostContent(post.getContent());

        // 실제 채팅방 가입 시간 저장
        history.setJoinedAt(joinedAt);

        // 데이터베이스 저장
        activityHistoryRepository.save(history);
    }

}