package net.devgrr.interp.ia.api.member.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DB -> (csv, exel) file 변환 시 옵션 요청")
public record MemberFileOptionRequest(
    @Schema(description = "내려받을 파일 타입 (.csv, .xlsx, .xls") String fileType,
    @Schema(description = "내려받을 파일 이름") String fileName,
    @Schema(description = "작성 포맷") String dataFormat,
    @Schema(description = "헤더 옵션") boolean header,
    @Schema(description = "Member 테이블에서 받을 컬럼") List<String> columns) {}
