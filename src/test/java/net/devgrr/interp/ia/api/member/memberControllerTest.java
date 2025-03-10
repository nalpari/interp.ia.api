package net.devgrr.interp.ia.api.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class memberControllerTest {
  @Autowired private ObjectMapper mapper;
  @Autowired private MemberRepository memberRepository;
  @Autowired private MemberMapper memberMapper;
  @Autowired private MockMvc mockMvc;

  @MockBean private MemberService memberServiceMock;

  private Member member;
  private MemberRequest memberRequest;

  @BeforeEach
  public void setUp() throws Exception {
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
  @Test
  public void setMemberTest() throws Exception {
    //  given
    String json = mapper.writeValueAsString(memberRequest);

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
    System.out.println(result.getResponse());
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
