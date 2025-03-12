package net.devgrr.interp.ia.api;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableBatchProcessing
public class InterpIaApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(InterpIaApiApplication.class, args);
  }
}
