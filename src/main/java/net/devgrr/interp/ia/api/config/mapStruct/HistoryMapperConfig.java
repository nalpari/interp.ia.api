package net.devgrr.interp.ia.api.config.mapStruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoryMapperConfig {

  @Bean
  public HistoryMapper historyMapper() {
    return new HistoryMapperImpl();
  }
}
