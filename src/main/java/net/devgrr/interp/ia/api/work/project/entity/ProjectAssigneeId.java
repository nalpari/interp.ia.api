package net.devgrr.interp.ia.api.work.project.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProjectAssigneeId implements Serializable {
  private Long projectId;
  private Long memberId;
}
