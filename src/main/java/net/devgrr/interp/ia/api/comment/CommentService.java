package net.devgrr.interp.ia.api.comment;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.comment.dto.CommentRequest;
import net.devgrr.interp.ia.api.comment.dto.CommentResponse;
import net.devgrr.interp.ia.api.comment.entity.Comment;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.config.mapStruct.CommentMapper;
import net.devgrr.interp.ia.api.member.MemberRole;
import net.devgrr.interp.ia.api.member.MemberService;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.issue.IssueService;
import net.devgrr.interp.ia.api.work.project.ProjectService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentMapper commentMapper;

  private final MemberRepository memberRepository;
  private final CommentRepository commentRepository;
  private final ProjectRepository projectRepository;
  private final IssueRepository issueRepository;

  @Transactional
  public Comment setComments(CommentRequest req, String userEmail) throws BaseException {
    Member member =
        memberRepository
            .findByEmail(userEmail)
            .orElseThrow(
                () -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보를 찾을 수 없습니다."));
    verifyRefType(req.referenceType(), req.referenceId());
    return commentRepository.save(commentMapper.toComment(req, member));
  }

  private void verifyRefType(IssueCategory type, Long id) throws BaseException {
    if (type == IssueCategory.PROJECT && !projectService.existById(id)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 프로젝트입니다.");
    }
    if (type == IssueCategory.ISSUE && !issueService.existById(id)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 이슈입니다.");
    }
  }

  public List<Comment> getCommentsById(IssueCategory referenceType, Long id) throws BaseException {
    verifyRefType(referenceType, id);
    return commentRepository.findAllByReferenceTypeAndReferenceId(referenceType, id);
  }

  public List<CommentResponse> getCommentsByIdWithHierarchy(IssueCategory referenceType, Long id)
      throws BaseException {
    List<Comment> comments = getCommentsById(referenceType, id);

    List<Comment> parentComments =
        comments.stream().filter(comment -> comment.getParentCommentId() == null).toList();

    return parentComments.stream()
        .map(parentComment -> buildHierarchy(parentComment, comments))
        .toList();
  }

  private CommentResponse buildHierarchy(Comment parentComment, List<Comment> comments) {
    List<CommentResponse> childComments =
        comments.stream()
            .filter(comment -> Objects.equals(comment.getParentCommentId(), parentComment.getId()))
            .map(child -> buildHierarchy(child, comments))
            .toList();
    return commentMapper.toResponseWithChildren(parentComment, childComments);
  }

  @Transactional
  public void delCommentsById(Long id, UserDetails user) throws BaseException {
    Comment comment =
        commentRepository
            .findById(id)
            .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다."));

    if (user.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(MemberRole.ADMIN.getValue()))
        || comment.getMember().getEmail().equals(user.getUsername())) {
      commentRepository.delete(comment);
    } else {
      throw new BaseException(ErrorCode.FORBIDDEN, "삭제 권한이 없습니다.");
    }
  }

  @Transactional
  public Comment putComments(CommentRequest req, String username) throws BaseException {
    Comment comment =
        commentRepository
            .findById(req.id())
            .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다."));

    if (!comment.getMember().getEmail().equals(username)) {
      throw new BaseException(ErrorCode.FORBIDDEN, "수정 권한이 없습니다.");
    }
    return commentMapper.updateComment(req, comment);
  }
}
