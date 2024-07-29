package beyond.ordersystem.member.controller;

import beyond.ordersystem.common.auth.JwtTokenProvider;
import beyond.ordersystem.common.dto.CommonErrorDto;
import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.dto.MemberCreateReqDto;
import beyond.ordersystem.member.dto.MemberRefreshDto;
import beyond.ordersystem.member.dto.MemberResDto;
import beyond.ordersystem.member.dto.MemberLoginDto;
import beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController {

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;
    //private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MemberController (@Qualifier("2") RedisTemplate<String, Object> redisTemplate,
                             MemberService memberService,
                             JwtTokenProvider jwtTokenProvider) {
        this.redisTemplate = redisTemplate;
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
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
     * => admin만 회원 목록 조회 가능
     */
    @PreAuthorize("hasRole('ADMIN')") // admin만 조회하도록
    @GetMapping("/list")
    public ResponseEntity<?> memberList(@PageableDefault(size=10, sort = "createdTime"
            , direction = Sort.Direction.DESC) Pageable pageable) {

//        try {
            Page<MemberResDto> memberListResDtos = memberService.memberList(pageable);

            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member are found", memberListResDtos);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//        } catch (IllegalArgumentException e) {
//
//            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
//            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
//        }
    }

    /**
     * 본인은 본인 회원 정보만 조회 가능
     */
    @GetMapping("/myinfo")
    public MemberResDto myInfo() {

        MemberResDto myInfo = memberService.findMyInfo();
        return myInfo;
    }


    /**
     * 로그인
     */
    @PostMapping("/doLogin")
    public ResponseEntity doLogin(@RequestBody MemberLoginDto dto) {

        // email, password가 일치한지 검증
        Member member = memberService.login(dto);

        // 일치할 경우 accessToken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        // refreshToken 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());
        // redis에 email과 rt를 key:value로하여 저장 - 시간 : 240시간
        // 로그인 했을 때 rt가 만들어지는데, 그 rt가 redis에 잘 들어가는지 확인 (rt는 redis에 저장)
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS);

        // 생성된 토큰을 commonResDto에 담아 사용자에게 return
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "login is successful", loginInfo);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    /**
     * 새로운 토큰 요청
     */
    @PostMapping("/refresh-token")
    // rt는 body에 담아서 보내옴 -> reqeustBody
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto) {

        // refreshToken은 로그인 시점에서 생성되기 때문에 doLogin으로 고고 -> rt 생성해놓기
        String rt = dto.getRefreshToken(); // token 꺼내기
        Claims claims = null;
        try {
            // 코드를 통해 rt 검증
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value(), "invalid refresh token"),
                    HttpStatus.UNAUTHORIZED);
        }

        // accessToken 새로 만들기
        String email = claims.getSubject(); // 이메일 꺼내기
        String role = claims.get("role").toString(); // role 꺼내기

        // redis를 조회하여 rt 추가 검증
        Object obj = redisTemplate.opsForValue().get(email);
        log.info("stored RedisRt: " + obj);
        log.info("equal관계 확인 : " + obj.equals(rt));

        // rt가 비어있거나 같지 않으면 에러 터뜨려
        if (obj == null || !obj.toString().equals(rt)) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value(), "invalid refresh token"),
                    HttpStatus.UNAUTHORIZED);
        }

        String newAt = jwtTokenProvider.createToken(email, role); // 새로 at를 받아야하니까 createToken
        // 생성된 토큰을 commonResDto에 담아 사용자에게 return
        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "at is renewed", info);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }



}
