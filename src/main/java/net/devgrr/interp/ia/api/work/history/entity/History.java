package net.devgrr.interp.ia.api.work.history.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "history")
@Schema(description = "변경이력 엔티티")
@AllArgsConstructor
public class History {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "고유 ID")
  private Long id;

  @Column(nullable = false)
  @Schema(description = "이슈 ID")
  private Long issueId;

  @Column(nullable = false)
  @Schema(description = "필드")
  private String fieldName;

  @Column(columnDefinition = "TEXT")
  @Schema(description = "변경 전 값")
  private String beforeValue;

  @Column(columnDefinition = "TEXT")
  @Schema(description = "변경 후 값")
  private String afterValue;

  @CreatedDate
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Schema(description = "수정 일자")
  private LocalDateTime modifiedDate;

  @ManyToOne
  @JoinColumn(name = "modifier_id", nullable = false)
  @Schema(description = "수정자")
  private Member modifier;

  public History() {}
}
