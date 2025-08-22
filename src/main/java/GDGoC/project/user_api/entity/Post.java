package GDGoC.project.user_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Post {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime createDate;

  @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
  private List<Comment> commentList;

  @ManyToOne
  private User author;

  private LocalDateTime modifyDate;

  @ManyToMany
  @JoinTable(
          name = "post_likes",
          joinColumns = @JoinColumn(name = "post_id"),              // Post FK
          inverseJoinColumns = @JoinColumn(name = "user_id")        // User FK (DB 실제 컬럼명에 맞춤)

  )
  private Set<User> likes = new HashSet<>();
}