package net.devgrr.interp.ia.api.config.mapStruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectMapperConfig {

  @Bean
  public ProjectMapper projectMapper() {
    return new ProjectMapperImpl();
  }
}
