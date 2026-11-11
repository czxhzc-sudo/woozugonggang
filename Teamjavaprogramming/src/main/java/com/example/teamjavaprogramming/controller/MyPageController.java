package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class MyPageController {
    private final MemberService memberService;
    public MyPageController(MemberService memberService){
        this.memberService = memberService;
    }
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model){
        String userName = (String)session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if(member.isPresent()){
            model.addAttribute("member",member.get());
        }
        return "MyPage";
    }
    // 회원정보 수정 페이지
    @GetMapping("/mypage/edit")
    public String editForm(HttpSession session, Model model){
        String userName = (String) session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if (member.isPresent()){
            model.addAttribute("member",member.get());
        }
        else {
            return "redirect:/member/login";
        }
        return "MyPageEdit";
    }
    // 회원정보 수정 처리
    @PostMapping("/mypage/edit")
    public String edit(
            @RequestParam String stdName,
            @RequestParam String stdGender,
            @RequestParam LocalDate stdBirth,
            @RequestParam String major,
            @RequestParam(required = false, defaultValue = "") String password,
            @RequestParam(required = false, defaultValue = "") String passwordConfirm,
            @RequestParam(required = false, defaultValue = "") String stdCon,
            @RequestParam(required = false, defaultValue = "") String stdCategory,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ){
        String userName = (String) session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if (member.isEmpty()) {
            return "redirect:/member/login";
        }
        MemberSet currentMember = member.get();
        // 비밀번호를 새로 입력한 경우에만 변경
        if (!password.isBlank()) {
            // 회원가입과 동일한 비밀번호 규칙
            Pattern passwordPattern = Pattern.compile(
                    "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]+$"
            );
            if (!passwordPattern.matcher(password).matches()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "비밀번호는 영문, 숫자, 특수문자를 각각 포함해야 합니다."
                );
                return "redirect:/mypage/edit";
            }
            if (!password.equals(passwordConfirm)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."
                );
                return "redirect:/mypage/edit";
            }
            currentMember.setPassword(password);

        } else if (!passwordConfirm.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "새 비밀번호를 입력해 주세요."
            );
            return "redirect:/mypage/edit";
        }

        // 관심 분야 최대 5개 검사
        if (!stdCon.isBlank()) {
            String[] selectedContents = stdCon.split(",");

            if (selectedContents.length > 5) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "관심 분야는 최대 5개까지 선택할 수 있습니다."
                );
                return "redirect:/mypage/edit";
            }
        }
        // 회원정보 수정
        currentMember.setStdName(stdName);
        currentMember.setStdGender(stdGender);
        currentMember.setStdBirth(stdBirth);
        currentMember.setMajor(major);
        currentMember.setStdCon(stdCon);
        currentMember.setStdCategory(stdCategory);
        memberService.update(currentMember);
        return "redirect:/mypage";
    }
}
