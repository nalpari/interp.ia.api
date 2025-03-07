package net.devgrr.interp.ia.api.member;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "사용자 API")
@RestController
public class MemberController {

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
  public ResponseEntity<Object> putUsers(
      @Valid @RequestBody MemberUpdateRequest req,
      @AuthenticationPrincipal UserDetails userDetails)
      throws BaseException {
    memberService.putUsers(userDetails, req);
    return ResponseEntity.ok().build();
  }

  @Operation(description = "사용자의 계정을 비활성화합니다.")
  @PatchMapping("/{email}/deactivate")
  public ResultResponse putUsersDeactivateByEmail(@PathVariable("email") String email)
      throws BaseException {
    return memberService.putUsersDeactivateByEmail(email);
  }

  @Operation(description = "사용자의 계정을 활성화합니다.")
  @PatchMapping("/{email}/activate")
  public ResultResponse putUsersActiveByEmail(@PathVariable("email") String email)
      throws BaseException {
    return memberService.putUsersActiveByEmail(email);
  }
}
