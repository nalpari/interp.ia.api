package net.devgrr.interp.ia.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 정보 수정 요청")
public record MemberUpdateRequest(
    @Schema(description = "고유 ID") Long id,
    @Schema(description = "이메일") @Email(message = "유효하지 않은 이메일 형식입니다.") String email,
    @Schema(description = "비밀번호") String password,
    @Schema(description = "이름") String name,
    @Schema(description = "회원 이미지") String image,
    @Schema(description = "직급") String position,
    @Schema(description = "직급") String department,
    @Schema(description = "직급") String job,
    @Schema(description = "전화번호") String phone,
    @Schema(description = "권한") String role) {}
