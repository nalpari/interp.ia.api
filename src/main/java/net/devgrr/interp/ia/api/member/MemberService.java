package net.devgrr.interp.ia.api.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberUpdateRequest;
import net.devgrr.interp.ia.api.member.dto.ResultResponse;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;

  public List<Member> getUsers(String isActive) throws BaseException {
    if (isActive == null) {
      return memberRepository.findAll();
    } else if (isActive.equals("true")) {
      return memberRepository.findAllByIsActiveTrue();
    } else if (isActive.equals("false")) {
      return memberRepository.findAllByIsActiveFalse();
    } else {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }

  public Member getUsersByEmail(String email) throws BaseException {
    Member member = memberRepository.findByEmail(email).orElse(null);
    if (member == null) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 Email 입니다.");
    }
    return member;
  }

  @Transactional
  public Member setUsers(MemberRequest req) throws BaseException {
    if (memberRepository.existsByEmail(req.email())) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 Email 입니다.");
    }

    Member member = memberMapper.toMember(req);
    try {
      memberRepository.save(member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }
    return member;
  }

  @Transactional
  public Member putUsers(UserDetails userDetails, MemberUpdateRequest req)
      throws BaseException {
    Member member =
        memberRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

    if (!member.getId().equals(req.id())) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보가 일치하지 않습니다.");
    }
    if (StringUtils.hasText(req.email())) {
      if (memberRepository.existsByEmail(req.email())) {
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 email 입니다.");
      }
    }
    memberMapper.updateMember(req, member);
    try {
      memberRepository.save(member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }
    return member;
  }

  @Transactional
  public ResultResponse putUsersDeactivateByEmail(String email) throws BaseException {
    boolean result = false;

    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일 입니다."));

    if (!member.getIsActive()) {
      return memberMapper.toResultResponse(result, "이미 비활성화 된 회원입니다.");
    }
    try {
      if (memberRepository.deactivateByEmail(member.getEmail()) == 1) result = true;
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }
    return memberMapper.toResultResponse(result, "회원 비활성화");
  }

  @Transactional
  public ResultResponse putUsersActiveByEmail(String email) throws BaseException {
    boolean result = false;

    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일 입니다."));

    if (member.getIsActive()) {
      return memberMapper.toResultResponse(result, "활성화 되어있는 회원입니다.");
    }
    try {
      if (memberRepository.activeByEmail(member.getEmail()) == 1) result = true;
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }
    return memberMapper.toResultResponse(result, "회원 활성화");
  }
}
