package net.devgrr.interp.ia.api.config.batch.importStep;

import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberForFileRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ItemsProcessor {
  private final MemberMapper memberMapper;

  @Bean
  @StepScope
  public ItemProcessor<MemberForFileRequest, Member> itemProcessor() {
//    MemberForFileRequest -> Member 변환
      return memberMapper::toMember;
  }
}
