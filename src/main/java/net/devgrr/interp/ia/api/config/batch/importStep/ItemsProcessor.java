package net.devgrr.interp.ia.api.config.batch.importStep;

import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.dto.MemberForFileRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ItemsProcessor {
  private final MemberMapper memberMapper;

  @Bean
  @StepScope
  public ItemProcessor<MemberForFileRequest, Member> itemProcessor() {
    //    MemberForFileRequest -> Member 변환
    return memberMapper::toMember;
  }

  @Bean
  @StepScope
  public ItemProcessor<?, ?> jpqlProcessor(@Value("#{jobParameters['columns']}") String columns) {
//    columns 옵션이 없다면 Member -> Object[] 로 타입 변경 필요
//    (jpaReader 에서 전체 데이터 조회 시 Member 타입으로 읽음)
    if (columns.isBlank()) {
      return new ItemProcessor<Member, Object[]>() {
        @Override
        public Object[] process(Member item) throws NoSuchFieldException, IllegalAccessException {
          Field[] fields = Member.class.getDeclaredFields();
          String[] fieldNames = new String[fields.length];
          for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
          }

          List<Object> values = new ArrayList<>();
          for (String fieldName : fieldNames) {
            Field field = Member.class.getDeclaredField(fieldName.trim());
            field.setAccessible(true);
            Object value = field.get(item);
            values.add(value);
          }
          return values.toArray();
        }
      };
//      옵션 없다면 받은 데이터 반환
    } else {
      return new ItemProcessor<Object[], Object[]>() {
        @Override
        public Object[] process(Object[] item) {
          return item;
        }
      };
    }
  }
}
