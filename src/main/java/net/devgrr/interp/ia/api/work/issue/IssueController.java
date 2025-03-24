package net.devgrr.interp.ia.api.work.issue;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.IssueMapper;
import net.devgrr.interp.ia.api.work.issue.dto.IssueRequest;
import net.devgrr.interp.ia.api.work.issue.dto.IssueResponse;
import net.devgrr.interp.ia.api.work.issue.dto.IssueValidationGroup;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "IssueController", description = "이슈 API")
@RestController
public class IssueController {

  private final IssueService issueService;
  private final IssueMapper issueMapper;

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
