package net.devgrr.interp.ia.api.work.issue.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "이슈 요청 객체")
public record IssueRequest(
    @Schema(description = "이슈 ID")
        @NotNull(message = "필수값: id", groups = IssueValidationGroup.putGroup.class)
        @JsonView(IssueValidationGroup.putGroup.class)
        Long id,
    @Schema(description = "제목")
        @NotBlank(message = "필수값: title", groups = IssueValidationGroup.postGroup.class)
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        String title,
    @Schema(description = "부제목")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        String subTitle,
    @Schema(description = "유형")
        @NotNull(message = "필수값: type", groups = IssueValidationGroup.postGroup.class)
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        IssueType type,
    @Schema(description = "상태")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        IssueStatus status,
    @Schema(description = "중요도")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        Priority priority,
    @Schema(description = "담당자 ID")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        Set<Integer> assigneeId,
    @Schema(description = "기한일")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate dueDate,
    @Schema(description = "시작일")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate startDate,
    @Schema(description = "종료일")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate endDate,
    @Schema(description = "내용")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        String description,
    @Schema(description = "태그")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        Set<String> tag,
    @Schema(description = "상위 프로젝트 ID")
        @NotNull(message = "필수값: 상위 프로젝트 ID", groups = IssueValidationGroup.postGroup.class)
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        Long parentProjectId,
    @Schema(description = "상위 이슈 ID")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        Long parentIssueId,
    @Schema(description = "하위 이슈 ID") @JsonView(IssueValidationGroup.putGroup.class)
        Set<Long> subIssuesId,
    @Schema(description = "연관 이슈 ID")
        @JsonView({IssueValidationGroup.postGroup.class, IssueValidationGroup.putGroup.class})
        Set<Long> relatedIssuesId) {}
