package net.devgrr.interp.ia.api.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
public class JwtService {

  private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
  private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
  private static final String USERID_CLAIM = "username";
  private static final String BEARER = "Bearer ";
  private final MemberRepository memberRepository;
  @Autowired private MemberMapper memberMapper;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access.expiration}")
  private long accessTokenValidityInSeconds;

  @Value("${jwt.refresh.expiration}")
  private long refreshTokenValidityInSeconds;

  @Value("${jwt.access.header}")
  private String accessHeader;

  @Value("${jwt.refresh.header}")
  private String refreshHeader;

  public String createAccessToken(String userId) {
    return JWT.create()
        .withSubject(ACCESS_TOKEN_SUBJECT)
        .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000))
        .withClaim(USERID_CLAIM, userId)
        .sign(Algorithm.HMAC512(secret));
  }

  public String createRefreshToken() {
    return JWT.create()
        .withSubject(REFRESH_TOKEN_SUBJECT)
        .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
        .sign(Algorithm.HMAC512(secret));
  }

  public void updateRefreshToken(String userId, String refreshToken) throws Exception {
    memberRepository
        .findByUserId(userId)
        .ifPresentOrElse(
            member ->
                memberRepository.save(
                    memberMapper.updateMemberRefreshToken(
                        Member.builder().refreshToken(refreshToken).build(), member)),
            () -> new Exception("Not found user"));
  }

  public void destroyRefreshToken(String userId) throws Exception {
    memberRepository
        .findByUserId(userId)
        .ifPresentOrElse(
            member ->
                memberRepository.save(
                    memberMapper.updateMemberRefreshToken(
                        Member.builder().refreshToken(null).build(), member)),
            () -> new Exception("Not found user"));
  }

  public void sendAccessAndRefreshToken(
      HttpServletResponse response, String accessToken, String refreshToken) {
    response.setStatus(HttpServletResponse.SC_OK);
    setAccessTokenHeader(response, accessToken);
    setRefreshTokenHeader(response, refreshToken);
  }

  public void sendAccessToken(HttpServletResponse response, String accessToken) {
    response.setStatus(HttpServletResponse.SC_OK);
    setAccessTokenHeader(response, accessToken);
  }

  public Optional<String> extractAccessToken(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(accessHeader))
        .filter(accessToken -> accessToken.startsWith(BEARER))
        .map(accessToken -> accessToken.replace(BEARER, ""));
  }

  public Optional<String> extractRefreshToken(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(refreshHeader))
        .filter(refreshToken -> refreshToken.startsWith(BEARER))
        .map(refreshToken -> refreshToken.replace(BEARER, ""));
  }

  public Optional<String> extractUserId(String accessToken) {
    try {
      return Optional.ofNullable(
          JWT.require(Algorithm.HMAC512(secret))
              .build()
              .verify(accessToken)
              .getClaim(USERID_CLAIM)
              .asString());
    } catch (Exception e) {
      System.out.println("[ERROR] extractUserId : " + e.getMessage());
      return Optional.empty();
    }
  }

  public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
    response.setHeader(accessHeader, accessToken);
  }

  public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
    response.setHeader(refreshHeader, refreshToken);
  }

  public boolean isTokenValid(String token) {
    try {
      JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
      return true;
    } catch (Exception e) {
      System.out.println("[ERROR] Invalid Token : " + e.getMessage());
      return false;
    }
  }
}
