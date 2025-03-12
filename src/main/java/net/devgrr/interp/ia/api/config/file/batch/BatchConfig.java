package net.devgrr.interp.ia.api.config.file.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.dto.MemberCsvRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final MemberRepository memberRepository;

  @Bean
  public Job importMemberJob(Step importMemberStep) {
    return new JobBuilder("importMemberJob", jobRepository).start(importMemberStep).build();
  }

  @Bean
  public Job exportMemberJob(Step exportMemberStep) {
    return new JobBuilder("exportMemberJob", jobRepository).start(exportMemberStep).build();
  }

  @Bean
  public Step importMemberStep(
      FlatFileItemReader<MemberCsvRequest> reader, MemberMapper memberMapper) {
    return new StepBuilder("importMemberStep", jobRepository)
        .<MemberCsvRequest, MemberCsvRequest>chunk(10, transactionManager)
        .reader(reader)
        .writer(
            chunk -> {
              for (MemberCsvRequest memberRequest : chunk) {
                memberRepository.save(memberMapper.toMember(memberRequest));
              }
            })
        .allowStartIfComplete(true)
        .build();
  }

  @Bean
  public Step exportMemberStep(
      FlatFileItemWriter<Member> writer, JpaCursorItemReader<Member> reader) {
    return new StepBuilder("exportMemberStep", jobRepository)
        .<Member, Member>chunk(10, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
  }
}
