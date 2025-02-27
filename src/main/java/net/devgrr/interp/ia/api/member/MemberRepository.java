package net.devgrr.interp.ia.api.member;

import java.util.List;
import java.util.Optional;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Integer> {
  List<Member> findAllByIsActiveTrue();

  List<Member> findAllByIsActiveFalse();

  Optional<Member> findByUserId(String userId);

  boolean existsByUserId(String userId);

  boolean existsByEmail(String email);

  Optional<Member> findByRefreshToken(String refreshToken);

  @Modifying
  @Query("UPDATE Member m SET m.isActive = false WHERE m.userId = :userId")
  void deactivateByUserId(@Param("userId") String userId);
}
