package net.devgrr.interp.ia.api.work.issue.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
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
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.project.entity.Project;

@Getter
@Setter
@Builder
@Entity
@Table(name = "issue")
@Schema(description = "이슈 엔티티")
@AllArgsConstructor
public class Issue {
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
      name = "issue_assignee",
      joinColumns = @JoinColumn(name = "issue_id"),
      inverseJoinColumns = @JoinColumn(name = "member_id"))
  @Schema(description = "담당자")
  private List<Member> assignee;

  @Schema(description = "기한일")
  private LocalDateTime dueDate;

  @Schema(description = "시작일")
  private LocalDateTime startDate;

  @Schema(description = "종료일")
  private LocalDateTime endDate;

  @Column(columnDefinition = "TEXT")
  @Schema(description = "내용")
  private String description;

  @Schema(description = "태그")
  private String tag;

  /*
   * TODO: 댓글 추가
   * */
  //  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
  //  @Schema(description = "댓글")
  //  private List<IssueComment> comments;

  /*
   * TODO: 변경이력 추가
   * */
  //  @Schema(description = "변경이력")
  //  private List<History> history;

  @ManyToOne
  @JoinColumn(name = "parent_project_id", nullable = false)
  @Schema(description = "상위 프로젝트")
  private Project parentProject;

  @ManyToOne
  @Schema(description = "상위 이슈")
  private Issue parentIssue;

  @OneToMany
  @Schema(description = "하위 이슈")
  private Set<Issue> subIssues;

  @OneToMany
  @Schema(description = "연관 이슈")
  private Set<Issue> relatedIssues;

  public Issue() {}
}
