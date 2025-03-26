package net.devgrr.interp.ia.api.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.jwt.JwtService;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
  private static final String JWT_REQUEST_URL = "/refresh";

  private final JwtService jwtService;
  private final MemberRepository memberRepository;
  private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getRequestURI().equals(JWT_REQUEST_URL)) {
      checkRefreshTokenAndReIssueAccessToken(request, response, filterChain);
    }

    checkAccessTokenAndAuthentication(request, response);

    filterChain.doFilter(request, response);
  }

  private void checkRefreshTokenAndReIssueAccessToken(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String refreshToken = jwtService.extractRefreshToken(request).orElse(null);

    if (refreshToken == null) {
      // refreshToken == null : 401 error
      setException(request, "token is null");
      return;
    }

    if (!jwtService.isTokenValid(request, refreshToken)) {
      return;
    }

    Member member = memberRepository.findByRefreshToken(refreshToken).orElse(null);
    if (member == null) {
      // RefreshToken 이 저장된 Member 없음 : 401 error
      setException(request, "cannot find member by Refresh Token");
      return;
    }

    // Access Token 재발급
    // securityContext 에 refreshToken 가지고 있는 Member 정보 저장
    // /refresh => User 이상 권한 필요함
    saveAuthentication(member);
    jwtService.sendAccessToken(response, jwtService.createAccessToken(member.getEmail()));
  }

  private void setException(HttpServletRequest request, String message) {
    // exception handling
    request.setAttribute("exception", new AccessDeniedException(message));
  }

  private void checkAccessTokenAndAuthentication(
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String accessToken = jwtService.extractAccessToken(request).orElse(null);
    if (accessToken == null) {
      // refreshToken == null : 401 error
      setException(request, "token is null");
      return;
    }
    if (!jwtService.isTokenValid(request, accessToken)) {
      return;
    }

    jwtService
        .extractUserId(accessToken)
        .flatMap(memberRepository::findByEmail)
        .ifPresent(this::saveAuthentication);
  }

  private void saveAuthentication(Member member) {
    UserDetails user =
        User.builder()
            .username(member.getEmail())
            .password(member.getPassword())
            .roles(member.getRole().name())
            .build();

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            user, null, authoritiesMapper.mapAuthorities(user.getAuthorities()));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
