package net.devgrr.interp.ia.api.jwt.exceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final HandlerExceptionResolver handlerExceptionResolver;

  public JwtAuthenticationEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    Exception exception = (Exception) request.getAttribute("exception");

    if (exception != null) {
      handlerExceptionResolver.resolveException(
          request, response, null, (Exception) request.getAttribute("exception"));
    } else {
      handlerExceptionResolver.resolveException(
          request, response, null, new AuthenticationException("권한이 없습니다.", authException){});
    }
  }
}
