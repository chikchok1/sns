package GDGoC.project.user_api.config;

import GDGoC.project.user_api.jwt.JWTFilter;
import GDGoC.project.user_api.jwt.JWTUtil;
import GDGoC.project.user_api.jwt.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

/**
 * @Configuration : 이 파일이 스프링의 환경 설정 파일임을 의미하는 애너테이션
 * @EnableWebSecurity : 모든 요청 URL이 스프링 시큐리티의 제어를 받도록 만드는 애너테이션. 내부적으로 SecurityFilterChain 클래스가 동작하여 모든 요청 URL에 이 클래스가 필터로 적용되어 URL별로 특별한 설정을 할 수 있게 된다.
 * @EnableMethodSecurity 애너테이션의 prePostEnabled = true는 PostController와 CommentController에서 로그인 여부를 판별할 때 사용한 @PreAuthorize 애너테이션을 사용하기 위해 반드시 필요한 설정
 */
@Configuration
@EnableWebSecurity // Spring Security를 활성화
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,  JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var cfg = new CorsConfiguration();
                    cfg.setAllowedOrigins(java.util.List.of("http://localhost:5500","http://127.0.0.1:5500")); // ✅ 프론트 도메인
                    cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
                    cfg.setAllowedHeaders(java.util.List.of("*"));
                    cfg.setAllowCredentials(true);
                    cfg.setExposedHeaders(java.util.List.of("Authorization")); // ✅ 토큰 헤더 노출
                    cfg.setMaxAge(3600L);
                    return cfg;
                }))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ 로그인/회원가입은 누구나 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/join").permitAll()               // ← 추가

                        // (기존 허용 경로 유지)
                        .requestMatchers("/", "/swagger", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // ✅ LoginFilter가 /api/auth/login 처리하도록 URL 지정
        var loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/auth/login");

        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}