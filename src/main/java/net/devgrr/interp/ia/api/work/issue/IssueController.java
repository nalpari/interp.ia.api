package net.devgrr.interp.ia.api.work.issue;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.config.mapStruct.IssueMapper;
import net.devgrr.interp.ia.api.work.issue.dto.IssueRequest;
import net.devgrr.interp.ia.api.work.issue.dto.IssueResponse;
import net.devgrr.interp.ia.api.work.issue.dto.IssueValidationGroup;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "IssueController", description = "이슈 API")
@RestController
public class IssueController {

  private final IssueService issueService;
  private final IssueMapper issueMapper;

  @Operation(description = "이슈 목록을 조회한다.")
  @GetMapping
  public List<IssueResponse> getIssues(
      @RequestParam(value = "projectId", required = false) @Parameter(description = "프로젝트 ID")
          Long projectId,
      @RequestParam(value = "issueId", required = false) @Parameter(description = "이슈 ID")
          Long issueId)
      throws BaseException {
    return issueService.getIssues(projectId, issueId).stream()
        .map(issueMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Operation(
      description =
          """
          이슈를 검색한다. \n
          검색 조건은 전부 선택 사항이며, 조건이 여러 개 있을 경우 AND 조건으로 검색한다. <br>
          검색 조건이 없을 경우 전체 목록을 조회한다. <br>
          하위 이슈는 검색되지 않는다. (이슈 단건 조회 이용)
          """)
  @GetMapping("/search")
  public List<IssueResponse> getIssuesByKeywords(
      @RequestParam(value = "projectId", required = false) @Parameter(description = "상위 프로젝트 ID")
          List<Long> projectId,
      @RequestParam(value = "parentIssueId", required = false) @Parameter(description = "상위 이슈 ID")
          List<Long> parentIssueId,
      @RequestParam(value = "issueId", required = false) @Parameter(description = "이슈 ID")
          List<Long> issueId,
      @RequestParam(value = "type", required = false) @Parameter(description = "유형")
          List<IssueType> type,
      @RequestParam(value = "status", required = false) @Parameter(description = "상태")
          List<IssueStatus> status,
      @RequestParam(value = "priority", required = false) @Parameter(description = "중요도")
          List<Priority> priority,
      @RequestParam(value = "title", required = false) @Parameter(description = "이슈 제목")
          String title,
      @RequestParam(value = "subTitle", required = false) @Parameter(description = "이슈 부제목")
          String subTitle,
      @RequestParam(value = "creatorId", required = false) @Parameter(description = "생성자 ID")
          List<Long> creatorId,
      @RequestParam(value = "assigneeId", required = false) @Parameter(description = "담당자 ID")
          List<Long> assigneeId,
      @RequestParam(value = "createdDateFrom", required = false)
          @Parameter(description = "생성일 시작 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate createdDateFrom,
      @RequestParam(value = "createdDateTo", required = false)
          @Parameter(description = "생성일 종료 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate createdDateTo,
      @RequestParam(value = "updatedDateFrom", required = false)
          @Parameter(description = "수정일 시작 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate updatedDateFrom,
      @RequestParam(value = "updatedDateTo", required = false)
          @Parameter(description = "수정일 종료 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate updatedDateTo,
      @RequestParam(value = "dueDateFrom", required = false)
          @Parameter(description = "기한일 시작 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dueDateFrom,
      @RequestParam(value = "dueDateTo", required = false)
          @Parameter(description = "기한일 종료 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dueDateTo,
      @RequestParam(value = "startDateFrom", required = false)
          @Parameter(description = "시작일 시작 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDateFrom,
      @RequestParam(value = "startDateTo", required = false)
          @Parameter(description = "시작일 종료 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDateTo,
      @RequestParam(value = "endDateFrom", required = false)
          @Parameter(description = "종료일 시작 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDateFrom,
      @RequestParam(value = "endDateTo", required = false)
          @Parameter(description = "종료일 종료 (yyyy-MM-dd)")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDateTo,
      @RequestParam(value = "tag", required = false) @Parameter(description = "태그") Set<String> tag)
      throws BaseException {
    return issueService
        .getIssuesByKeywords(
            projectId,
            parentIssueId,
            issueId,
            type,
            status,
            priority,
            title,
            subTitle,
            creatorId,
            assigneeId,
            createdDateFrom,
            createdDateTo,
            updatedDateFrom,
            updatedDateTo,
            dueDateFrom,
            dueDateTo,
            startDateFrom,
            startDateTo,
            endDateFrom,
            endDateTo,
            tag)
        .stream()
        .map(issueMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Operation(description = "이슈를 조회한다.")
  @GetMapping("/{id}")
  public IssueResponse getIssuesById(@PathVariable("id") @Parameter(description = "이슈 ID") Long id)
      throws BaseException {
    return issueMapper.toResponse(issueService.getIssueWithRelatedById(id));
  }

  @Operation(description = "이슈를 등록한다.")
  @JsonView(IssueValidationGroup.postGroup.class)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public IssueResponse setIssues(
      @Validated(IssueValidationGroup.postGroup.class) @RequestBody IssueRequest req,
      @AuthenticationPrincipal UserDetails userDetails)
      throws BaseException {
    return issueMapper.toResponse(issueService.setIssues(req, userDetails.getUsername()));
  }

  @Operation(description = "이슈를 삭제한다.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteIssuesById(@PathVariable("id") @Parameter(description = "이슈 ID") Long id)
      throws BaseException {
    issueService.deleteIssuesById(id);
  }
}
