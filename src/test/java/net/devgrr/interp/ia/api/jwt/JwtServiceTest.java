package net.devgrr.interp.ia.api.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import net.devgrr.interp.ia.api.member.dto.LoginRequest;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtServiceTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Autowired private JwtService jwtService;

  private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

  @Test
  @DisplayName("로그인 이후 필터를 통한 유효 토큰 발급 테스트")
  public void GetAdminTokensTest() throws Exception {
    // when
    LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin");
    String loginJson = mapper.writeValueAsString(loginRequest);

    // given
    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

    String accessToken = mvcResult.getResponse().getHeader("authorization");
    String refreshToken = mvcResult.getResponse().getHeader("authorization-refresh");

    // then
    Assertions.assertTrue(jwtService.isTokenValid(request, accessToken));
    Assertions.assertTrue(jwtService.isTokenValid(request, refreshToken));
  }

  @Nested
  @DisplayName("권한 검증 테스트")
  class PermissionVerificationTest {
    @Nested
    @DisplayName("성공 케이스")
    class SuccessTest {
      private String adminAccessToken;

      @BeforeEach
      void setAdminAccessToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin");
        String loginJson = mapper.writeValueAsString(loginRequest);

        MvcResult mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        adminAccessToken = "Bearer " + mvcResult.getResponse().getHeader("authorization");
      }

      @Test
      @DisplayName("admin 권한으로 /admin endpoint 접근")
      public void accessAdminEndpointTest() throws Exception {
        MvcResult mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/admin")
                        .header("Authorization", adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
        Assertions.assertEquals("admin", mvcResult.getResponse().getContentAsString());
      }
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailTest {
      private String userAccessToken;

      @BeforeEach
      void setUserAccessToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user1@naver.com", "password");
        String loginJson = mapper.writeValueAsString(loginRequest);

        MvcResult mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        userAccessToken = "Bearer " + mvcResult.getResponse().getHeader("authorization");
      }

      @Test
      @DisplayName("user 권한으로 /admin endpoint 접근")
      public void accessUserEndpointTest() throws Exception {
        MvcResult result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/admin")
                        .header("Authorization", userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();

        Assertions.assertEquals(403, result.getResponse().getStatus());
      }
    }
  }

  @Nested
  @DisplayName("토큰 재발급 테스트")
  class TokenReissueTest {
    @Nested
    @DisplayName("잘못된 토큰으로 접근 시 401 반환 테스트")
    class WrongTokenTest {
      @Test
      @DisplayName("만료된 토큰 테스트")
      public void expiredTokenTest() throws Exception {
        String expiredToken =
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBY2Nlc3NUb2tlbiIsImV4cCI6MTc0Mjk1NjM5NSwidXNlcm5hbWUiOiJhZG1pbkBhZG1pbi5jb20ifQ.wNDaiRdgp-862cLkctDsYD9a_FSTZpdhor73F7hhj7IXf6HGFHSgcQNOv2cfW81WzG6pQ9fe2fVP_2rAEuATcQ";

        MvcResult mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/api/users")
                        .header("Authorization", expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        Assertions.assertEquals(401, mvcResult.getResponse().getStatus());
      }
      @Test
      @DisplayName("시그니처가 맞지 않는 토큰 테스트")
      public void invalidTokenTest() throws Exception {
        String expiredToken =
                "Bearer eyJ0eXAiOiJKV1QiLCaaaaaaaIUzUxMiJ9.eyJzdWIiOiJBY2Nlc3NUb2tlbiIsImV4cCI6MTc0Mjk1NjM5NSwidXNlcm5hbWUiOiJhZG1pbkBhZG1pbi5jb20ifQ.wNDaiRdgp-862cLkctDsYD9a_FSTZpdhor73F7hhj7IXf6HGFHSgcQNOv2cfW81WzG6pQ9fe2fVP_2rAEuATcQ";

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders.get("/api/users")
                                        .header("Authorization", expiredToken)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                        .andReturn();

        Assertions.assertEquals(401, mvcResult.getResponse().getStatus());
      }
    }
    @Nested
    @DisplayName("/refresh endpoint 로 accessToken 재발급 테스트")
    class RefreshTokenTest {
      @Nested
      @DisplayName("성공 케이스")
      class SuccessTest {
        private String refreshToken;

        @BeforeEach
        public void setUserAccessToken() throws Exception {
          LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin");
          String loginJson = mapper.writeValueAsString(loginRequest);

          MvcResult mvcResult =
                  mockMvc
                          .perform(
                                  MockMvcRequestBuilders.post("/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(loginJson))
                          .andExpect(MockMvcResultMatchers.status().isOk())
                          .andReturn();
          refreshToken = "Bearer " + mvcResult.getResponse().getHeader("authorization-refresh");
        }
        @Test
        public void getNewAccessToken() throws Exception {
          MvcResult mvcResult =
                  mockMvc
                          .perform(
                                  MockMvcRequestBuilders.post("/refresh")
                                          .header("Authorization-refresh", refreshToken)
                                          .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(MockMvcResultMatchers.status().isOk())
                          .andReturn();

          Assertions.assertEquals(200, mvcResult.getResponse().getStatus());

          String newAccessToken = mvcResult.getResponse().getHeader("Authorization");
          Assertions.assertTrue(jwtService.isTokenValid(request, newAccessToken));
        }
      }
      @Nested
      @DisplayName("실패 케이스")
      class FailTest {
        @Test
        @DisplayName("만료된 refreshToken 으로 재발급 요청 테스트")
        public void expiredRefreshTokenTest() throws Exception {
          String expiredToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSZWZyZXNoVG9rZW4iLCJleHAiOjE3NDI5NTc1OTV9.53r9pzNj9tjfuVhh0qre6Tt0Bsc6NUFhwdzKlk_1vsNStnMgrBCZCxXwJuln2PgVKFfOKrEop--7y95Le1CEnw";
          MvcResult mvcResult =
                  mockMvc
                          .perform(
                                  MockMvcRequestBuilders.post("/refresh")
                                          .header("Authorization-refresh", expiredToken)
                                          .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                          .andReturn();

          Assertions.assertEquals(401, mvcResult.getResponse().getStatus());
        }
        @Test
        @DisplayName("시그니처가 잘못된 refreshToken 으로 재발급 요청 테스트")
        public void invalidRefreshTokenTest() throws Exception {
          String invalidToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIaaaaaaaa.eyJzdWIiOiJSZWZyZXNoVG9rZW4iLCJleHAiOjE3NDI5NTc1OTV9.53r9pzNj9tjfuVhh0qre6Tt0Bsc6NUFhwdzKlk_1vsNStnMgrBCZCxXwJuln2PgVKFfOKrEop--7y95Le1CEnw";
          MvcResult mvcResult =
                  mockMvc
                          .perform(
                                  MockMvcRequestBuilders.post("/refresh")
                                          .header("Authorization-refresh", invalidToken)
                                          .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                          .andReturn();

          Assertions.assertEquals(401, mvcResult.getResponse().getStatus());
        }
        @Test
        @DisplayName("빈 토큰으로 재발급 요청 테스트")
        public void nullTokenTest() throws Exception {
          MvcResult mvcResult =
                  mockMvc
                          .perform(
                                  MockMvcRequestBuilders.post("/refresh")
                                          .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                          .andReturn();

          Assertions.assertEquals(401, mvcResult.getResponse().getStatus());
        }
      }
    }
  }
}
