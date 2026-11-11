package com.example.teamjavaprogramming.controller;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.Post;
import com.example.teamjavaprogramming.service.MemberService;
import com.example.teamjavaprogramming.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class PostController {
    private final MemberService memberService;
    private final PostService postService;
    public PostController(PostService postService,
                          MemberService memberService) {
        this.postService = postService;
        this.memberService = memberService;
    }
    @GetMapping("/post/create")
    public String createForm(HttpSession session, Model model) {
        String userName =
                (String) session.getAttribute("userName");
        Optional<MemberSet> member =
                memberService.findByUserName(userName);
        if (member.isPresent()) {
            model.addAttribute("gender",
                    member.get().getStdGender());
            model.addAttribute(
                    "major",
                    member.get().getMajor()
            );
        }

        return "PostCreate";
    }

    @PostMapping("/post/create")
    public String createPost(Post post, HttpSession session) {
        String userName = (String) session.getAttribute("userName");
        // 로그인 여부 확인
        if (userName == null) {
            return "redirect:/login";
        }
        // 회원 정보 조회
        MemberSet member = memberService.findByUserName(userName)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));
        // 작성자 자동 설정
        post.setAuthor(member.getStdName());
        // 게시글 등록
        postService.createPost(post, userName);
        return "redirect:/main";
    }

    @PostMapping("/post/{postId}/join")
    @ResponseBody
    public String joinPost(@PathVariable Long postId,
                           HttpSession session) {
        String userName =
                (String) session.getAttribute("userName");
        String result =
                postService.joinPost(postId, userName);
        // 가입됬는지 판단
        if ("SUCCESS".equals(result)) {
            return "가입되었습니다.";

        } else if ("ALREADY_JOINED".equals(result)) {
            return "이미 가입한 모임입니다.";

        } else if ("FULL".equals(result)) {
            return "모집 인원이 모두 찼습니다.";

        } else if ("MALE_FULL".equals(result)) {
            return "남성 모집 인원이 모두 찼습니다.";

        } else if ("FEMALE_FULL".equals(result)) {
            return "여성 모집 인원이 모두 찼습니다.";
        }
        return "가입할 수 없습니다.";
    }
    // 게시글 삭제
    @PostMapping("/post/{postId}/delete")
    public String deletePost(@PathVariable Long postId,
                             HttpSession session) {

        String userName = (String) session.getAttribute("userName");

        // 로그인 확인
        if (userName == null) {
            return "redirect:/login";
        }
        // 게시글 삭제
        boolean result = postService.deletePost(postId, userName);
        if (!result) {
            return "redirect:/main?deleteError";
        }
        return "redirect:/main";
    }
}