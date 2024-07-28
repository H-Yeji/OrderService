package beyond.ordersystem.common.auth;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * jwt 토큰 (json web tokens)
 */
@Component
@Slf4j
public class JwtAuthFilter extends GenericFilter {

    /**
     * yml에 추가함
     */
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private int expiration;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String bearerToken = ((HttpServletRequest) request).getHeader("Authorization");

        try {
            if (bearerToken != null) {
                // 토큰 있으면 처리, 없으면 알아서 에러
                // token 관례적으로 Bearer로 시작하는 문구를 넣어서 요청
                if (!bearerToken.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }
                String token = bearerToken.substring(7);
                // token 검증 및 claims(사용자 정보) 추출
                // token 생성시에 사용한 secret 키 값을 넣어 토큰 검증에 사용
                // secretKey를 사용해 jwt 토큰을 검증하고, 토큰이 유효한지 확인
                // -> 유효한 토큰이면 토킅에서 사용자 정보인 claims를 추출
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

                //Authentication 객체 생성 ( 여기 안에 사용자 이메일, 롤 이런거 들어있음)
                // 사용자 권한을 담는 authorities 리스트 생성 > claims에서 추출한 역할(role)를 기반으로 권한 추가
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                // .getSubject가 email을 말함 > claims.getSubject로 사용자 이메일 가져와서 UserDetails 객체 생성
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                // userDetail 객체 기반으로 UsernamePasswordAuthenticationToken 생성해서 Authentication 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

                // SecurityContextHolder를 사용해 현재 보안 컨텍스트에 인증 정보 전달
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 이렇게 인증 정보까지 전달하면 -> 애플리케이션은 이후 요청에서 해당 사용자가 "인증된 사용자"임을 확인할 수 있음 ⭐
            }
            // filterChain에서 그 다음 filtering으로 넘어가도록 하는 메서드
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("token error");
        }

    }
}
