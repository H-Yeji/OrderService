package beyond.ordersystem.member.service;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.MemberCreateReqDto;
import beyond.ordersystem.member.dto.MemberResDto;
import beyond.ordersystem.member.dto.MemberLoginDto;
import beyond.ordersystem.member.dto.MemberUpdatePwdDto;
import beyond.ordersystem.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.transaction.TransactionScoped;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    // 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService (MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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

        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
//        Member member = dto.toEntity();
        log.info("찾아온 멤버 : " + member);

        Member savedMember = memberRepository.save(member);

        return savedMember;
    }

    /**
     * 회원 목록 조회
     */
    public Page<MemberResDto> memberList(Pageable pageable) {
        Page<Member> memberList = memberRepository.findAll(pageable);

        Page<MemberResDto> memberListResDtos = memberList.map(a->a.listFromEntity());
//        for (Member member: memberList) {
//            memberListResDtos.add(member.listfromEntity());
//        }
        return memberListResDtos;
    }


    /**
     * 내 정보 조회
     */
    public MemberResDto findMyInfo() {

        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member myMember = memberRepository.findByEmail(myEmail).orElseThrow(
                () -> new EntityNotFoundException("no email")
        );
        String myName = myMember.getName();
        Address myAddress = myMember.getAddress();

        return myMember.myDetailFromEntity(myEmail, myName, myAddress);
    }


    /**
     * 로그인
     */
    public Member login(MemberLoginDto dto) {

        // email의 존재 여부 확인
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 이메일입니다.")
        );

        // password 일치 여부 확인 => dto에서 가져온 비밀번호를 인코딩해서 들어간 비밀번호와 비교
        // 그냥 dto.getPassword는 암호화가 안된 상태이고, member.getPassword는 인코더돼서 db에 있음
        // 두개를 비교하기 위해 dto의 비번을 인코딩함
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    /**
     * 회원 비밀번호 수정
     */
    @Transactional
    public void updatePassword(MemberUpdatePwdDto dto) {

        Member findMember = memberRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("해당 이메일의 회원이 없음")
        );
        // 이메일 찾아와서 작성한 비밀번호와 기존의 비밀번호가 같은지 비교
        if (!passwordEncoder.matches(dto.getAsIsPassword(), findMember.getPassword())) { //⭐ matches하는 순서 주의
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        findMember.updateFromEntity(passwordEncoder.encode(dto.getToBePassword()));
    }
}
