package net.devgrr.interp.ia.api.config.batch.exportStep;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class FilesWriter {
  Class<?> clazz;

  @Bean
  @StepScope
  public ItemStreamWriter<Object[]> fileWriter(
      @Value("#{jobParameters['filePath']}") String filePath,
      @Value("#{jobParameters['dataFormat']}") String dataFormat,
      @Value("#{jobParameters['header']}") String header,
      @Value("#{jobParameters['columns']}") String columns,
      @Value("#{jobParameters['classType']}") String classType) {
    setClazz(classType);

    //    받은 파일 경로로 확장자 구분하여 writer 지정
    if (filePath.endsWith(".csv")) {
      return csvWriter(filePath, header, columns);
    } else if (filePath.endsWith(".xlsx")) {
      return exelWriter(filePath, true, dataFormat, header, columns);
    } else if (filePath.endsWith(".xls")) {
      return exelWriter(filePath, false, dataFormat, header, columns);
    } else {
      throw new IllegalArgumentException("Unsupported file type: " + filePath);
    }
  }

  //  reflection 사용을 위한 clazz 지정
  private void setClazz(String classType) {
    switch (classType) {
      case "Member":
        clazz = Member.class;
    }
  }

  //  .xlsx, .xls 파일로 출력
  private ItemStreamWriter<Object[]> exelWriter(
      String filePath, boolean isXlsx, String dataFormat, String header, String columns) {
    ExelWriter<Object[]> writer = new ExelWriter<>();

    writer.setResource(new FileSystemResource(filePath));
    writer.setXlsx(isXlsx);
    writer.setHeader("true".equals(header));

    //    columns 옵션이 없다면 전체 데이터 조회
    if (columns.isBlank()) {
      writer.setClazz(clazz);
    } else {
      writer.setFieldsName(columns.split(","));
    }
    if (!dataFormat.isBlank()) {
      writer.setDataFormat(dataFormat);
    }
    return writer;
  }

  //  csv 파일로 출력
  public FlatFileItemWriter<Object[]> csvWriter(String filePath, String header, String columns) {
    FlatFileItemWriter<Object[]> writer = new FlatFileItemWriter<>();
    writer.setResource(new FileSystemResource(filePath));

    PassThroughFieldExtractor<Object[]> fieldExtractor = new PassThroughFieldExtractor<>();

    //    entity 들 fields 에 맞춰 , 를 구분자로 set
    DelimitedLineAggregator<Object[]> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setFieldExtractor(fieldExtractor);
    writer.setLineAggregator(lineAggregator);

    //    header 지정
    if ("true".equals(header)) {
      if (columns.isBlank()) {
        writer.setHeaderCallback(
            w -> {
              String memberFields =
                  Arrays.stream(clazz.getDeclaredFields())
                      .map(Field::getName)
                      .collect(Collectors.joining(","));
              String baseFields =
                  Arrays.stream(clazz.getSuperclass().getDeclaredFields())
                      .map(Field::getName)
                      .collect(Collectors.joining(","));
              w.write(memberFields + "," + baseFields);
            });
      } else {
        writer.setHeaderCallback(
            w -> {
              w.write(columns);
            });
      }
    }
    return writer;
  }
}
