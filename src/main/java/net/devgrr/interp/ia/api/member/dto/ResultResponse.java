package net.devgrr.interp.ia.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 활성화/비활성화 성공 여부")
public record ResultResponse(
        @Schema(description = "전달 할 메세지(실행한 메소드 명, 경고 등)") String message,
        @Schema(description = "성공 여부") boolean result
) {
}