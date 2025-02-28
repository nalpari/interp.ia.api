package net.devgrr.interp.ia.api.config.mapStruct;

import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.member.MemberRole;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;
import net.devgrr.interp.ia.api.member.dto.ResultResponse;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static java.time.LocalTime.now;

// import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MemberMapper {
  //  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Named("pwEncoder")
  static String pwEncoder(String password) {
    BCryptPasswordEncoder pe = new BCryptPasswordEncoder(); // interface <-> bean ..
    return pe.encode(password);
  }

  @Named("toMemberRole")
  static MemberRole toMemberRole(String role) {
    if (role == null) {
      return MemberRole.USER;
    }
    String r = role.toUpperCase();
    if (r.equals("ADMIN")) {
      return MemberRole.ADMIN;
    } else {
      return MemberRole.USER;
    }
  }

  @Mapping(source = "password", target = "password", qualifiedByName = "pwEncoder")
  @Mapping(source = "role", target = "role", qualifiedByName = "toMemberRole")
  @Mapping(target = "isActive", expression = "java(true)")
  Member toMember(MemberRequest memberRequest);

  MemberResponse toResponse(Member member);

  ResultResponse toResultResponse(boolean result, String message);

  @Mapping(target = "email", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "image", ignore = true)
  @Mapping(target = "position", ignore = true)
  @Mapping(target = "department", ignore = true)
  @Mapping(target = "job", ignore = true)
  @Mapping(target = "phone", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "isActive", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "refreshToken", source = "refreshToken")
  Member updateMemberRefreshToken(Member updateMember, @MappingTarget Member member);

  @Mapping(source = "password", target = "password", qualifiedByName = "pwEncoder")
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "refreshToken", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
  void updateMember(MemberRequest req, @MappingTarget Member member) throws BaseException;
}
