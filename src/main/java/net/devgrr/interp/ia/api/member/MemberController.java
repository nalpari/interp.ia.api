package net.devgrr.interp.ia.api.member;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.config.swagger.annotation.SwaggerBody;
import net.devgrr.interp.ia.api.member.dto.*;
import net.devgrr.interp.ia.api.member.file.FileService;
import net.devgrr.interp.ia.api.member.file.dto.MemberFileOptionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "사용자 API")
@RestController
public class MemberController {

  private final FileService memberFileService;
  private final MemberService memberService;
  private final MemberMapper memberMapper;

  @Operation(description = "전체 사용자를 조회한다. <br>isActive가 있을 경우 해당 조건에 맞는 사용자를 조회한다.")
  @GetMapping
  public List<MemberResponse> getUsers(
      @RequestParam(value = "isActive", required = false)
          @Parameter(description = "true = 활성화 / false = 비활성화 ")
          String isActive)
      throws BaseException {
    return memberService.getUsers(isActive).stream()
        .map(memberMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Operation(description = "사용자를 조회한다.")
  @GetMapping("/{email}")
  public MemberResponse getUsersByEmail(@PathVariable("email") String email) throws BaseException {
    return memberMapper.toResponse(memberService.getUsersByEmail(email));
  }

  @Operation(description = "사용자를 생성한다.")
  @JsonView(MemberValidationGroup.createGroup.class)
  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public MemberResponse setUsers(
      @Validated(MemberValidationGroup.createGroup.class) @RequestBody MemberRequest req)
      throws BaseException {
    return memberMapper.toResponse(memberService.setUsers(req));
  }

  @Operation(description = "사용자의 정보를 수정한다.")
  @PutMapping
  public void putUsers(
      @Valid @RequestBody MemberUpdateRequest req, @AuthenticationPrincipal UserDetails userDetails)
      throws BaseException {
    memberService.putUsers(userDetails, req);
  }

  @Operation(description = "사용자의 계정을 비활성화한다..")
  @PatchMapping("/{email}/deactivate")
  public void putUsersDeactivateByEmail(@PathVariable("email") String email) throws BaseException {
    memberService.putUsersDeactivateByEmail(email);
  }

  @Operation(description = "사용자의 계정을 활성화한다.")
  @PatchMapping("/{email}/activate")
  public void putUsersActiveByEmail(@PathVariable("email") String email) throws BaseException {
    memberService.putUsersActiveByEmail(email);
  }

  @Operation(
      description =
          "파일을 입력받아 데이터를 저장한다.<br>확장자가 .csv, .xlsx, .xls 인 것만 가능<br>입력된 데이터 중 같은 email 존재 시 해당 데이터 제외하고 저장")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public void uploadMemberFile(
      @RequestPart("file") @Parameter(description = "MemberRequest 에서 필수 값은 무조건 포함되어야 함<br>email, name, password, department, job, position")
          MultipartFile file)
      throws Exception {
    memberFileService.uploadMemberFile(file);
  }

  @Operation(description = "모든 회원 정보를 파일로 내려받는다.<br>csv 파일과 엑셀 파일 중 선택")
  @SwaggerBody(
      content =
          @Content(
              encoding = @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE)))
  @PostMapping(value = "/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public void downloadMemberFile(
      @Parameter(description = "다운로드 옵션") @RequestPart(value = "dto")
          MemberFileOptionRequest memberFileOptionRequest,
      HttpServletResponse response)
      throws BaseException, IOException {
    File saveFile = memberFileService.downloadMemberFile(memberFileOptionRequest);
    String fileNameEncoded = URLEncoder.encode(saveFile.getName(), StandardCharsets.UTF_8);

    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameEncoded + "\"");

    memberFileService.getStreamingResponse(response, saveFile);
  }
}
