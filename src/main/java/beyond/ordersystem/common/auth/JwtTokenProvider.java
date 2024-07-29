package beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 토큰 만들기
 */
@Component
public class JwtTokenProvider {

    /**
     * yml에 추가함
     */
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;


    public String createToken(String email, String role) {

        // claims 만들기 (claims : 사용자 정보이자 payload에 들어갈 정보)
        Claims claims = Jwts.claims().setSubject(email); // setSubject : email로 세팅
        claims.put("role", role);

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 생성 시간
                .setExpiration(new Date(now.getTime() + expiration*60*1000L)) // 만료 시간 (생성시간 + 30분)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return token;
    }
    public String createRefreshToken(String email, String role) {

        // claims 만들기 (claims : 사용자 정보이자 payload에 들어갈 정보)
        Claims claims = Jwts.claims().setSubject(email); // setSubject : email로 세팅
        claims.put("role", role);

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 생성 시간
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) // 만료 시간 (생성시간 + 30분)
                .signWith(SignatureAlgorithm.HS256, secretKeyRt) // rt로 생성
                .compact();

        return token;
    }
}
