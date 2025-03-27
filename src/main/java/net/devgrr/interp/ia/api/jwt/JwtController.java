package net.devgrr.interp.ia.api.jwt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@Tag(name = "JwtController", description = "토큰 API")
@RestController
public class JwtController {
    @Operation(description = "refreshToken 으로 새로운 accessToken을 받는다.")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public void getNewAccessToken() {}

    @Operation(description = "/admin 엔드포인트 테스트 컨트롤러")
    @GetMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    public String getAdmin() {
        return "admin";
    }
}
