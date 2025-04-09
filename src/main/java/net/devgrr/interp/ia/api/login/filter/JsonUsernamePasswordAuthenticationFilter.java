package net.devgrr.interp.ia.api.login.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

public class JsonUsernamePasswordAuthenticationFilter
    extends AbstractAuthenticationProcessingFilter {

  private static final String DEFAULT_LOGIN_REQUEST_URL = "/login";
  private static final String HTTP_METHOD = "POST";
  private static final String CONTENT_TYPE = "application/json";
  private static final String USER_EMAIL_KEY = "email";
  private static final String PASSWORD_KEY = "password";

  private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
      new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);
  private final ObjectMapper objectMapper;
  private final MemberRepository memberRepository;

  public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, MemberRepository memberRepository) {
    super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
    this.objectMapper = objectMapper;
    this.memberRepository = memberRepository;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {

    if (!request.getContentType().equals(CONTENT_TYPE)) {
      throw new AuthenticationServiceException(
          "Authentication Content-Type not supported: " + request.getContentType());
    }

    String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
    Map<String, String> userEmailPasswordMap =
        objectMapper.readValue(messageBody, new TypeReference<>() {});

    String email = userEmailPasswordMap.get(USER_EMAIL_KEY);

    Member member =
        memberRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("잘못된 이메일: "+email));

    if(!member.getIsActive()) {
      throw new DisabledException("비활성화된 계정입니다.");
    }

    UsernamePasswordAuthenticationToken authRequest =
        new UsernamePasswordAuthenticationToken(
            userEmailPasswordMap.get(USER_EMAIL_KEY), userEmailPasswordMap.get(PASSWORD_KEY));

    return this.getAuthenticationManager().authenticate(authRequest);
  }
}
