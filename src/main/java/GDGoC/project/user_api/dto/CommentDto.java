package GDGoC.project.user_api.dto;

import GDGoC.project.user_api.entity.Comment;
import GDGoC.project.user_api.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record CommentDto(
        Integer id,
        String content,
        String authorId,           // username (기존 유지)
        String authorName,         // ✅ 표시용 이름 (name 우선, 없으면 username, 없으면 "익명")
        LocalDateTime createdAt,   // 기존 유지
        LocalDateTime modifiedAt,  // 기존 유지
        String createdOn,          // ✅ YYYY-MM-DD 표시용 문자열
        int likeCount,
        boolean likedByMe
) {
  public static CommentDto from(Comment comment, User me) {
    // 안전 가드
    User author = comment.getAuthor();
    String username = (author != null) ? author.getUsername() : null;
    String name = (author != null && author.getName() != null && !author.getName().isBlank())
            ? author.getName()
            : (username != null ? username : "익명");

    LocalDateTime createdAt = comment.getCreateDate();
    String createdOn = (createdAt != null) ? createdAt.toLocalDate().toString() : "";

    int likeCount = (comment.getLikes() == null) ? 0 : comment.getLikes().size();

    // ID 기반 비교로 likedByMe 계산(contains(me) 대비 안전)
    boolean likedByMe = false;
    if (me != null && comment.getLikes() != null) {
      likedByMe = comment.getLikes().stream()
              .filter(Objects::nonNull)
              .anyMatch(u -> u.getId() != null && u.getId().equals(me.getId()));
    }

    return new CommentDto(
            comment.getId(),
            comment.getContent(),
            username,
            name,                // ✅ authorName
            createdAt,
            comment.getModifyDate(),
            createdOn,           // ✅ YYYY-MM-DD
            likeCount,
            likedByMe
    );
  }
}
