package net.devgrr.interp.ia.api.member.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.devgrr.interp.ia.api.member.dto.MemberValidationGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "파일 단위의 회원 요청")
public class MemberForFileRequest {
  @Schema(description = "이메일")
  @Email(message = "유효하지 않은 이메일 형식입니다.", groups = MemberValidationGroup.class)
  @NotBlank(message = "필수값 : email", groups = MemberValidationGroup.class)
  private String email;

  @Schema(description = "이름")
  @NotBlank(message = "필수값 : 이름", groups = MemberValidationGroup.class)
  private String name;

  @Schema(description = "비밀번호")
  @NotBlank(message = "필수값 : 비밀번호", groups = MemberValidationGroup.class)
  private String password;

  @Schema(description = "부서")
  @NotBlank(message = "필수값 : 부서", groups = MemberValidationGroup.class)
  private String department;

  @Schema(description = "직무")
  @NotBlank(message = "필수값 : 직무", groups = MemberValidationGroup.class)
  private String job;

  @Schema(description = "직급")
  @NotBlank(message = "필수값 : 직급", groups = MemberValidationGroup.class)
  private String position;

  @Schema(description = "전화번호")
  private String phone;

  @Schema(description = "회원 이미지")
  private String image;

  @Schema(description = "권한")
  private String role;

  public static List<String> getFields() {
    Field[] fields = MemberForFileRequest.class.getDeclaredFields();
    List<String> list = new ArrayList<>();
    for(Field f : fields) {
      list.add(f.getName());
    }
    return list;
  }
}
