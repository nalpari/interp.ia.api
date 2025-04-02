package net.devgrr.interp.ia.api.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.devgrr.interp.ia.api.comment.dto.CommentRequest;
import net.devgrr.interp.ia.api.comment.dto.CommentResponse;
import net.devgrr.interp.ia.api.comment.entity.Comment;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.config.mapStruct.CommentMapper;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.MemberRole;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.issue.IssueRepository;
import net.devgrr.interp.ia.api.work.project.ProjectRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class commentServiceTest {
  @Mock CommentMapper commentMapper;

  @Mock CommentRepository commentRepository;
  @Mock ProjectRepository projectRepository;
  @Mock MemberRepository memberRepository;
  @Mock IssueRepository issueRepository;

  private Comment comment;
  private CommentRequest commentRequest;
  private Member member;
  private CommentService commentService;

  @BeforeEach
  void setUp() {
    member =
        Member.builder().email("admin@admin.com").role(MemberRole.ADMIN).isActive(true).build();

    comment =
        Comment.builder()
            .id(99L)
            .referenceType(IssueCategory.PROJECT)
            .referenceId(10L)
            .content("This is a comment")
            .member(member)
            .parentCommentId(null)
            .build();

    commentRequest = new CommentRequest(null, "project", 10L, null, "This is a comment");
    commentService =
        new CommentService(
            commentMapper, memberRepository, commentRepository, projectRepository, issueRepository);
  }

  @Nested
  @DisplayName("댓글 작성 테스트")
  class SetComments {
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @Test
      @DisplayName("댓글 작성 성공")
      public void setCommentsTest() throws BaseException {
        when(memberRepository.findByEmail("admin@admin.com"))
            .thenReturn(Optional.ofNullable(member));
        when(commentMapper.toComment(commentRequest, member)).thenReturn(comment);
        when(projectRepository.existsById(10L)).thenReturn(true);

        Comment result = commentService.setComments(commentRequest, "admin@admin.com");

        verify(commentRepository, times(1)).save(comment);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @Test
      @DisplayName("잘못된 referenceType 으로 댓글 작성 시도")
      public void WrongReferenceTypeTest() {
        when(memberRepository.findByEmail("admin@admin.com"))
            .thenReturn(Optional.ofNullable(member));

        commentRequest = new CommentRequest(null, "wrong", 10L, null, "This is a comment");

        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> commentService.setComments(commentRequest, "admin@admin.com"));
        Assertions.assertEquals("참조 타입이 잘못되었습니다. (ISSUE/PROJECT)", exception.getMessage());
      }

      @Test
      @DisplayName("존재하지 않는 프로젝트에 댓글 작성 시도")
      public void notExistProjectTest() {
        when(memberRepository.findByEmail("admin@admin.com"))
            .thenReturn(Optional.ofNullable(member));
        when(projectRepository.existsById(1L)).thenReturn(false);

        commentRequest = new CommentRequest(null, "project", 1L, null, "This is a comment");

        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> commentService.setComments(commentRequest, "admin@admin.com"));
        Assertions.assertEquals("존재하지 않는 프로젝트입니다.", exception.getMessage());
      }
    }
  }

  @Nested
  @DisplayName("댓글 조회 테스트")
  class GetComments {
    private List<Comment> comments = new ArrayList<>();

    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      Comment parentComment;
      Comment childComment;
      List<Comment> comments = new ArrayList<>();
      CommentResponse childResp;
      CommentResponse parentResp;

      @BeforeEach
      void setUp() {
        parentComment = Comment.builder().id(1L).parentCommentId(null).build();
        childComment = Comment.builder().id(2L).parentCommentId(1L).build();

        comments = List.of(parentComment, childComment);

        childResp =
            new CommentResponse(
                childComment.getId().intValue(),
                parentComment.getId().intValue(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        parentResp =
            new CommentResponse(
                parentComment.getId().intValue(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(childResp));
      }

      @Test
      @DisplayName("중첩된 구조로 댓글 조회 성공")
      public void getCommentsWithNestedTest() throws BaseException {
        when(commentRepository.findAllByReferenceTypeAndReferenceId(IssueCategory.PROJECT, 10L))
            .thenReturn(comments);
        when(commentMapper.toResponseWithChildren(any(Comment.class), anyList()))
            .thenReturn(parentResp);
        when(projectRepository.existsById(10L)).thenReturn(true);

        List<CommentResponse> result = commentService.getCommentsByIdWithHierarchy("project", 10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).childComment().size());

        verify(commentRepository, times(1)).findAllByReferenceTypeAndReferenceId(any(), anyLong());
        verify(commentMapper, times(2)).toResponseWithChildren(any(Comment.class), anyList());
      }

      @Test
      @DisplayName("댓글 조회 성공")
      public void getCommentsTest() throws BaseException {
        when(commentRepository.findAllByReferenceTypeAndReferenceId(IssueCategory.PROJECT, 10L))
            .thenReturn(comments);
        when(projectRepository.existsById(10L)).thenReturn(true);

        List<Comment> result = commentService.getCommentsById("project", 10L);

        Assertions.assertEquals(comments, result);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @Test
      @DisplayName("잘못된 referenceType 으로 댓글 조회 시도")
      public void WrongReferenceTypeTest() {
        BaseException exception =
            assertThrows(BaseException.class, () -> commentService.getCommentsById("wrong", 10L));

        Assertions.assertEquals("참조 타입이 잘못되었습니다. (ISSUE/PROJECT)", exception.getMessage());
      }

      @Test
      @DisplayName("존재하지 않는 프로젝트의 댓글 조회 시도")
      public void notExistProjectTest() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        BaseException exception =
            assertThrows(BaseException.class, () -> commentService.getCommentsById("project", 99L));

        Assertions.assertEquals("존재하지 않는 프로젝트입니다.", exception.getMessage());
      }
    }
  }

  @Nested
  @DisplayName("댓글 수정 테스트")
  class PutComments {
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @Test
      @DisplayName("댓글 수정 성공")
      public void putCommentsTest() throws BaseException {
        when(commentRepository.findById(90L)).thenReturn(Optional.ofNullable(comment));

        Comment expected =
            Comment.builder()
                .id(90L)
                .referenceType(IssueCategory.PROJECT)
                .referenceId(10L)
                .content("update!!!")
                .build();
        when(commentMapper.updateComment(any(CommentRequest.class), any(Comment.class)))
            .thenReturn(expected);

        commentRequest = new CommentRequest(90L, "project", 10L, null, "update!!!");

        Comment updateComment = commentService.putComments(commentRequest, "admin@admin.com");

        Assertions.assertEquals(expected, updateComment);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @Test
      @DisplayName("존재하지 않는 댓글 수정 시도")
      public void putCommentsTest() {
        commentRequest = new CommentRequest(80L, "project", 10L, null, "test");

        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> commentService.putComments(commentRequest, "admin@admin.com"));

        Assertions.assertEquals("댓글을 찾을 수 없습니다.", exception.getMessage());
      }

      @Test
      @DisplayName("본인 댓글이 아닌 댓글 수정 시도")
      public void notOwnPutCommentsTest() {
        when(commentRepository.findById(99L)).thenReturn(Optional.ofNullable(comment));

        commentRequest = new CommentRequest(99L, "project", 10L, null, "test");
        BaseException exception =
            assertThrows(
                BaseException.class, () -> commentService.putComments(commentRequest, "wrong"));

        Assertions.assertEquals("수정 권한이 없습니다.", exception.getMessage());
      }
    }
  }

  @Nested
  @DisplayName("댓글 삭제 테스트")
  class DeleteComments {
    private UserDetails mockUser(String email, String role) {
      return User.withUsername(email).password("password").authorities(role).build();
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @Test
      @DisplayName("관리자 계정으로 댓글 삭제 성공")
      @WithMockUser(username = "admin@admin.com", roles = "ADMIN")
      public void deleteCommentsTest() throws BaseException {
        when(commentRepository.findById(99L)).thenReturn(Optional.ofNullable(comment));

        assertDoesNotThrow(
            () -> commentService.delCommentsById(99L, mockUser("admin@admin.com", "ROLE_ADMIN")));

        verify(commentRepository, times(1)).delete(comment);
      }

      @Test
      @DisplayName("본인 댓글 삭제 성공")
      @WithMockUser(username = "user1@naver.com", roles = "USER")
      public void deleteOwnCommentsTest() throws BaseException {
        Member userMember = Member.builder().email("user1@naver.com").role(MemberRole.USER).build();
        Comment delComment = Comment.builder().id(90L).member(userMember).content("delete").build();

        when(commentRepository.findById(90L)).thenReturn(Optional.ofNullable(delComment));

        assertDoesNotThrow(
            () -> commentService.delCommentsById(90L, mockUser("user1@naver.com", "ROLE_USER")));
        verify(commentRepository, times(1)).delete(delComment);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @Test
      @DisplayName("본인 댓글이 아닌 댓글 삭제 시도")
      @WithMockUser(username = "user1@naver.com", roles = "USER")
      void deleteCommentNotOwnerTest() {
        when(commentRepository.findById(99L)).thenReturn(Optional.ofNullable(comment));

        BaseException exception =
            assertThrows(
                BaseException.class,
                () ->
                    commentService.delCommentsById(99L, mockUser("user1@naver.com", "ROLE_USER")));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(commentRepository, never()).delete(comment);
      }

      @Test
      @DisplayName("존재하지 않는 댓글 삭제 시도")
      void notExistDeleteCommentTest() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> commentService.delCommentsById(1L, mockUser("user1@naver.com", "ROLE_USER")));
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(commentRepository, never()).delete(comment);
      }
    }
  }
}
