package net.devgrr.interp.ia.api.member.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.devgrr.interp.ia.api.member.MemberRole;
import net.devgrr.interp.ia.api.model.entity.BaseEntity;

@Getter
@Setter
@Builder
@Entity
@Table(name = "member")
@Schema(description = "회원 엔티티")
@AllArgsConstructor
public class Member extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "고유 ID")
  private Long id;

  @Column(nullable = false, unique = true)
  @Schema(description = "이메일")
  private String email;

  @Column(nullable = false)
  @Schema(description = "비밀번호")
  private String password;

  @Column(nullable = false)
  @Schema(description = "이름")
  private String name;

  @Schema(description = "이미지")
  private String image;

  @Column(nullable = false)
  @Schema(description = "직급")
  private String position;

  @Column(nullable = false)
  @Schema(description = "소속 부서")
  private String department;

  @Column(nullable = false)
  @Schema(description = "직무")
  private String job;

  @Schema(description = "전화번호")
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Schema(description = "권한")
  private MemberRole role;

  @Column(length = 1000)
  @Schema(description = "인증 토큰")
  private String refreshToken;

  @Column(nullable = false, columnDefinition = "boolean default true")
  @Schema(description = "활성 여부 (true: 활성, false: 비활성)")
  private Boolean isActive;

  public Member() {}
}
