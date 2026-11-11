package com.example.teamjavaprogramming.controller;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.example.teamjavaprogramming.Post;
import com.example.teamjavaprogramming.service.PostService;
import com.example.teamjavaprogramming.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.ChatRoom;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import java.util.HashSet;
import java.util.Set;

@Controller
public class MemberController {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PostService postService;
    private final MemberService memberService;
    public MemberController(MemberService memberService,
                            PostService postService,
                            ChatRoomMemberRepository chatRoomMemberRepository,
                            ChatRoomRepository chatRoomRepository,
                            ChatMessageRepository chatMessageRepository) {
        this.memberService = memberService;
        this.postService = postService;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }
    @GetMapping("/member/join")// 회원가입 화면
    public String joinForm() {
        return "Join";
    }
    @PostMapping("/member/join") // 가입 처리
    public String join(MemberSet member,
                       @RequestParam(required = false) String stdTime) {
        //회원 정보 저장
        MemberSet savedMember = memberService.join(member);
        memberService.saveTimetable(savedMember, stdTime);
        return "redirect:/member/login";
    }
    @GetMapping("/member/login")
    public String loginForm() {
        return "Login";
    }

    // 로그인 화면
    @PostMapping("/member/login")
    public String login(String userName,
                        String password,
                        Model model,
                        HttpSession session) {
        boolean result = memberService.login(userName,password);
        if(result){
            session.setAttribute("userName",userName);
            return "redirect:/main";
        }
        model.addAttribute("loginError","아이디 또는 비밀번호가 올바르지 않습니다.");
        return "Login";
    }
    //메인화면
    @GetMapping("/main")
    public String main(HttpSession session, Model model) {

        String userName = (String) session.getAttribute("userName");

        if (userName == null) {
            return "redirect:/member/login";
        }

        Optional<MemberSet> member =
                memberService.findByUserName(userName);

        if (member.isPresent()) {
            MemberSet currentMember = member.get();
            model.addAttribute("member", currentMember);
            // 회원가입 때 선택한 관심 콘텐츠
            model.addAttribute("stdCon", currentMember.getStdCon());
            //새 시간표 테이블에서 회원의 시간표 가져옴
            List<String> memberTimeList =
                    memberService.findTimetableByMemberId(currentMember.getId())
                            .stream()
                            .map(t -> t.getDay_of_week() + t.getSlot_index())
                            .toList();
            model.addAttribute("memberTimeList", memberTimeList);
        }

        // 모든 게시글 가져오기
        List<Post> posts = postService.findAllPosts();

        // 내가 가입한 채팅방 목록
        List<ChatRoomMember> members =
                chatRoomMemberRepository.findByUsername(userName);
        // 전체 안 읽은 메시지 수
        long totalUnreadCount = 0;
        // 내가 가입한 모든 채팅방 확인
        for (ChatRoomMember memberInfo : members) {
            // 마지막으로 읽은 시간
            LocalDateTime lastReadAt = memberInfo.getLastReadAt();
            // 기존 회원의 읽은 시간이 없으면 가입 시간 사용
            if (lastReadAt == null) {
                lastReadAt = memberInfo.getJoinedAt();
            }
            // 읽은 시간 정보가 있을 때만 계산
            if (lastReadAt != null) {
                long count = chatMessageRepository
                        .countByRoomIdAndSentAtAfterAndUsernameNot(
                                memberInfo.getRoomId(),
                                lastReadAt,
                                userName
                        );
                totalUnreadCount += count;
            }
        }
        // HTML로 전체 안 읽은 메시지 수 전달
        model.addAttribute("totalUnreadCount", totalUnreadCount);
        // 내가 가입한 게시글 ID 저장
        Set<Long> joinedPostIds = new HashSet<>();

        for (ChatRoomMember memberInfo : members) {

            ChatRoom chatRoom =
                    chatRoomRepository.findById(memberInfo.getRoomId())
                            .orElse(null);

            if (chatRoom != null && chatRoom.getPost() != null) {
                joinedPostIds.add(chatRoom.getPost().getId());
            }
        }

        // 내가 가입한 게시글은 제외
        posts.removeIf(post ->
                joinedPostIds.contains(post.getId())
                        && !post.getAuthor().equals(member.get().getStdName())
        );

        model.addAttribute("posts", posts);

        return "Main";
    }
    // 아이디 중복 확인
    @GetMapping("/member/check-username")
    @ResponseBody
    public boolean checkUserName(@RequestParam String userName){
        return memberService.checkUserName(userName);
    }

    //로그아웃 session삭제
    @GetMapping("/member/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/member/login";
    }
    // 시간표 화면
    @GetMapping("/timetable")
    public String timetable(HttpSession session, Model model) {

        String userName =
                (String) session.getAttribute("userName");
        if (userName == null) {
            return "redirect:/member/login";
        }
        Optional<MemberSet> member =
                memberService.findByUserName(userName);
        if (member.isPresent()) {
            MemberSet currentMember = member.get();
            model.addAttribute("member", currentMember);
            //시간표 테이블에서 회원의 시간표 조회
            model.addAttribute("timetableList",
                    memberService.findTimetableByMemberId(currentMember.getId()
                    )
            );
        }
        return "Timetable";
    }
    @PostMapping("/timetable/save") // 시간표 저장 및 수정
    public String timetableEdit(@RequestParam String stdTime,
                                HttpSession session) {
        String userName = (String) session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if(member.isPresent()){
            MemberSet currentMember = member.get();
            //시간표 테이블에 저장
            memberService.saveTimetable(currentMember,stdTime);
        }
        return "redirect:/timetable";
    }
    // 내가 작성한 게시글
    @GetMapping("/my-posts")
    public String myPosts(HttpSession session, Model model) {

        String userName = (String) session.getAttribute("userName");

        if (userName == null) {
            return "redirect:/member/login";
        }
        Optional<MemberSet> member =
                memberService.findByUserName(userName);
        if (member.isEmpty()) {
            return "redirect:/member/login";
        }

        MemberSet currentMember = member.get();

        // 회원 정보
        model.addAttribute("member", currentMember);

        // 관심 콘텐츠
        model.addAttribute("stdCon", currentMember.getStdCon());

        // 시간표
        List<String> memberTimeList =
                memberService.findTimetableByMemberId(currentMember.getId())
                        .stream()
                        .map(t -> t.getDay_of_week() + t.getSlot_index())
                        .toList();
        model.addAttribute("memberTimeList", memberTimeList);
        // 내가 작성한 게시글만 가져오기
        List<Post> myPosts =
                postService.findMyPosts(currentMember.getStdName());
        model.addAttribute("posts", myPosts);
        return "Main";
    }
}
