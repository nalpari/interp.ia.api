package net.devgrr.interp.ia.api.config.batch.importStep;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JpaWriter {
  @Bean
  @StepScope
  public JpaItemWriter<Member> jpaItemWriter(EntityManagerFactory em) {
    JpaItemWriter<Member> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(em);
    return writer;
  }
}
