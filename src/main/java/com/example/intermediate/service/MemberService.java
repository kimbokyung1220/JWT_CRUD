package com.example.intermediate.service;

import com.example.intermediate.controller.request.LoginRequestDto;
import com.example.intermediate.controller.request.MemberRequestDto;
import com.example.intermediate.controller.request.TokenDto;
import com.example.intermediate.controller.response.MemberResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Member;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // 회원 조회
    @Transactional(readOnly = true)
    public Member isPresentMember(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        return member.orElse(null);
    }

    // 토큰 헤더
    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("Refresh-Token", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }

    // 회원 가입
    @Transactional
    public Member joinMember(MemberRequestDto memberRequestDto) {
        // 1. 회원정보를 select => 회원정보가 있다면 false
        if (isPresentMember(memberRequestDto.getEmail()) != null) {
            throw new IllegalArgumentException("중복된 사용자 ID 가 존재합니다.");
        }

        // 2. 비밀번호, 비밀번호 확인
        if (!memberRequestDto.getPassword().equals(memberRequestDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호확인이 일치하지 않습니다.");
        }

        Member memberInfo = Member.builder()
                .email(memberRequestDto.getEmail())
                .name(memberRequestDto.getName())
                .password(passwordEncoder.encode(memberRequestDto.getPassword()))
                .build();
        memberRepository.save(memberInfo);
        return memberInfo;
    }

    // 로그인 String => MemberResponseDto로 변경필요
    @Transactional
    public TokenDto login(LoginRequestDto loginRequestDto, HttpServletResponse httpResponse) {
        // 1. 회원 아이디가 존재하는지 확인한다.
        Member memberInfo = isPresentMember(loginRequestDto.getEmail());
        if (memberInfo == null) {
            throw new IllegalArgumentException("사용자ID가 존재하지 않습니다.");
        }
        // 2. 비밀번호가 일치하는지 확인한다.
        if (!memberInfo.validatePassword(passwordEncoder, loginRequestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        // 3. 정보가 일치하면 토큰값을 부여한다. [accessToken / refreshToken]
        TokenDto tokenDto = tokenProvider.generateTokenDto(memberInfo);
        tokenToHeaders(tokenDto, httpResponse);

        MemberResponseDto loginInfo = MemberResponseDto.builder()
                .id(memberInfo.getId())
                .email(memberInfo.getName())
                .createdAt(memberInfo.getCreatedAt())
                .modifiedAt(memberInfo.getModifiedAt())
                .build();

        TokenDto token = TokenDto.builder()
                .accessToken("Bearer " + tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .build();

        return token;
    }

    // 로그아웃
    public ResponseDto<?> logout(HttpServletRequest httpRequest) {
        if(!tokenProvider.validateToken(httpRequest.getHeader("Refresh-Token"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        Member memberToken = tokenProvider.getMemberFromAuthentication();
        if(memberToken == null) {
            return ResponseDto.fail("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }
        return tokenProvider.deleteRefreshToken(memberToken);
    }
}
