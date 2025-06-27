package lock.prac.Lock_Practice.global.config;

import lock.prac.Lock_Practice.domain.user.repository.UserRepository;
import lock.prac.Lock_Practice.global.jwt.JwtAuthenticationFilter;
import lock.prac.Lock_Practice.global.jwt.JwtUtil;
//import lock.prac.Lock_Practice.global.oauth.handler.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
//    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final UserRepository userRepository;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))  // CORS 설정 활성화
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/api/health",  "/auth/login/kakao/**").permitAll() // 인증 없이 접근 허용
                        .requestMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll() // 정적 파일 허용
                        .requestMatchers("/**").permitAll()  // 모든 요청 허용 (테스트용)
                )
//                .oauth2Login(oauth2 -> oauth2
//                        .successHandler(oAuth2LoginSuccessHandler) // OAuth2 로그인 성공 핸들러 추가
//                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}