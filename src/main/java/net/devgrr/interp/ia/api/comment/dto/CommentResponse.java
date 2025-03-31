package net.devgrr.interp.ia.api.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 응답")
public record CommentResponse(
        @Schema(description = "댓글 ID") Integer id,
        @Schema(description = "부모 댓글 ID") Integer parentCommentId,
        @Schema(description = "댓글 내용") String content,
        @Schema(description = "작성자 Email") String writerEmail,
        @Schema(description = "작성자 이름") String writerName,
        @Schema(description = "댓글이 달린 Entity 종류 (Issue/Project)") String referenceType,
        @Schema(description = "댓글이 달린 Entity ID") Integer referenceId,
        @Schema(description = "댓글 작성 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        @Schema(description = "댓글 수정 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedDate,
        @Schema(description = "하위 댓글") @JsonInclude(JsonInclude.Include.NON_NULL)
        List<CommentResponse> childComment
) {}
