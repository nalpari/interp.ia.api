package net.devgrr.interp.ia.api.config.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.member.dto.MemberCsvRequest;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CsvReader {
  @Bean
  @StepScope
  public FlatFileItemReader<MemberCsvRequest> reader(
      @Value("#{jobParameters['filePath']}") String filePath) {
    FlatFileItemReader<MemberCsvRequest> reader = new FlatFileItemReader<>();
    reader.setResource(new FileSystemResource(filePath));
    reader.setEncoding("UTF-8");
    reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
    reader.setLinesToSkip(1);

    DefaultLineMapper<MemberCsvRequest> defaultLineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setNames(MemberCsvRequest.getFields().toArray(String[]::new));
    defaultLineMapper.setLineTokenizer(lineTokenizer);

    BeanWrapperFieldSetMapper<MemberCsvRequest> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(MemberCsvRequest.class);
    defaultLineMapper.setFieldSetMapper(fieldSetMapper);

    reader.setStrict(false);
    reader.setLineMapper(defaultLineMapper);
    return reader;
  }
}
