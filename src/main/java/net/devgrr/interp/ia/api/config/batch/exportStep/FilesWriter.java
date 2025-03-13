package net.devgrr.interp.ia.api.config.batch.exportStep;

import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class FilesWriter {
  @Bean
  @StepScope
  public ItemStreamWriter<Member> fileWriter(@Value("#{jobParameters['filePath']}") String filePath) {
//    받은 파일 경로로 확장자 구분하여 writer 지정
    if(filePath.endsWith(".csv")) {
      return csvWriter(filePath);
    } else if(filePath.endsWith(".xlsx")) {
      return exelWriter(filePath, true);
    } else if(filePath.endsWith(".xls")){
      return exelWriter(filePath, false);
    } else{
      throw new IllegalArgumentException("Unsupported file type: " + filePath);
    }
  }
//  .xlsx, .xls 파일로 출력
  private ItemStreamWriter<Member> exelWriter(String filePath, boolean isXlsx) {
    ExelWriter<Member> writer = new ExelWriter<>(Member.class);
    writer.setResource(new FileSystemResource(filePath));
    writer.setXlsx(isXlsx);

    return writer;
  }

//  csv 파일로 출력
  public FlatFileItemWriter<Member> csvWriter(String filePath) {
    FlatFileItemWriter<Member> writer = new FlatFileItemWriter<>();
    writer.setResource(new FileSystemResource(filePath));

//    field 지정
    BeanWrapperFieldExtractor<Member> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(Member.getFields().toArray(String[]::new));

//    entity 들 fields 에 맞춰 , 를 구분자로 set
    DelimitedLineAggregator<Member> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setFieldExtractor(fieldExtractor);
    writer.setLineAggregator(lineAggregator);

//    header 지정
    writer.setHeaderCallback(
        w -> {
          w.write(String.join(",", Member.getFields()));
        });
    return writer;
  }
}
