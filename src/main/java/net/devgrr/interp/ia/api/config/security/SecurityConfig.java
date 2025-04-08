package net.devgrr.interp.ia.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.jwt.JwtService;
import net.devgrr.interp.ia.api.jwt.exceptionHandler.JwtAuthenticationEntryPoint;
import net.devgrr.interp.ia.api.jwt.filter.JwtAuthenticationProcessingFilter;
import net.devgrr.interp.ia.api.login.LoginService;
import net.devgrr.interp.ia.api.login.filter.JsonUsernamePasswordAuthenticationFilter;
import net.devgrr.interp.ia.api.login.handler.LoginFailureHandler;
import net.devgrr.interp.ia.api.login.handler.LoginSuccessJWTProvideHandler;
import net.devgrr.interp.ia.api.member.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final LoginService loginService;
  private final ObjectMapper objectMapper;
  private final MemberRepository memberRepository;
  private final JwtService jwtService;
  private final ApiLoggingFilter apiLoggingFilter;

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            (sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            (authorizeHttpRequests) ->
                authorizeHttpRequests
                    .dispatcherTypeMatchers(DispatcherType.ASYNC)
                    .hasAnyRole("USER", "ADMIN")
                    .requestMatchers(
                        new AntPathRequestMatcher("/swagger-ui/**"),
                        new AntPathRequestMatcher("/v3/api-docs/**"),
                        new AntPathRequestMatcher("/test/**"))
                    .permitAll()
                    .requestMatchers(
                        new AntPathRequestMatcher("/login"),
                        new AntPathRequestMatcher("/api/users/signup"),
                        new AntPathRequestMatcher("/error"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/admin"))
                    .hasRole("ADMIN")
                    .requestMatchers(new AntPathRequestMatcher("/api/**/export"))
                    .hasAnyRole("USER", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(apiLoggingFilter, LogoutFilter.class)
        .addFilterBefore(jwtAuthenticationProcessingFilter(), LogoutFilter.class)
        .addFilterBefore(
            jsonUsernamePasswordLoginFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exceptionHandler -> {
              exceptionHandler.authenticationEntryPoint(jwtAuthenticationEntryPoint);
            });

    return httpSecurity.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(Collections.singletonList("*"));
    config.setAllowedMethods(Arrays.asList("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Collections.singletonList("*"));
    config.setExposedHeaders(
        Arrays.asList("Authorization", "Content-Disposition", "Content-Length", "Content-Type"));
    config.setAllowCredentials(true);

    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder());
    provider.setUserDetailsService(loginService);
    return new ProviderManager(provider);
  }

  @Bean
  public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler() {
    return new LoginSuccessJWTProvideHandler(jwtService, memberRepository);
  }

  @Bean
  public LoginFailureHandler loginFailureHandler() {
    return new LoginFailureHandler();
  }

  @Bean
  public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() {
    JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter =
        new JsonUsernamePasswordAuthenticationFilter(objectMapper);
    jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
    jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(
        loginSuccessJWTProvideHandler());
    jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
    return jsonUsernamePasswordLoginFilter;
  }

  @Bean
  public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
    return new JwtAuthenticationProcessingFilter(jwtService, memberRepository);
  }
}
