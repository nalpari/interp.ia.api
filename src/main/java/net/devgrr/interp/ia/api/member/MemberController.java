package net.devgrr.interp.ia.api.member;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.*;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "사용자 API")
@RestController
public class MemberController {

  private final MemberFileService memberFileService;
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

  @Operation(description = "파일을 입력받아 데이터를 저장한다." + "<br>확장자가 .csv, .xlsx, .xls 인 것만 가능"
  +"<br> ")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public void uploadMemberFile(@RequestPart("file") MultipartFile file)
      throws IOException, JobExecutionException, BaseException {
    memberFileService.uploadMemberFile(file);
  }

  @Operation(description = "모든 회원 정보를 파일로 내려받는다." + "<br>csv 파일과 엑셀 파일 중 선택")
  @GetMapping("/download")
  public ResponseEntity<FileSystemResource> downloadMemberFile(
      @RequestParam("fileType") @Parameter(description = "csv = .csv 파일 | xlsx = .xlsx 파일 | xls = .xls 파일")
          String fileType)
      throws JobExecutionException, BaseException {
    File file = memberFileService.downloadMemberFile(fileType);
    FileSystemResource resource = new FileSystemResource(file);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }
}
