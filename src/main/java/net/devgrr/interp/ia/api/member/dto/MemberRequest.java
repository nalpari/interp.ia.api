package net.devgrr.interp.ia.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 요청")
public record MemberRequest(
    @Schema(description = "이메일")
        @NotBlank(message = "필수값: email", groups = MemberValidationGroup.createGroup.class)
        @Email(message = "유효하지 않은 이메일 형식입니다.", groups = MemberValidationGroup.createGroup.class)
        String email,
    @Schema(description = "이름")
    @NotBlank(message = "필수값: name", groups = MemberValidationGroup.createGroup.class)
    String name,
    @Schema(description = "비밀번호")
    @NotBlank(message = "필수값: password", groups = MemberValidationGroup.createGroup.class)
    String password,
    @Schema(description = "부서")
    @NotBlank(message = "필수값: department", groups = MemberValidationGroup.createGroup.class)
    String department,
    @Schema(description = "직무")
    @NotBlank(message = "필수값: job", groups = MemberValidationGroup.createGroup.class)
    String job,
    @Schema(description = "직급")
    @NotBlank(message = "필수값: position", groups = MemberValidationGroup.createGroup.class)
    String position,
    @Schema(description = "전화번호") String phone,
    @Schema(description = "회원 이미지") String image,
    @Schema(description = "권한") String role) {}
