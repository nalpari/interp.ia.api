package net.devgrr.interp.ia.api.work.issue.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
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
import net.devgrr.interp.ia.api.model.entity.BaseEntity;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Builder
@Entity
@Table(name = "issue")
@Schema(description = "이슈 엔티티")
@AllArgsConstructor
@DynamicUpdate
public class Issue extends BaseEntity {
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

  @Column(nullable = false)
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
  private LocalDate dueDate;

  @Schema(description = "시작일")
  private LocalDate startDate;

  @Schema(description = "종료일")
  private LocalDate endDate;

  @Column(columnDefinition = "TEXT")
  @Schema(description = "내용")
  private String description;

  @ElementCollection
  @CollectionTable(name = "issue_tags", joinColumns = @JoinColumn(name = "issue_id"))
  @Column(name = "tag")
  @Schema(description = "태그")
  private Set<String> tag;

  /*
   * TODO: 댓글 추가
   * */
  //  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
  //  @Schema(description = "댓글")
  //  private List<IssueComment> comments;

  @ManyToOne
  @JoinColumn(name = "parent_project_id", nullable = false)
  @Schema(description = "상위 프로젝트")
  private Project parentProject;

  @ManyToOne
  @Schema(description = "상위 이슈")
  private Issue parentIssue;

  @Transient
  @Schema(description = "하위 이슈")
  private Set<Issue> subIssues;

  @ManyToMany
  @Schema(description = "연관 이슈")
  private Set<Issue> relatedIssues;

  @Column(nullable = false, columnDefinition = "boolean default false")
  @Schema(description = "삭제 여부 (true: 삭제, false: 비삭제)")
  private Boolean isDeleted;

  public Issue() {}
}
