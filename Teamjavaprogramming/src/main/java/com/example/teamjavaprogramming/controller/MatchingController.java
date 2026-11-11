package com.example.teamjavaprogramming.controller;
import com.example.teamjavaprogramming.MatchingRequest;
import com.example.teamjavaprogramming.service.MatchingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }
    // 빠른 매칭 신청
    @PostMapping("/start")
    @ResponseBody
    public MatchingRequest startMatching(
            @RequestParam String targetGender,
            @RequestParam String targetMajor,
            @RequestParam(required = false) Integer minStudentYear,
            @RequestParam(required = false) Integer maxStudentYear,
            @RequestParam(required = false) String targetContents,
            HttpSession session
    ) {
        // 로그인한 사용자 확인
        String username =
                (String) session.getAttribute("userName");

        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        // 매칭 시작
        return matchingService.startMatching(
                username,
                targetGender,
                targetMajor,
                minStudentYear,
                maxStudentYear,
                targetContents
        );
    }
    @GetMapping("/status")
    @ResponseBody
    public MatchingRequest getMatchingStatus(HttpSession session) {

        String username = (String) session.getAttribute("userName");

        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        return matchingService.getMatchingStatus(username);
    }
    // 빠른 매칭 취소
    @PostMapping("/cancel")
    @ResponseBody
    public MatchingRequest cancelMatching(HttpSession session) {

        String username =
                (String) session.getAttribute("userName");

        if (username == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        return matchingService.cancelMatching(username);
    }
}