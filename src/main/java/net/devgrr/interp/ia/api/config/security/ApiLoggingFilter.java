package net.devgrr.interp.ia.api.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      logRequest(request);
      filterChain.doFilter(request, response);
      logResponse(response);
    } catch (Exception e) {
      log.error("[ERROR] ", e);
      throw e;
    }
  }

  private void logRequest(HttpServletRequest request) {
    log.info(
        "Request: method={}, uri={}, params={}",
        request.getMethod(),
        request.getRequestURI(),
        request.getParameterMap());
  }

  private void logResponse(HttpServletResponse response) {
    if (response.getStatus() / 100 >= 4) {
      log.error("Response: status={}", response.getStatus());
      return;
    }
    log.info("Response: status={}", response.getStatus());
  }
}
