package net.devgrr.interp.ia.api.login.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.jwt.JwtService;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@RequiredArgsConstructor
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final MemberRepository memberRepository;
  @Autowired private MemberMapper memberMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    String email = extractUsername(authentication);
    String accessToken = jwtService.createAccessToken(email);
    String refreshToken = jwtService.createRefreshToken();

    jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

    memberRepository
        .findByEmail(email)
        .ifPresent(
            member ->
                memberRepository.save(
                    memberMapper.updateMemberRefreshToken(
                        Member.builder().refreshToken(refreshToken).build(), member)));
  }

  private String extractUsername(Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    return userDetails.getUsername();
  }
}
