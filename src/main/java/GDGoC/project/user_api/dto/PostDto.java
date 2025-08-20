package GDGoC.project.user_api.dto;

import GDGoC.project.user_api.entity.Post;
import java.time.LocalDateTime;

public record PostDto(
        Integer id,
        String content,
        String authorId,      // 로그인 아이디(유지)
        String authorName,    // 화면 표시용 이름(신규)
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        int commentCount,
        int likeCount
) {
  public static PostDto from(Post post) {
    var author = post.getAuthor();
    String username = (author != null) ? author.getUsername() : "anonymous";

    // 이름 우선, 없으면 아이디로 폴백
    String displayName = username;
    if (author != null && author.getName() != null && !author.getName().isBlank()) {
      displayName = author.getName();
    }

    return new PostDto(
            post.getId(),
            post.getContent(),
            username,          // authorId
            displayName,       // authorName
            post.getCreateDate(),
            post.getModifyDate(),
            post.getCommentList() == null ? 0 : post.getCommentList().size(),
            post.getLikes() == null ? 0 : post.getLikes().size()
    );
  }
}
