package net.devgrr.interp.ia.api.member;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class memberServiceTest {
  @Mock MemberRepository memberRepository;
  @Mock MemberMapper memberMapper;

  @Nested
  @DisplayName("회원가입 테스트")
  class SetUsers {
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
        BaseException exception = assertThrows(BaseException.class, () -> memberService.setUsers(memberRequest));

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
        BaseException exception = assertThrows(BaseException.class, () -> memberService.setUsers(memberRequest));

        Assertions.assertEquals("회원가입에 실패했습니다.", exception.getMessage());
      }
    }
  }
}
