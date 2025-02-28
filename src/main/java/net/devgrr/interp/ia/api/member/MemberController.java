package net.devgrr.interp.ia.api.member;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;
import net.devgrr.interp.ia.api.member.dto.MemberValidationGroup;
import net.devgrr.interp.ia.api.member.dto.ResultResponse;
import org.springframework.http.HttpStatus;
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

    // TODO: 기존 ID 회원 조회 api, 로직 주석 삭제 처리 예정
    //
    //  @Operation(description = "사용자를 조회한다.")
    //  @GetMapping("/{userId}")
    //  public MemberResponse getUsersById(@PathVariable("userId") String userId) throws BaseException {
    //    return memberMapper.toResponse(memberService.getUsersById(userId));
    //  }

    // 기존 ID/Password login/signup 방식에서 Email/password 로 변경하며 회원조회 api 파라미터도 email 로 변경
    @Operation(description = "사용자를 조회한다.")
    @GetMapping("/{email}")
    public MemberResponse getUserByEmail(@PathVariable("email") String email) throws BaseException {
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

    /*
     * TODO: 사용자 정보 수정 API 추가
     * */
    @Operation(description = "사용자의 정보를 수정한다.")
    @PutMapping("/{pkId}")
    public MemberResponse putUserById(@PathVariable("pkId") Long pkId,
                                      @Validated(MemberValidationGroup.createGroup.class) @RequestBody MemberRequest req)
            throws BaseException {
        return memberMapper.toResponse(memberService.putUsersById(pkId, req));
    }

    /*
     * TODO: 사용자 비활성(삭제) API 추가
     * */
    @Operation(description = "사용자의 계정을 비활성화합니다.")
    @PatchMapping("/{email}")
    public ResultResponse delUsersByEmail(@PathVariable("email") String email)
        throws BaseException {
        return memberService.delUsersByEmail(email);
    }
    /*
     * TODO: 사용자 활성(복구) API 추가
     * */
}
