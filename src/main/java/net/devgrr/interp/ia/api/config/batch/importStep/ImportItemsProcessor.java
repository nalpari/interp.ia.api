package net.devgrr.interp.ia.api.config.batch.importStep;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.file.MemberForFileRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ImportItemsProcessor {
  private final MemberMapper memberMapper;
  private final EntityManager entityManager;
  @Bean
  @StepScope
  public ItemProcessor<MemberForFileRequest, Member> itemProcessor(
      @Value("#{jobParameters['dataSkip']}") String dataSkip) {
    //    MemberForFileRequest -> Member 변환 후
    //    파라미터 값에 따라 같은 이메일이 존재 할 때 건너뛰고 저장할 지, 값을 업데이트 할 지 지정
    return request -> {
      Member member = memberMapper.toMember(request);

      // 이메일이 같은 기존 멤버 조회
      Member existingMember = entityManager.createQuery(
                      "SELECT m FROM Member m WHERE m.email = :email", Member.class)
              .setParameter("email", member.getEmail())
              .getResultList()
              .stream()
              .findFirst()
              .orElse(null);

      if (existingMember != null) {
        if ("skip".equalsIgnoreCase(dataSkip)) {
          // 중복된 이메일이면 건너뜀
          return null;
        } else if ("update".equalsIgnoreCase(dataSkip)) {
          // 기존 데이터 업데이트
          memberMapper.updateMember(request, existingMember);
          return existingMember;
        }
      }
      // 중복되지 않은 경우 새로 저장
      return member;
    };
  }
}
