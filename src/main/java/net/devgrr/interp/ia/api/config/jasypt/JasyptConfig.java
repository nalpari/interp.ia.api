package net.devgrr.interp.ia.api.config.jasypt;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JasyptConfig {

  @Value("${jasypt.encryptor.password}")
  private String PASSWORD;

  @Primary
  @Bean("jasyptStringEncryptor")
  public StringEncryptor stringEncryptor() {
    PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    SimpleStringPBEConfig config = new SimpleStringPBEConfig();

    config.setPassword(PASSWORD); // 암호화/복호화에 사용할 키
    config.setPoolSize("1"); // 암호화/복호화에 사용할 스레드 풀 사이즈
    config.setAlgorithm("PBEWithMD5AndDES"); // 사용할 알고리즘
    config.setStringOutputType("base64"); // 인코딩 타입
    config.setKeyObtentionIterations("1000"); // 키 생성 횟수
    config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator"); // salt 생성 클래스

    encryptor.setConfig(config); // 설정 적용

    return encryptor;
  }

  public String jasyptEncrypt(String input) {
    return stringEncryptor().encrypt(input);
  }

  public String jasyptDecryt(String input) {
    return stringEncryptor().decrypt(input);
  }
}
