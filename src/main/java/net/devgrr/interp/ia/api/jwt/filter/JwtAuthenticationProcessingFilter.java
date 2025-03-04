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

  private final JwtService jwtService;
  private final MemberRepository memberRepository;
  private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // RefreshToken 존재 -> 유효하면 AccessToken 재발급
    String refreshToken =
        jwtService.extractRefreshToken(request).filter(jwtService::isTokenValid).orElse(null);
    if (refreshToken != null) {
      checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
      return;
    }

    // AccessToken 만 존재 -> AccessToken 검사 후 유저정보 저장, 필터 계속 진행
    checkAccessTokenAndAuthentication(request, response, filterChain);
  }

  private void checkRefreshTokenAndReIssueAccessToken(
      HttpServletResponse response, String refreshToken) {

    memberRepository
        .findByRefreshToken(refreshToken)
        .ifPresent(
            member ->
                jwtService.sendAccessToken(
                    response, jwtService.createAccessToken(member.getEmail())));
  }

  private void checkAccessTokenAndAuthentication(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    jwtService
        .extractAccessToken(request)
        .filter(jwtService::isTokenValid)
        .flatMap(
            accessToken ->
                jwtService.extractUserId(accessToken).flatMap(memberRepository::findByEmail))
        .ifPresent(this::saveAuthentication);

    filterChain.doFilter(request, response);
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
