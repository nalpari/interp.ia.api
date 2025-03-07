package net.devgrr.interp.ia.api.work.project.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.Priority;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "프로젝트 요청 객체")
public record ProjectRequest(
    @Schema(description = "프로젝트 ID")
        @NotNull(message = "필수값: id", groups = ProjectValidationGroup.putGroup.class)
        @JsonView(ProjectValidationGroup.putGroup.class)
        Long id,
    @Schema(description = "제목")
        @NotBlank(message = "필수값: title", groups = ProjectValidationGroup.postGroup.class)
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        String title,
    @Schema(description = "부제목")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        String subTitle,
    //    @Schema(description = "유형") @JsonView(ProjectValidationGroup.putGroup.class)
    //        IssueType type, // IssueType.PROJECT 고정, 수정 불가
    @Schema(description = "상태")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        IssueStatus status,
    @Schema(description = "중요도")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        Priority priority,
    @Schema(description = "담당자 ID")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        Set<Integer> assigneeId,
    @Schema(description = "기한일")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate dueDate,
    @Schema(description = "시작일")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate startDate,
    @Schema(description = "종료일")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        @DateTimeFormat(pattern = "yyyyMMdd")
        LocalDate endDate,
    @Schema(description = "내용")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        String description,
    @Schema(description = "태그")
        @JsonView({ProjectValidationGroup.postGroup.class, ProjectValidationGroup.putGroup.class})
        Set<String> tag,
    @Schema(description = "하위 이슈") @JsonView(ProjectValidationGroup.putGroup.class)
        Set<Long> subIssuesId) {}
