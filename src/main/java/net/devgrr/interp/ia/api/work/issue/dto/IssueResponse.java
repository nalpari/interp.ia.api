package net.devgrr.interp.ia.api.work.issue.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;
import net.devgrr.interp.ia.api.work.project.dto.ProjectRefResponse;

@Schema(description = "이슈 응답 객체")
public record IssueResponse(
    @Schema(description = "이슈 ID") Long id,
    @Schema(description = "제목") String title,
    @Schema(description = "부제목") String subTitle,
    @Schema(description = "유형") IssueType type,
    @Schema(description = "상태") IssueStatus status,
    @Schema(description = "중요도") Priority priority,
    @Schema(description = "생성자") MemberResponse creator,
    @Schema(description = "담당자") Set<MemberResponse> assignee,
    @Schema(description = "생성일") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
    @Schema(description = "수정일") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedDate,
    @Schema(description = "기한일") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dueDate,
    @Schema(description = "시작일") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate,
    @Schema(description = "종료일") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
    @Schema(description = "내용") String description,
    @Schema(description = "태그") Set<String> tag,
    @Schema(description = "상위 프로젝트 ID") ProjectRefResponse parentProject,
    @Schema(description = "상위 이슈 ID") ProjectRefResponse parentIssue,
    @Schema(description = "하위 이슈") Set<IssueRefResponse> subIssues,
    @Schema(description = "연관 이슈") Set<IssueRefResponse> relatedIssues) {}
