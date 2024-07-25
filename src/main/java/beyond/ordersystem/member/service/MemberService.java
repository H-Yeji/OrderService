package beyond.ordersystem.member.service;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.MemberCreateReqDto;
import beyond.ordersystem.member.dto.MemberListResDto;
import beyond.ordersystem.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    // 비밀번호 암호화
    //private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService (MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        //this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입
     */
    @Transactional
    public Member createMember(MemberCreateReqDto dto) {

        // 이메일 중복 확인
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        log.info("이메일: " + dto.getEmail());
        // 비밀번호 길이 확인
        if (dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("비밀번호의 길이가 짧습니다.");
        }
        log.info("password : " + dto.getPassword());

        //Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        Member member = dto.toEntity();
        log.info("찾아온 멤버 : " + member);

        Member savedMember = memberRepository.save(member);

        return savedMember;
    }

    /**
     * 회원 목록 조회
     */
    public Page<MemberListResDto> memberList(Pageable pageable) {
        Page<Member> memberList = memberRepository.findAll(pageable);

        Page<MemberListResDto> memberListResDtos = memberList.map(a->a.listFromEntity());
//        for (Member member: memberList) {
//            memberListResDtos.add(member.listfromEntity());
//        }
        return memberListResDtos;
    }
}
