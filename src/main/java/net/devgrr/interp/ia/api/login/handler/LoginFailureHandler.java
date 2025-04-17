package net.devgrr.interp.ia.api.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.exception.ErrorResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {

    String errMsg = exception.getMessage();
    Map<String, Object> responseBody = new HashMap<>();
    response.setContentType(CONTENT_TYPE);

    if (errMsg.contains("Method not supported")) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);

      responseBody.put("error", ErrorCode.METHOD_NOT_ALLOWED);
      responseBody.put("message", errMsg);

      mapper.writeValue(response.getWriter(), responseBody);
    } else if (exception instanceof DisabledException) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);

      responseBody.put("error", ErrorCode.FORBIDDEN);
      responseBody.put("message", "비활성화 된 계정입니다.");

      mapper.writeValue(response.getWriter(), responseBody);
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

      responseBody.put("error", ErrorCode.FORBIDDEN);
      responseBody.put("message", errMsg);

      mapper.writeValue(response.getWriter(), responseBody);
    }
  }
}
