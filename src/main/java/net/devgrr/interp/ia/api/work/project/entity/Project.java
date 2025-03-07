package net.devgrr.interp.ia.api.work.project.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.model.entity.BaseEntity;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "project")
@Schema(description = "프로젝트 엔티티")
@AllArgsConstructor
@DynamicUpdate
public class Project extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "고유 ID")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Schema(description = "유형")
  private IssueType type;

  @Enumerated(EnumType.STRING)
  @Schema(description = "상태")
  private IssueStatus status;

  @Enumerated(EnumType.STRING)
  @Schema(description = "중요도")
  private Priority priority;

  @Schema(description = "제목")
  private String title;

  @Schema(description = "부제목")
  private String subTitle;

  @ManyToOne
  @JoinColumn(name = "creator_id", nullable = false)
  @Schema(description = "생성자")
  private Member creator;

  @ManyToMany
  @JoinTable(
      name = "project_assignee",
      joinColumns = @JoinColumn(name = "project_id"),
      inverseJoinColumns = @JoinColumn(name = "member_id"))
  @Schema(description = "담당자")
  private Set<Member> assignee;

  @Schema(description = "기한일")
  private LocalDateTime dueDate;

  @Schema(description = "시작일")
  private LocalDateTime startDate;

  @Schema(description = "종료일")
  private LocalDateTime endDate;

  @Column(columnDefinition = "TEXT")
  @Schema(description = "내용")
  private String description;

  @ElementCollection
  @CollectionTable(name = "project_tags", joinColumns = @JoinColumn(name = "project_id"))
  @Column(name = "tag")
  @Schema(description = "태그")
  private Set<String> tag;

  /*
   * TODO: 댓글 추가
   * */
  //  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
  //  @Schema(description = "댓글")
  //  private List<IssueComment> comments;

  @OneToMany(mappedBy = "parentProject")
  @Schema(description = "하위 이슈")
  private Set<Issue> subIssues;

  public Project() {}
}
