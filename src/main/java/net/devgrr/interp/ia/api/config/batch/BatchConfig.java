package net.devgrr.interp.ia.api.config.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.member.dto.file.MemberForFileRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  //  파일 DB에 import
  //  파일을 읽고 DB 에 write 하는 하나의 step 으로 구성
  @Bean
  public Job importMemberJob(Step importMemberStep) {
    return new JobBuilder("importMemberJob", jobRepository).start(importMemberStep).build();
  }

  //  DB 파일로 export
  @Bean
  public Job exportMemberJob(Step exportMemberStep) {
    return new JobBuilder("exportMemberJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(exportMemberStep)
        .build();
  }

  /**
   * @param fileReader - 확장자에 따라 파일 읽고 MemberForFileRequest 타입으로 매핑
   * @param processor - Member Entity 저장하기 위해 MemberForFileRequest -> Member 변환
   * @param jpaWriter - Member 타입의 Item 을 DB 에 작성
   * @return importMemberJob 의 배치처리를 위한 step
   */
  @Bean
  public Step importMemberStep(
      ItemStreamReader<MemberForFileRequest> fileReader,
      ItemProcessor<MemberForFileRequest, Member> processor,
      JpaItemWriter<Member> jpaWriter) {
    return new StepBuilder("importMemberStep", jobRepository)
        .<MemberForFileRequest, Member>chunk(10, transactionManager)
        .reader(fileReader)
        .processor(processor)
        .writer(jpaWriter)
        .allowStartIfComplete(true)
        .build();
  }

  /**
   * @param writer - 파일 확장자에 따라 Item 을 처리하여 작성
   * @param reader - Member 전체 데이터를 조회 후 Member 타입의 Item 으로 반환
   * @return exportMemberJob 의 배치처리를 위한 step
   */
  @Bean
  public Step exportMemberStep(
      ItemStreamWriter<Object[]> writer,
      ItemProcessor<Member, Object[]> jpqlProcessor,
      JpaCursorItemReader<Member> reader) {
    return new StepBuilder("exportMemberStep", jobRepository)
        .<Member, Object[]>chunk(10, transactionManager)
        .reader(reader)
        .processor(jpqlProcessor)
        .writer(writer)
        .build();
  }
}
