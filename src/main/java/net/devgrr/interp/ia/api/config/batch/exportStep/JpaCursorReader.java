package net.devgrr.interp.ia.api.config.batch.exportStep;

import jakarta.persistence.EntityManagerFactory;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaCursorReader {
    @Bean
    public JpaCursorItemReader<Member> jpaCursorItemReader(EntityManagerFactory emf) {
        JpaCursorItemReader<Member> reader = new JpaCursorItemReader<>();
//        Jpa DB 접근
        reader.setEntityManagerFactory(emf);
//        Member 테이블 전체 읽음
        reader.setQueryString("select m from Member m");

        return reader;
    }
}
