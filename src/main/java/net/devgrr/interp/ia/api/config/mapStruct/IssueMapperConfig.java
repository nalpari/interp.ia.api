package net.devgrr.interp.ia.api.config.mapStruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IssueMapperConfig {

  @Bean
  public IssueMapper issueMapper() {
    return new IssueMapperImpl();
  }
}
