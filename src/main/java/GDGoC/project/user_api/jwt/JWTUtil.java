package GDGoC.project.user_api.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTUtil {

  private final SecretKey secretKey;

  // application.properties: spring.jwt.secret= <Base64 인코딩된 32바이트 이상 키>
  public JWTUtil(@Value("${spring.jwt.secret}") String secretBase64) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
  }

  public String getUsername(String token) {
    return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload().get("username", String.class);
  }

  // ✅ 새로 추가
  public String getName(String token) {
    return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload().get("name", String.class);
  }

  public String getRole(String token) {
    return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload().get("role", String.class);
  }

  public Boolean isExpired(String token) {
    Date exp = Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload().getExpiration();
    return exp.before(new Date());
  }

  // 기존 (username + role)
  public String createJwt(String username, String role, Long expiredMs) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date(now))
            .expiration(new Date(now + expiredMs))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
  }

  // ✅ 새로 추가 (username + name + role)
  public String createJwt(String username, String name, String role, Long expiredMs) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .claim("username", username)
            .claim("name", name)        // 이름 클레임 추가
            .claim("role", role)
            .issuedAt(new Date(now))
            .expiration(new Date(now + expiredMs))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
  }
}
