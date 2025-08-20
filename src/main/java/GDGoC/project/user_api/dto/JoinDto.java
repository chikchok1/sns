package GDGoC.project.user_api.dto;

import GDGoC.project.user_api.entity.User;

public record JoinDto(
        String username,
        String password,
        String name,
        String phone
) {
  public static JoinDto from(User u) {
    return new JoinDto(u.getUsername(), u.getPassword(), u.getPhone(), u.getRole());
  }
}
