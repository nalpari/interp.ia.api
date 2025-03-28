package net.devgrr.interp.ia.api.work.history.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;

@Schema(description = "변경이력 응답 객체")
public record HistoryResponse(
    @Schema(description = "고유 ID") Long id,
    @Schema(description = "카테고리 (ex. project, issue)") String category,
    @Schema(description = "이슈 ID") Long issueId,
    @Schema(description = "필드") String fieldName,
    @Schema(description = "변경 전 값") String beforeValue,
    @Schema(description = "변경 후 값") String afterValue,
    @Schema(description = "수정 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime modifiedDate,
    @Schema(description = "수정자") MemberResponse modifier) {}
