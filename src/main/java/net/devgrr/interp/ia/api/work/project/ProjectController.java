package net.devgrr.interp.ia.api.work.project;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.ProjectMapper;
import net.devgrr.interp.ia.api.work.project.dto.ProjectRequest;
import net.devgrr.interp.ia.api.work.project.dto.ProjectResponse;
import net.devgrr.interp.ia.api.work.project.dto.ProjectValidationGroup;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "ProjectController", description = "프로젝트 API")
@RestController
public class ProjectController {

  private final ProjectService projectService;
  private final ProjectMapper projectMapper;

  @Operation(description = "프로젝트 목록을 조회한다. <br>검색 조건이 있다면 해당 조건이 적용된 프로젝트를 조회한다.")
  @GetMapping
  public List<ProjectResponse> getProjects(
      @RequestParam(value = "status", required = false) @Parameter(description = "상태")
          String status,
      @RequestParam(value = "priority", required = false) @Parameter(description = "중요도")
          String priority,
      @RequestParam(value = "title", required = false) @Parameter(description = "프로젝트 제목")
          String title,
      @RequestParam(value = "subTitle", required = false) @Parameter(description = "프로젝트 부제목")
          String subTitle,
      @RequestParam(value = "creator", required = false) @Parameter(description = "생성자")
          String creator,
      @RequestParam(value = "assignee", required = false) @Parameter(description = "담당자")
          String assignee,
      @RequestParam(value = "createdDate", required = false)
          @Parameter(description = "생성일 (yyyy-MM-dd)")
          @DateTimeFormat(pattern = "yyyyMMdd")
          LocalDateTime createdDate,
      @RequestParam(value = "updatedDate", required = false)
          @Parameter(description = "수정일 (yyyy-MM-dd)")
          @DateTimeFormat(pattern = "yyyyMMdd")
          LocalDateTime updatedDate,
      @RequestParam(value = "dueDate", required = false)
          @Parameter(description = "기한일 (yyyy-MM-dd)")
          @DateTimeFormat(pattern = "yyyyMMdd")
          LocalDateTime dueDate,
      @RequestParam(value = "startDate", required = false)
          @Parameter(description = "시작일 (yyyy-MM-dd)")
          @DateTimeFormat(pattern = "yyyyMMdd")
          LocalDateTime startDate,
      @RequestParam(value = "endDate", required = false)
          @Parameter(description = "종료일 (yyyy-MM-dd)")
          @DateTimeFormat(pattern = "yyyyMMdd")
          LocalDateTime endDate,
      @RequestParam(value = "tag", required = false) @Parameter(description = "태그") Set<String> tag)
      throws BaseException {
    if ((status != null && !status.trim().isEmpty())
        || (priority != null) && !priority.trim().isEmpty()
        || (title != null && !title.trim().isEmpty())
        || (subTitle != null && !subTitle.trim().isEmpty())
        || (creator != null && !creator.trim().isEmpty())
        || (assignee != null && !assignee.trim().isEmpty())
        || (createdDate != null && !createdDate.equals(LocalDateTime.MIN))
        || (updatedDate != null && !updatedDate.equals(LocalDateTime.MIN))
        || (dueDate != null && !dueDate.equals(LocalDateTime.MIN))
        || (startDate != null && !startDate.equals(LocalDateTime.MIN))
        || (endDate != null && !endDate.equals(LocalDateTime.MIN))
        || (tag != null && !tag.isEmpty())) {
      return projectService
          .getProjectsByKeywords(
              status,
              priority,
              title,
              subTitle,
              creator,
              assignee,
              createdDate,
              updatedDate,
              dueDate,
              startDate,
              endDate,
              tag)
          .stream()
          .map(projectMapper::toResponse)
          .collect(java.util.stream.Collectors.toList());
    } else {
      return projectService.getProjects().stream()
          .map(projectMapper::toResponse)
          .collect(java.util.stream.Collectors.toList());
    }
  }

  @Operation(description = "프로젝트를 조회한다.")
  @GetMapping("/{id}")
  public ProjectResponse getProjectsById(
      @PathVariable("id") @Parameter(description = "프로젝트 ID") Long id) {
    return projectMapper.toResponse(projectService.getProjectsById(id));
  }

  @Operation(description = "프로젝트를 등록한다.")
  @JsonView(ProjectValidationGroup.postGroup.class)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProjectResponse setProjects(
      @RequestBody ProjectRequest req, @AuthenticationPrincipal UserDetails userDetails)
      throws BaseException {
    return projectMapper.toResponse(projectService.setProjects(req, userDetails.getUsername()));
  }

  /**
   * TODO
   *
   * <p>- 수정 권한 처리 추가
   */
  @Operation(
      description =
          """
          프로젝트 정보를 수정한다.

          수정할 필드명은 key, 수정 데이터는 value의 JSON 형태로 입력한다.

          key 목록 및 value 타입
          - title (제목) - String
          - subTitle (부제목) - String
          - status (상태) - String
          - priority (중요도) - String
          - assigneeId (담당자 ID) - List<Integer>
          - dueDate (기한일) - String (format: yyyy-MM-dd)
          - startDate (시작일) - String (format: yyyy-MM-dd)
          - endDate (종료일) - String (format: yyyy-MM-dd)
          - description (내용) - String
          - tag (태그) - List<String>
          - subIssuesId (하위 이슈 ID) - List<Integer>
          """,
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = "application/json",
                      examples = {
                        @ExampleObject(name = "제목 수정 요청", value = "{\"title\": \"제목 수정\"}"),
                        @ExampleObject(name = "담당자 수정 요청", value = "{\"assigneeId\": [1, 2]}"),
                        @ExampleObject(name = "기한일 수정 요청", value = "{\"dueDate\": \"2025-01-01\"}")
                      })))
  @PatchMapping("/{id}")
  public void putProjectsById(
      @PathVariable("id") @Parameter(description = "프로젝트 ID") Long id,
      @RequestBody Map<String, Object> req,
      @AuthenticationPrincipal UserDetails userDetails)
      throws BaseException {
    projectService.putProjectsById(id, req, userDetails);
  }

  /**
   * TODO
   *
   * <p>- 삭제 정책 필요(삭제 플래그를 활용할 것인지)
   *
   * <p>- 삭제 시 연관된 데이터(issue, history) 삭제 여부
   *
   * <p>- 삭제 권한 처리 추가
   */
  @Operation(description = "프로젝트를 삭제한다.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delProjectsById(@PathVariable("id") @Parameter(description = "프로젝트 ID") Long id)
      throws BaseException {
    projectService.delProjectsById(id);
  }
}
