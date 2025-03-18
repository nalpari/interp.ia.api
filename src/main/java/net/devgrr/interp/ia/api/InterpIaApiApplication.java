package net.devgrr.interp.ia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InterpIaApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(InterpIaApiApplication.class, args);
  }
}
