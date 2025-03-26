package net.devgrr.interp.ia.api.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.member.dto.LoginRequest;
import net.devgrr.interp.ia.api.member.dto.MemberValidationGroup;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@Tag(name = "LoginController", description = "로그인 API")
@RestController
public class LoginController {

  @Operation(description = "로그인을 한다.")
  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public void login(
      @Validated(MemberValidationGroup.loginGroup.class) @RequestBody LoginRequest req) {
    // JsonUsernamePasswordAuthenticationFilter 에서 처리
  }
}
