package net.devgrr.interp.ia.api.member;

import java.util.List;
import java.util.Optional;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
  List<Member> findAllByIsActiveTrue();

  List<Member> findAllByIsActiveFalse();

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Member> findByRefreshToken(String refreshToken);
}
