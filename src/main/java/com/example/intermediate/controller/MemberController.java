package com.example.intermediate.controller;

import com.example.intermediate.controller.request.LoginRequestDto;
import com.example.intermediate.controller.request.MemberRequestDto;
import com.example.intermediate.controller.request.TokenDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Member;
import com.example.intermediate.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;

    //회원가입
    @PostMapping("/join")
    public Member join(@Valid @RequestBody MemberRequestDto memberRequestDto){
        return memberService.joinMember(memberRequestDto);
    }

    //로그인
    @PostMapping("/login")
    public TokenDto login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpResponse){
        return memberService.login(loginRequestDto, httpResponse);
    }

    //로그아웃
    @GetMapping("/logout")
    public ResponseDto<?> logout(HttpServletRequest httpRequest) {
        return memberService.logout(httpRequest);
    }
}
