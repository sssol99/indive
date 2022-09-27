package com.ssafy.indive.domain.member.service;

import com.ssafy.indive.domain.member.entity.Member;
import com.ssafy.indive.domain.member.repository.MemberRepository;
import com.ssafy.indive.domain.member.service.dto.ServiceMemberAddRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberAddService {

    private final MemberRepository memberRepository;

    public boolean addMember(ServiceMemberAddRequestDto dto) {

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        Member member = Member.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .wallet(dto.getWallet())
                .profileMessage(dto.getProfileMessage())
                .build();


        //TODO : 프로필 사진 추가해야 합니다

        memberRepository.save(member);

        return true;
    }


}