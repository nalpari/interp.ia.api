package net.devgrr.interp.ia.api.comment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.model.entity.BaseEntity;

@Getter
@Setter
@Builder
@Entity
@Table(name = "comment")
@Schema(description = "댓글 엔티티")
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "댓글 ID")
    private Long id;

    @Schema(description = "부모 댓글 ID")
    private Integer parentCommentId;

    @Schema(description = "댓글 내용")
    @Column(nullable = false)
    private String content;

    @Schema(description = "작성자")
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Schema(description = "프로젝트 / 이슈 구분")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueCategory referenceType;

    @Schema(description = "댓글이 달린 Entity의 ID")
    @Column(nullable = false)
    private Long referenceId;
}
