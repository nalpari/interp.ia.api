package net.devgrr.interp.ia.api.comment;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.comment.dto.CommentRequest;
import net.devgrr.interp.ia.api.comment.dto.CommentResponse;
import net.devgrr.interp.ia.api.comment.dto.CommentValidationGroup;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.config.mapStruct.CommentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "CommentController", description = "댓글 API")
@RestController
public class CommentController {
  private final CommentMapper commentMapper;
  private final CommentService commentService;

  @Operation(description = "댓글을 생성한다.")
  @JsonView(CommentValidationGroup.postGroup.class)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CommentResponse setComments(
      @Validated(CommentValidationGroup.postGroup.class) @RequestBody CommentRequest req,
      @AuthenticationPrincipal UserDetails user)
      throws BaseException {
    return commentMapper.toResponse(commentService.setComments(req, user.getUsername()));
  }

  @Operation(description = "Reference Type과 Id로 댓글 목록을 조회한다.")
  @GetMapping("/{referenceType}/{id}")
  public List<CommentResponse> getCommentsById(
      @PathVariable("referenceType") @Parameter(description = "참조 타입 (issue/project)")
          IssueCategory referenceType,
      @PathVariable("id") @Parameter(description = "참조 ID") Long id,
      @RequestParam(value = "nested", required = false, defaultValue = "false")
          @Parameter(description = "중첩구조 여부")
          Boolean nested)
      throws BaseException {
    return nested
        ? commentService.getCommentsByIdWithHierarchy(referenceType, id)
        : commentService.getCommentsById(referenceType, id).stream()
            .map(commentMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Operation(description = "댓글을 삭제한다.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delCommentsById(
      @PathVariable("id") @Parameter(description = "댓글 ID") Long id,
      @AuthenticationPrincipal UserDetails user)
      throws BaseException {
    commentService.delCommentsById(id, user);
  }

  @Operation(description = "댓글을 수정한다.")
  @PutMapping
  @JsonView(CommentValidationGroup.putGroup.class)
  public CommentResponse updateComments(
      @Validated(CommentValidationGroup.putGroup.class) @RequestBody CommentRequest req,
      @AuthenticationPrincipal UserDetails user)
      throws BaseException {
    return commentMapper.toResponse(commentService.putComments(req, user.getUsername()));
  }
}
