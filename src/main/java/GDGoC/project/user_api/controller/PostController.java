package GDGoC.project.user_api.controller;

import GDGoC.project.user_api.dto.PostRequest;
import GDGoC.project.user_api.dto.PostDto;
import GDGoC.project.user_api.entity.Post;
import GDGoC.project.user_api.entity.User;
import GDGoC.project.user_api.service.PostService;
import GDGoC.project.user_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

  private final PostService postService;
  private final UserService userService;

  @GetMapping
  public List<PostDto> getList() {
    System.out.println("PostController.getList()");
    return postService.getList().stream()
            .map(PostDto::from)
            .toList();
  }

  /** 게시글 조회 */
  @GetMapping("/{id}")
  public PostDto getDetail(@PathVariable Integer id) {
    return PostDto.from(postService.getPost(id));
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<PostDto> create(@Valid @RequestBody PostRequest req,
                                        Principal principal) {

    User user = userService.getUser(principal.getName());
    Post post = postService.createPost(req.content(), user);

    return ResponseEntity
            .created(URI.create("/api/posts/" + post.getId())) // 201 + Location
            .body(PostDto.from(post));
  }

  /** 게시글 수정 */
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}")
  public PostDto modify(@PathVariable Integer id,
                        @Valid @RequestBody PostRequest req,
                        Principal principal) {

    Post post = postService.getPost(id);
    authorize(principal, post);                 // 수정 권한 체크
    postService.modifyPost(post, req.content());
    return PostDto.from(post);
  }

  /** 게시글 삭제 */
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id,
                                     Principal principal) {
    Post post = postService.getPost(id);
    authorize(principal, post);
    postService.deletePost(post);
    return ResponseEntity.noContent().build();  // 204
  }

  /** 좋아요 */
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{id}/like")
  public PostDto like(@PathVariable Integer id, Principal principal) {
    Post post = postService.getPost(id);
    User user = userService.getUser(principal.getName());
    postService.toggleLike(post, user);
    return PostDto.from(post);
  }

  /** 좋아요 취소 */
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{id}/like")
  public PostDto cancelLike(@PathVariable Integer id, Principal principal) {
    Post post = postService.getPost(id);
    User user = userService.getUser(principal.getName());
    postService.toggleLike(post, user);
    return PostDto.from(post);
  }

  /** 내부 권한 체크 메서드 */
  private void authorize(Principal principal, Post post) {
    if (!post.getAuthor().getUsername().equals(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
    }
  }
}