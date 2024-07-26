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
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

                //Authentication 객체 생성 ( 여기 안에 사용자 이메일, 롤 이런거 들어있음)
                // userDetail 객체도 필요
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                // .getSubject가 email을 말함, 마지막은 권한을 넣는 것
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

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
