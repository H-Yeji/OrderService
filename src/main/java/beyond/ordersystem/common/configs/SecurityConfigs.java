package beyond.ordersystem.common.configs;

import beyond.ordersystem.common.auth.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity /// 이 코드가 security 관련 코드닷
@EnableGlobalMethodSecurity(prePostEnabled = true) // 사전 검증 하겠다 (로그인)
public class SecurityConfigs {

    /**
     * JwtAuthFilter 주입
     */
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    /**
     * 검증 코드
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf().disable()
                // cors 활성화 : 다른 도메인에서 서버로 호출하는 것을 금지 (같은 도메인끼리만 통신 가능)
                .cors().and()
                .httpBasic().disable()
                .authorizeRequests()
                    .antMatchers("/member/create", "/", "/member/doLogin", "/member/refresh-token")
                    .permitAll()
                .anyRequest().authenticated()
                .and()
                // 세션 로그인이 아닌 stateless한 token을 사용하겠다는 의미
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 로그인시 사용자는 서버로부터 토큰을 발급받고, 매 요청마다 해당 토큰을 http header에 넣어 요청
                // 아래 코드는 사용자로부터 받아온 토큰이 정상인지 아닌지를 검증하는 코드 ⭐
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
//        return null;
    }

}
