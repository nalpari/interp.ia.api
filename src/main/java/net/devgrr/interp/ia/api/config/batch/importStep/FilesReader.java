package net.devgrr.interp.ia.api.config.batch.importStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.member.dto.file.MemberForFileRequest;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
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
public class FilesReader {
  @Bean
  @StepScope
  public ItemStreamReader<MemberForFileRequest> fileReader(
      @Value("#{jobParameters['filePath']}") String filePath) {

    if (filePath.endsWith(".csv")) {
      return csvReader(filePath);
    } else if (filePath.endsWith(".xlsx")) {
      return exelReader(filePath, true);
    } else if (filePath.endsWith(".xls")) {
      return exelReader(filePath, false);
    } else {
      throw new IllegalArgumentException("Unsupported file type: " + filePath);
    }
  }

  private FlatFileItemReader<MemberForFileRequest> csvReader(String filePath) {
    FlatFileItemReader<MemberForFileRequest> reader = new FlatFileItemReader<>();
    reader.setResource(new FileSystemResource(filePath));
    reader.setEncoding("UTF-8");
//    개행 처리
    reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
//    헤더는 따로 읽지 않음
    reader.setLinesToSkip(1);

//    , 를 구분자로 되어있는 데이터들 MemberForFileRequest 의 fields 에 따라 set
    DefaultLineMapper<MemberForFileRequest> defaultLineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setNames(MemberForFileRequest.getFields().toArray(String[]::new));
    defaultLineMapper.setLineTokenizer(lineTokenizer);

//    데이터 MemberForFileRequest 객체에 파싱
    BeanWrapperFieldSetMapper<MemberForFileRequest> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(MemberForFileRequest.class);
    defaultLineMapper.setFieldSetMapper(fieldSetMapper);

//    오류 있어도 무시
    reader.setStrict(false);
    reader.setLineMapper(defaultLineMapper);
    return reader;
  }

  private ItemStreamReader<MemberForFileRequest> exelReader(String filePath, boolean isXlsx) {
    ExelReader<MemberForFileRequest> reader = new ExelReader<>(MemberForFileRequest.class);
    reader.setResource(new FileSystemResource(filePath));
    reader.setXlsx(isXlsx);

    return reader;
  }
}
