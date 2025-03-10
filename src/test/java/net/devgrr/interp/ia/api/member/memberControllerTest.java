package net.devgrr.interp.ia.api.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import net.devgrr.interp.ia.api.jwt.JwtService;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberUpdateRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class memberControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;
  @Autowired private JwtService jwtService;
  @MockBean private MemberService memberServiceMock;

  private Member member;
  private MemberRequest memberRequest;
  private String accessToken;

  @BeforeEach
  public void setUp() {
    // given
    accessToken = jwtService.createAccessToken("admin@admin.com");
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

  @DisplayName("회원가입 테스트")
  @Nested
  public class setMembers {
    @DisplayName("성공 케이스")
    @Nested
    class SuccessTest {
      @DisplayName("회원가입 성공")
      @Test
      public void setMemberTest() throws Exception {
        //  given
        String json = mapper.writeValueAsString(memberRequest);

        // mock 설정
        // 실제 service 를 호출하지 않고 mocking 한 service 객체를 호출하여 해당 메소드가 호출 되었다면 member 를 반환하도록 함
        when(memberServiceMock.setUsers(memberRequest)).thenReturn(member);

        // when
        MvcResult result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        // then
        JSONObject resp = new JSONObject(result.getResponse().getContentAsString());
        Assertions.assertAll(
            "Signup Method Response Object",
            () -> assertEquals(memberRequest.email(), resp.get("email"), "이메일 값이 다릅니다."),
            () -> assertEquals(memberRequest.name(), resp.get("name"), "이름 값이 다릅니다."),
            () -> assertEquals(memberRequest.position(), resp.get("position"), "직급 값이 다릅니다."),
            () -> assertEquals(memberRequest.department(), resp.get("department"), "부서 값이 다릅니다."),
            () -> assertEquals(memberRequest.job(), resp.get("job"), "직무 값이 다릅니다."),
            () -> assertEquals(true, resp.get("isActive"), "회원은 활성화 상태여야 합니다."),
            //        expect null
            () -> assertEquals(JSONObject.NULL, resp.get("image"), "이미지 값은 null 이어야 합니다."),
            () -> assertEquals(JSONObject.NULL, resp.get("phone"), "전화번호 값은 null 이어야 합니다."));
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @Test
      @DisplayName("RequestBody 없이 회원가입 요청을 시도한 경우")
      public void signupWithNoRequestBodyTest() throws Exception {
        // when & then
        MvcResult result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andReturn();
        //  then
        JSONObject resp = new JSONObject(result.getResponse().getContentAsString());
        Assertions.assertEquals("INTERNAL_SERVER_ERROR", resp.get("status"));
      }

      @Test
      @DisplayName("필수값(Email)을 기입하지 않고 회원가입 시도한 경우")
      public void signupWithNoEmailTest() throws Exception {
        //        given
        memberRequest =
            new MemberRequest(
                null, "test", "test", null, "position", "department", "job", null, null);
        String json = mapper.writeValueAsString(memberRequest);
        //        when
        MvcResult result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
        //        then
        JSONObject resp = new JSONObject(result.getResponse().getContentAsString());
        Assertions.assertEquals("BAD_REQUEST", resp.get("status"));
      }
    }
  }

  @Nested
  @DisplayName("사용자 조회 테스트")
  public class getMembers {
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      @DisplayName("사용자 조회 테스트")
      @Test
      public void getUserByEmailTest() throws Exception {
        // mock 설정
        when(memberServiceMock.getUsersByEmail("test@test.com")).thenReturn(member);

        // when & then
        MvcResult result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/api/users/test@test.com")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // then
        JSONObject resp = new JSONObject(result.getResponse().getContentAsString());
        assertEquals(member.getEmail(), resp.get("email"));
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      @DisplayName("pathVariable 에 빈 값으로 사용자 조회 할 경우")
      @Test
      public void getUserWithNoEmailTest() throws Exception {
        // when & then
        MvcResult result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/api/users/")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andReturn();

        // then
        JSONObject resp = new JSONObject(result.getResponse().getContentAsString());
        Assertions.assertEquals("INTERNAL_SERVER_ERROR", resp.get("status"));
      }

      @DisplayName("로그인 정보 없이(Authorization header) 사용자 조회 시도 할 경우")
      @Test
      public void getUserWithNoAuthTest() throws Exception {
        // when & then
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/users/").contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andReturn();
      }
    }
  }

  @Nested
  @DisplayName("전체 사용자 목록 조회 테스트")
  class getUsers {
    private final List<Member> members = new ArrayList<>();

    @BeforeEach
    void setUp() {
      // given
      members.add(Member.builder().isActive(true).build());
      members.add(Member.builder().isActive(false).build());
    }

    @DisplayName("활성화 조건이 없을 때 (isActive == null)")
    @Test
    public void getUsersTest() throws Exception {
      // mock 설정
      when(memberServiceMock.getUsers(null)).thenReturn(members);

      // when & then
      MvcResult result =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get("/api/users")
                      .header("Authorization", "Bearer " + accessToken)
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andReturn();

      // then
      JSONArray resp = new JSONArray(result.getResponse().getContentAsString());
      assertEquals(members.size(), resp.length());
    }

    @DisplayName("활성화 조건이 ture 일 때 (isActive == true)")
    @Test
    public void getUsersWithActiveTest() throws Exception {
      // mock 설정
      when(memberServiceMock.getUsers("true"))
          .thenReturn(members.stream().filter(Member::getIsActive).toList());

      // when & then
      MvcResult result =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get("/api/users")
                      .param("isActive", "true")
                      .header("Authorization", "Bearer " + accessToken)
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(MockMvcResultMatchers.status().isOk())
              .andReturn();

      // then
      JSONArray resp = new JSONArray(result.getResponse().getContentAsString());
      assertEquals(1, resp.length());
      assertTrue(resp.getJSONObject(0).getBoolean("isActive"));
    }
  }

  @Nested
  @DisplayName("사용자 정보 수정 테스트")
  class putUsers {
    private MemberUpdateRequest memberUpdateRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
      // given
      memberUpdateRequest =
          new MemberUpdateRequest(
              "test", "newTest", null, "newPosition", "newDepartment", "newJob", null, null);

      userDetails = new User("admin@admin.com", "pwd", new ArrayList<>());
    }

    @Nested
    @DisplayName("성공 케이스")
    public class SuccessTest {
      @DisplayName("사용자 정보 수정 테스트")
      @Test
      public void putUsersTest() throws Exception {
        // mock 설정
        doAnswer(invocationOnMock -> null)
            .when(memberServiceMock)
            .putUsers(userDetails, memberUpdateRequest);

        // given
        String json = mapper.writeValueAsString(memberUpdateRequest);

        // when & then
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/api/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    public class FailTest {

      @DisplayName("로그인 정보 없이(Authorization header) 회원 정보 수정 요청한 경우")
      @Test
      public void putUsersWithNoPrincipalTest() throws Exception {
        // given
        accessToken = jwtService.createAccessToken("");
        String json = mapper.writeValueAsString(memberUpdateRequest);

        // when & then
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/api/users")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andReturn();
      }
    }
  }

  @DisplayName("사용자 활성화/비활성화 테스트")
  @Nested
  public class putUsersActive {

    @DisplayName("계정 활성화 테스트")
    @Test
    public void putUsersActiveTest() throws Exception {
      // mock 설정
      doAnswer(invocationOnMock -> null)
          .when(memberServiceMock)
          .putUsersActiveByEmail(member.getEmail());

      // when & then
      mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/api/users/" + member.getEmail() + "/activate")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    }

    @DisplayName("계정 비활성화 테스트")
    @Test
    public void putUsersDeactivateTest() throws Exception {
      // mock 설정
      doAnswer(invocationOnMock -> null)
          .when(memberServiceMock)
          .putUsersActiveByEmail(member.getEmail());

      // when & then
      mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/api/users/" + member.getEmail() + "/deactivate")
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    }
  }
}
