package GDGoC.project.user_api.controller;

import GDGoC.project.user_api.entity.User;
import GDGoC.project.user_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 간단 DTO
    public record ProfileDto(Integer id, String username, String name, String phone) {
        public static ProfileDto from(User u) {
            return new ProfileDto(u.getId(), u.getUsername(), u.getName(), u.getPhone());
        }
    }
    public record UpdateProfileRequest(String name, String phone) {}

    /** 내 프로필 조회 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ProfileDto me(Principal principal) {
        User u = userService.getUser(principal.getName());
        if (u == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        return ProfileDto.from(u);
    }

    /** 내 프로필 수정 (이름/전화번호) */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ProfileDto updateMe(@RequestBody UpdateProfileRequest req, Principal principal) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        User updated = userService.updateProfile(principal.getName(), req.name(), req.phone());
        return ProfileDto.from(updated);
    }
}
