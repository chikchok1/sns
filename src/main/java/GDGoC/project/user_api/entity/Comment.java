package GDGoC.project.user_api.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime createDate;

  @ManyToOne(fetch = FetchType.LAZY)
  private Post post;

  @ManyToOne
  private User author;

  private LocalDateTime modifyDate;

  @ManyToMany
  @JoinTable(
          name = "comment_likes",                      // ✅ 실제 테이블명과 통일
          joinColumns = @JoinColumn(name = "Comment_id"),
          inverseJoinColumns = @JoinColumn(name = "likes_id")
          // uniqueConstraints 는 (운영 DB 충돌 방지 차원에서) DDL 로 추가 권장 → 아래 4) 참고
  )
  private Set<User> likes = new HashSet<>();
}
