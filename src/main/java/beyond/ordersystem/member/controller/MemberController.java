package beyond.ordersystem.member.controller;

import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.MemberCreateReqDto;
import beyond.ordersystem.member.dto.MemberListResDto;
import beyond.ordersystem.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    @Autowired
    public MemberController (MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 회원 가입
     */
    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberCreateReqDto dto) {

//        try {
            log.info("controller: " + dto.getEmail());
            Member member = memberService.createMember(dto);

            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member created successfully", member.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
//        } catch (IllegalArgumentException e) {
//
//            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
//            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
//        }
    }


    /**
     * 회원 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<?> memberList(@PageableDefault(size=10, sort = "createdTime"
            , direction = Sort.Direction.DESC) Pageable pageable) {

//        try {
            Page<MemberListResDto> memberListResDtos = memberService.memberList(pageable);

            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member are found", memberListResDtos);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//        } catch (IllegalArgumentException e) {
//
//            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
//            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
//        }
    }



}
