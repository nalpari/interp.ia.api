package net.devgrr.interp.ia.api.config.batch.exportStep;

import jakarta.persistence.EntityManagerFactory;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class JpaReader {
  @Bean
  @StepScope
  public JpaCursorItemReader<Member> jpaCursorItemReader(
      EntityManagerFactory emf, @Value("#{jobParameters['columns']}") String columns) {
    JpaCursorItemReader<Member> reader = new JpaCursorItemReader<>();
    //        Jpa DB 접근
    reader.setEntityManagerFactory(emf);

    //   columns 옵션이 있으면 지정한 컬럼만 조회
    ArrayList<String> sqlList = new ArrayList<>();
    if (!columns.isBlank()) {
      String[] columnsArray = columns.split(",");
      for (String s : columnsArray) {
        sqlList.add("m." + s);
      }
    } else {
      sqlList.add("m"); // 전체 컬럼 조회
    }

    reader.setQueryString("SELECT " + String.join(", ", sqlList) + " FROM Member m");
    return reader;
  }
}
