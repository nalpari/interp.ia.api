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

//  Optional<Member> findByUserId(String userId);

  Optional<Member> findByEmail(String email);

//  boolean existsByUserId(String userId);

  boolean existsByEmail(String email);

  Optional<Member> findByRefreshToken(String refreshToken);

  Optional<Member> findById(Long pkId);

  @Modifying
  @Query("UPDATE Member m SET m.isActive = false WHERE m.email = :userId")
  void deactivateByUserId(@Param("email") String email);
}
