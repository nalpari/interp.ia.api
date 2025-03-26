package net.devgrr.interp.ia.api.jwt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@Tag(name = "JwtController", description = "토큰 API")
@RestController
public class JwtController {
    @Operation(description = "refreshToken 으로 새로운 accessToken을 받는다.")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public void getNewAccessToken() {}
}
