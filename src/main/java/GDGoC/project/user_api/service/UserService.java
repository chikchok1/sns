package GDGoC.project.user_api.service;

import GDGoC.project.user_api.entity.User;
import GDGoC.project.user_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /* userId 로 조회 (로그인 세션용) */
  @Transactional(Transactional.TxType.SUPPORTS)
  public User getUser(String username) {
    System.out.println("userService.getUser");
    return userRepository.findByUsername(username);
  }

  /* id 로 조회 (공개 프로필용) */
  @Transactional(Transactional.TxType.SUPPORTS)
  public Optional<User> getById(Integer id) {
    return userRepository.findById(id);
  }

  /* ★ 프로필 수정: 이름/전화번호 DB 반영 */
  public User updateProfile(String username, String name, String phone) {
    User user = Optional.ofNullable(userRepository.findByUsername(username))
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    // 이름: 공백/빈 문자열은 무시(프론트에서 빈값 방지해도 한번 더 보호)
    if (name != null && !name.isBlank()) {
      user.setName(name.trim());
    }
    // 전화번호: 빈 문자열도 허용(지우고 저장 가능하도록). null이면 변경 안 함
    if (phone != null) {
      user.setPhone(phone.trim());
    }

    return userRepository.save(user);
  }
}
