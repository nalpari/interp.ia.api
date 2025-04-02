package net.devgrr.interp.ia.api.comment.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;

@Schema(description = "댓글 요청")
public record CommentRequest(
    @Schema(description = "댓글 ID")
        @NotNull(message = "필수값: 댓글 ID", groups = CommentValidationGroup.putGroup.class)
        Long id,
    @Schema(description = "댓글이 달린 Entity 타입 (Issue/Project)")
        @NotNull(message = "필수값: reference type", groups = CommentValidationGroup.postGroup.class)
        @JsonView(CommentValidationGroup.postGroup.class)
        IssueCategory referenceType,
    @Schema(description = "댓글이 달린 Entity ID")
        @NotNull(message = "필수값: Entity id", groups = CommentValidationGroup.postGroup.class)
        @JsonView(CommentValidationGroup.postGroup.class)
        Long referenceId,
    @Schema(description = "부모 댓글 ID") @JsonView(CommentValidationGroup.postGroup.class)
        Integer parentCommentId,
    @Schema(description = "댓글 내용")
        @NotBlank(
            message = "필수값: content",
            groups = {
              CommentValidationGroup.postGroup.class,
              CommentValidationGroup.putGroup.class
            })
        @JsonView({CommentValidationGroup.postGroup.class, CommentValidationGroup.putGroup.class})
        String content) {}
