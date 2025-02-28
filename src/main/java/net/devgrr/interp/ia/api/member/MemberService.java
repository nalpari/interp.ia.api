package net.devgrr.interp.ia.api.member;

import java.util.List;

import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.dto.ResultResponse;
import net.devgrr.interp.ia.api.member.entity.Member;
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

    // TODO: 기존 ID 회원 조회 api, 로직 주석 삭제 처리 예정
    //
    //  public Member getUsersById(String userId) throws BaseException {
    //    Member member = memberRepository.findByUserId(userId).orElse(null);
    //    if (member == null) {
    //      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 ID 입니다.");
    //    }
    //    return member;
    //  }

    // 기존 ID/Password login/signup 방식에서 Email/password 로 변경하며 회원조회 api 파라미터도 email 로 변경
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
    public Member putUsersById(Long pkId, MemberRequest req) throws BaseException {
        Member member = memberRepository.findById(pkId).orElseThrow(
                () -> new BaseException(
                        ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
//        Member updateMember = memberMapper.toMember(req);
        memberMapper.updateMember(req, member);
        try {
            memberRepository.save(member);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        }
        return member;
    }

    @Transactional
    public ResultResponse delUsersByEmail(String email) throws BaseException {
        boolean result = false;

        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일 입니다."));

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

        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일 입니다."));

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
