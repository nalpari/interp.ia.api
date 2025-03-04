package net.devgrr.interp.ia.api.config.mapStruct;

import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.member.MemberRole;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;
import net.devgrr.interp.ia.api.member.dto.MemberUpdateRequest;
import net.devgrr.interp.ia.api.member.dto.ResultResponse;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.mapstruct.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

  @Mapping(target = "updatedDate", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "refreshToken", source = "refreshToken")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Member updateMemberRefreshToken(Member updateMember, @MappingTarget Member member);

  @Mapping(
      source = "password",
      target = "password",
      qualifiedByName = "pwEncoder",
      conditionExpression = "java(req.password() != null && !req.password().isEmpty())")
  @Mapping(target = "updatedDate", expression = "java(java.time.LocalDateTime.now())")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateMember(MemberUpdateRequest req, @MappingTarget Member member) throws BaseException;
}
