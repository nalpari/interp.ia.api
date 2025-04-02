package net.devgrr.interp.ia.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
    @Schema(description = "회원 Email")
        @NotBlank(message = "필수값: email", groups = MemberValidationGroup.loginGroup.class)
        String email,
    @Schema(description = "비밀번호")
        @NotBlank(message = "필수값: password", groups = MemberValidationGroup.loginGroup.class)
        String password) {}
