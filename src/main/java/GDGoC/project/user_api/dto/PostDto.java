package GDGoC.project.user_api.dto;

import GDGoC.project.user_api.entity.Post;
import java.time.LocalDateTime;

public record PostDto(
        Integer id,
        String content,
        String authorId,      // 로그인 아이디
        String authorName,    // 화면 표시용 이름
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        int commentCount,
        int likeCount,
        boolean likedByMe     // ✅ 추가 필드
) {
  public static PostDto from(Post post, String currentUsernameOrNull) {
    var author = post.getAuthor();
    String username = (author != null) ? author.getUsername() : "anonymous";

    // 이름 우선, 없으면 아이디로 폴백
    String displayName = username;
    if (author != null && author.getName() != null && !author.getName().isBlank()) {
      displayName = author.getName();
    }

    boolean likedByMe = false;
    if (currentUsernameOrNull != null && post.getLikes() != null) {
      likedByMe = post.getLikes().stream()
              .anyMatch(u -> currentUsernameOrNull.equals(u.getUsername()));
    }

    return new PostDto(
            post.getId(),
            post.getContent(),
            username,                                   // authorId
            displayName,                                // authorName
            post.getCreateDate(),
            post.getModifyDate(),
            (post.getCommentList() == null) ? 0 : post.getCommentList().size(),
            (post.getLikes() == null) ? 0 : post.getLikes().size(),
            likedByMe                                   // ✅ 빠졌던 인자 추가
    );
  }

  // 비로그인/알 수 없음일 때 호환 유지
  public static PostDto from(Post post) {
    return from(post, null);
  }
}
