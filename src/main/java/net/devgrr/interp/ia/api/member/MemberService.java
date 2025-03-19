package net.devgrr.interp.ia.api.member;

import java.util.*;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.MemberUpdateRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public Set<Member> getUsersByIds(Set<Integer> ids) throws BaseException {
    List<Member> members = memberRepository.findAllById(ids);
    if (members.size() != ids.size()) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 회원 ID가 있습니다.");
    }
    return new HashSet<>(members);
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
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "회원가입에 실패했습니다.");
    }
    return member;
  }

  @Transactional
  public void putUsers(UserDetails userDetails, MemberUpdateRequest req) throws BaseException {
    Member member =
        memberRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(
                () -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보를 찾을 수 없습니다."));
    try {
      memberMapper.updateMember(req, member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "수정에 실패했습니다. :" + e.getMessage());
    }
  }

  @Transactional
  public void putUsersDeactivateByEmail(String email) throws BaseException {
    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일 입니다."));
    try {
      memberMapper.deactivateMember(member, member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "수정에 실패했습니다. :" + e.getMessage());
    }
  }

  @Transactional
  public void putUsersActiveByEmail(String email) throws BaseException {
    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일 입니다."));

    try {
      memberMapper.activeMember(member, member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "수정에 실패했습니다. :" + e.getMessage());
    }
  }
}
