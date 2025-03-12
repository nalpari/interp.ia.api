package net.devgrr.interp.ia.api.config.file;

import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class CsvWriter {
  @Bean
  @StepScope
  public FlatFileItemWriter<Member> writer(@Value("#{jobParameters['filePath']}") String filePath) {
    FlatFileItemWriter<Member> writer = new FlatFileItemWriter<>();
    writer.setResource(new FileSystemResource(filePath));

    BeanWrapperFieldExtractor<Member> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(Member.getFields().toArray(String[]::new));

    DelimitedLineAggregator<Member> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setFieldExtractor(fieldExtractor);
    writer.setLineAggregator(lineAggregator);

    writer.setHeaderCallback(
        w -> {
          w.write(String.join(",", Member.getFields()));
        });
    return writer;
  }
}
