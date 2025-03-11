package net.devgrr.interp.ia.api.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberUpdateRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class memberServiceTest {
  @Mock MemberRepository memberRepository;
  @Mock MemberMapper memberMapper;

  private Member member;
  private MemberRequest memberRequest;

  @BeforeEach
  void setUp() {
    // given
    member =
        Member.builder()
            .email("test@test.com")
            .name("test")
            .image(null)
            .department("department")
            .position("position")
            .job("job")
            .phone(null)
            .isActive(true)
            .build();

    memberRequest =
        new MemberRequest(
            "test@test.com", "test", "test", null, "position", "department", "job", null, null);
  }

  @Nested
  @DisplayName("회원가입 테스트")
  class SetUsers {
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @Test
      @DisplayName("회원가입 성공")
      public void setUsersTest() throws BaseException {
        // mock 설정
        when(memberRepository.existsByEmail(memberRequest.email())).thenReturn(Boolean.FALSE);
        when(memberMapper.toMember(memberRequest)).thenReturn(member);
        when(memberRepository.save(member)).thenReturn(member);

        // Service 생성
        MemberService memberService = new MemberService(memberRepository, memberMapper);
        // when
        Member result = memberService.setUsers(memberRequest);

        // then
        Assertions.assertEquals(member, result);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @Test
      @DisplayName("중복 이메일로 회원가입을 시도한 경우")
      public void SignupToDuplicateEmailTest() {
        // mock 설정
        when(memberRepository.existsByEmail(memberRequest.email())).thenReturn(Boolean.TRUE);

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        BaseException exception =
            assertThrows(BaseException.class, () -> memberService.setUsers(memberRequest));

        Assertions.assertEquals("이미 존재하는 Email 입니다.", exception.getMessage());
      }

      @Test
      @DisplayName("Repository 에 Member 객체 저장이 실패한 경우")
      public void failToSignupTest() {
        // mock 설정
        when(memberRepository.existsByEmail(memberRequest.email())).thenReturn(Boolean.FALSE);
        // memberRepository.save 의 반환값이 null -> 객체 저장에 실패함
        when(memberRepository.save(member)).thenReturn(null);

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        BaseException exception =
            assertThrows(BaseException.class, () -> memberService.setUsers(memberRequest));

        Assertions.assertEquals("회원가입에 실패했습니다.", exception.getMessage());
      }
    }
  }

  @Nested
  @DisplayName("회원 조회 테스트")
  class GetUserTest {
    private List<Member> members = new ArrayList<>();

    @Nested
    @DisplayName("회원 목록 조회 테스트")
    class GetUserListTest {
      @Nested
      @DisplayName("성콩 케이스")
      class SuccessTest {
        @BeforeEach
        void setUp() {
          members.add(Member.builder().isActive(true).build());
          members.add(Member.builder().isActive(false).build());
        }

        @Test
        @DisplayName("활성화 옵션 없을 때 회원 목록 조회 성공")
        public void getUsersWithNoOptionTest() throws BaseException {
          when(memberRepository.findAll()).thenReturn(members);

          MemberService memberService = new MemberService(memberRepository, memberMapper);
          List<Member> result = memberService.getUsers(null);

          Assertions.assertEquals(members, result);
        }

        @Test
        @DisplayName("활성화 옵션 있을 때 회원 목록 조회 성공")
        public void getUsersWithOptionTest() throws BaseException {
          List<Member> activeMembers =
              members.stream().filter(Member::getIsActive).collect(Collectors.toList());
          when(memberRepository.findAllByIsActiveTrue()).thenReturn(activeMembers);

          MemberService memberService = new MemberService(memberRepository, memberMapper);
          List<Member> result = memberService.getUsers("true");

          Assertions.assertEquals(activeMembers, result);
        }
      }

      @Nested
      @DisplayName("실패 케이스")
      class FailTest {
        @Test
        @DisplayName("활성화 옵션에 true 나 false 이외의 문자열이 들어왔을 경우")
        public void getUsersWithOtherStringTest() {
          MemberService memberService = new MemberService(memberRepository, memberMapper);
          BaseException exception =
              assertThrows(BaseException.class, () -> memberService.getUsers("else"));

          Assertions.assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        }
      }
    }

    @Nested
    @DisplayName("회원 조회 테스트")
    class GetUsersByEmail {
      @Nested
      @DisplayName("성공 케이스")
      class SuccessTest {
        @Test
        @DisplayName("Email 로 회원 조회 성공 테스트")
        public void getUsersByEmailTest() throws BaseException {
          when(memberRepository.findByEmail("test@test.com"))
              .thenReturn(Optional.ofNullable(member));

          MemberService memberService = new MemberService(memberRepository, memberMapper);
          Member result = memberService.getUsersByEmail("test@test.com");

          Assertions.assertEquals(member, result);
        }
      }

      @Nested
      @DisplayName("실패 케이스")
      class FailTest {
        @Test
        @DisplayName("존재하지 않는 Email 로 조회 했을 경우")
        public void getUsersByEmailTest() {
          when(memberRepository.findByEmail("nonExist@test.com")).thenReturn(Optional.empty());

          MemberService memberService = new MemberService(memberRepository, memberMapper);
          BaseException exception =
              assertThrows(
                  BaseException.class, () -> memberService.getUsersByEmail("nonExist@test.com"));

          assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        }
      }
    }
  }

  @Nested
  @DisplayName("회원 수정 테스트")
  class PutUser {
    private MemberUpdateRequest memberUpdateRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
      memberUpdateRequest =
          new MemberUpdateRequest("test", "test", null, "test", "test", "test", null, null);
      userDetails = User.withUsername("test@test.com").password("test").build();
    }

    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @DisplayName("회원 수정 성공")
      @Test
      public void putUserTest() throws BaseException {
        when(memberRepository.findByEmail(userDetails.getUsername()))
            .thenReturn(Optional.ofNullable(member));

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        memberService.putUsers(userDetails, memberUpdateRequest);

        verify(memberMapper, times(1)).updateMember(memberUpdateRequest, member);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @DisplayName("존재하지 않는 이메일로 수정 시도 할 경우")
      @Test
      public void putUserNonExistMemberTest() throws BaseException {
        when(memberRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> memberService.putUsers(userDetails, memberUpdateRequest));

        Assertions.assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
      }

      @DisplayName("DB 업데이트 중 오류 발생한 경우")
      @Test
      public void putUserWithExceptionTest() throws BaseException {
        when(memberRepository.findByEmail(userDetails.getUsername()))
            .thenReturn(Optional.ofNullable(member));
        doThrow(new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "수정에 실패했습니다."))
            .when(memberMapper)
            .updateMember(memberUpdateRequest, member);

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> memberService.putUsers(userDetails, memberUpdateRequest));

        Assertions.assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
      }
    }
  }

  @Nested
  @DisplayName("회원 활성화/비활성화 테스트")
  class putUserActive {
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @DisplayName("회원 활성화 성공 케이스")
      @Test
      public void putUserActiveByEmailTest() throws BaseException {
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.ofNullable(member));

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        memberService.putUsersActiveByEmail("test@test.com");

        verify(memberMapper, times(1)).activeMember(member, member);
      }

      @DisplayName("회원 비활성화 성공 케이스")
      @Test
      public void putUserDeactivateByEmailTest() throws BaseException {
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.ofNullable(member));

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        memberService.putUsersDeactivateByEmail("test@test.com");

        verify(memberMapper, times(1)).deactivateMember(member, member);
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @DisplayName("존재하지 않는 이메일로 수정 시도 할 경우")
      @Test
      public void putUserNonExistMemberTest() {
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        BaseException exception =
            assertThrows(
                BaseException.class, () -> memberService.putUsersActiveByEmail("test@test.com"));

        Assertions.assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
      }

      @DisplayName("DB 업데이트 중 오류 발생한 경우")
      @Test
      public void putUserWithExceptionTest() throws BaseException {
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.ofNullable(member));
        doThrow(new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "수정에 실패했습니다."))
            .when(memberMapper)
            .deactivateMember(member, member);

        MemberService memberService = new MemberService(memberRepository, memberMapper);
        BaseException exception =
            assertThrows(
                BaseException.class,
                () -> memberService.putUsersDeactivateByEmail("test@test.com"));

        Assertions.assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
      }
    }
  }
}
