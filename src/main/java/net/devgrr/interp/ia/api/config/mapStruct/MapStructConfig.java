package net.devgrr.interp.ia.api.config.mapStruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {

  @Bean
  public MemberMapper memberMapper() {
    return new MemberMapperImpl();
  }

  @Bean
  public ProjectMapper projectMapper() {
    return new ProjectMapperImpl();
  }

  @Bean
  public IssueMapper issueMapper() {
    return new IssueMapperImpl();
  }

  @Bean
  public HistoryMapper historyMapper() {
    return new HistoryMapperImpl();
  }

  @Bean
  public CommentMapper commentMapper() {
    return new CommentMapperImpl();
  }
}
