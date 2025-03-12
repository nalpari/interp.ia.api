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
  private ItemStreamWriter<Member> exelWriter(String filePath, boolean isXlsx) {
    ExelWriter<Member> writer = new ExelWriter<>(Member.class);
    writer.setResource(new FileSystemResource(filePath));
    writer.setXlsx(isXlsx);

    return writer;
  }

  public FlatFileItemWriter<Member> csvWriter(String filePath) {
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
