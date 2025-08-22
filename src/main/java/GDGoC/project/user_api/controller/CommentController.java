package GDGoC.project.user_api.controller;

import GDGoC.project.user_api.dto.CommentDto;
import GDGoC.project.user_api.dto.CommentRequest;
import GDGoC.project.user_api.entity.Comment;
import GDGoC.project.user_api.entity.Post;
import GDGoC.project.user_api.entity.User;
import GDGoC.project.user_api.service.CommentService;
import GDGoC.project.user_api.service.PostService;
import GDGoC.project.user_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final PostService postService;
  private final UserService userService;

  /** 특정 게시글의 댓글 목록 조회 */
  @GetMapping("/posts/{postId}/comments")
  public List<CommentDto> list(@PathVariable Integer postId, Principal principal) {
    User me = (principal != null) ? userService.getUser(principal.getName()) : null;
    return commentService.getComments(postId).stream()
            .map(c -> CommentDto.from(c, me))
            .toList();
  }

  /** 댓글 단건 조회 */
  @GetMapping("/comments/{id}")
  public CommentDto detail(@PathVariable Integer id, Principal principal) {
    User me = (principal != null) ? userService.getUser(principal.getName()) : null;
    return CommentDto.from(commentService.getComment(id), me);
  }

  /** 댓글 생성 */
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<CommentDto> create(@PathVariable Integer postId,
                                           @Valid @RequestBody CommentRequest req,
                                           Principal principal) {
    Post post = postService.getPost(postId);
    User author = userService.getUser(principal.getName());
    Comment comment = commentService.createComment(post, req.content(), author);
    return ResponseEntity
            .created(URI.create("/api/comments/" + comment.getId()))
            .body(CommentDto.from(comment, author));
  }

  /** 댓글 수정 */
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/comments/{id}")
  public CommentDto modify(@PathVariable Integer id,
                           @Valid @RequestBody CommentRequest req,
                           Principal principal) {
    Comment comment = commentService.getComment(id);
    commentService.assertAuthor(comment, principal.getName());
    commentService.modifyComment(comment, req.content());
    User me = userService.getUser(principal.getName());
    return CommentDto.from(comment, me);
  }

  /** 댓글 삭제 */
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/comments/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id, Principal principal) {
    Comment comment = commentService.getComment(id);
    commentService.assertAuthor(comment, principal.getName());
    commentService.deleteComment(comment);
    return ResponseEntity.noContent().build();
  }

  /** 댓글 좋아요 */
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/comments/{id}/like")
  public CommentDto like(@PathVariable Integer id, Principal principal) {
    Comment comment = commentService.getComment(id);
    User user = userService.getUser(principal.getName());
    commentService.addLike(comment, user); // 또는 toggleLike(comment, user)
    return CommentDto.from(comment, user);
  }

  /** 댓글 좋아요 취소 */
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/comments/{id}/like")
  public CommentDto cancelLike(@PathVariable Integer id, Principal principal) {
    Comment comment = commentService.getComment(id);
    User user = userService.getUser(principal.getName());
    commentService.removeLike(comment, user);
    return CommentDto.from(comment, user);
  }
}
