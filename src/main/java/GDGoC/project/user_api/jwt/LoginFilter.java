package GDGoC.project.user_api.jwt;

import GDGoC.project.user_api.dto.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;

  public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    // 프론트가 호출하는 로그인 엔드포인트와 일치시킴
    setFilterProcessesUrl("/api/auth/login");
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
          throws AuthenticationException {
    // form-urlencoded 기준
    String username = obtainUsername(request);
    String password = obtainPassword(request);

    UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(username, password);

    return authenticationManager.authenticate(authToken);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain chain,
                                          Authentication authentication) throws IOException, ServletException {

    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

    String username = principal.getUsername();      // 아이디
    String name     = principal.getName();          // ✅ 이름 (CustomUserDetails에 getName() 추가해둔 것)
    String role     = authentication.getAuthorities()
            .iterator().next().getAuthority();

    // ✅ 이름(name) 클레임까지 포함하여 발급 (JWTUtil도 같은 시그니처로 수정되어 있어야 함)
    String token = jwtUtil.createJwt(username, name, role, 1000L * 60 * 60);

    // 헤더로 전달
    response.setHeader("Authorization", "Bearer " + token);

    // 바디로도 안전하게 내려줌 (프론트가 헤더/바디 어느 쪽이든 처리 가능)
    response.setContentType("application/json;charset=UTF-8");
    Map<String, Object> body = new HashMap<>();
    body.put("token", "Bearer " + token);
    body.put("username", username);
    body.put("name", name);                         // ✅ 이름 함께 내려줌
    new ObjectMapper().writeValue(response.getWriter(), body);
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException failed) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
