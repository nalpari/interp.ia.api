package net.devgrr.interp.ia.api.work.project.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import net.devgrr.interp.ia.api.member.entity.Member;

@Entity
@Table(name = "project_assignee")
@Schema(description = "프로젝트 담당자 엔티티")
public class ProjectAssignee {

  @EmbeddedId private ProjectAssigneeId id;

  @ManyToOne
  @MapsId("projectId")
  @Schema(description = "프로젝트 ID")
  private Project project;

  @ManyToOne
  @MapsId("memberId")
  @Schema(description = "담당자 ID")
  private Member member;
}
