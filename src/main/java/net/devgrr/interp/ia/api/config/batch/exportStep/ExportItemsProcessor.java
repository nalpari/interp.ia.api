package net.devgrr.interp.ia.api.config.batch.exportStep;

import lombok.RequiredArgsConstructor;
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
public class ExportItemsProcessor {
  private Class<?> clazz;

  @Bean
  @StepScope
  public ItemProcessor<?, ?> jpqlProcessor(
      @Value("#{jobParameters['columns']}") String columns,
      @Value("#{jobParameters['classType']}") String classType) {
    //      reflection 사용 위해 clazz 지정
    setClazz(classType);

    //    columns 옵션이 없다면 Member -> Object[] 로 타입 변경 필요
    //    (jpaReader 에서 전체 데이터 조회 시 Member 타입으로 읽음)
    if (columns.isBlank()) {
      return (ItemProcessor<Member, Object[]>)
          item -> {
            //  지정한 class 의 reflection 사용하여 필드명 가져옴
            //  Member class 에서 필드명을 찾지 못할 경우 부모 클래스의 필드명을 가져옴
            List<Field> fields = new ArrayList<>(List.of(clazz.getDeclaredFields()));
            fields.addAll(List.of(clazz.getSuperclass().getDeclaredFields()));
            String[] fieldNames = new String[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
              fieldNames[i] = fields.get(i).getName();
            }

            List<Object> values = new ArrayList<>();
            for (String fieldName : fieldNames) {
              Object value = getObject(item, fieldName);
              values.add(value);
            }
//            가져온 값을 배열로 변환하여 반환 -> item 을 Member 가 아닌 Object[] 로 보내기 위함
            return values.toArray();
          };
      //      옵션 없다면 받은 데이터 반환
    } else {
      return (ItemProcessor<Object[], Object[]>) item -> item;
    }
  }

  private void setClazz(String classType) {
    switch (classType) {
      case "Member":
        clazz = Member.class;
    }
  }

//  Member, BaseEntity 클래스의 필드 이름에 따라 타입 지정해서 값을 가져옴
//  ex : email 은 String 타입, "aa@aa.com" 을 String 타입으로 지정해서 값 가져옴
  private Object getObject(Member item, String fieldName) throws IllegalAccessException {
    Field field = null;
    while (clazz != null) {
      try {
        field = clazz.getDeclaredField(fieldName.trim());
        field.setAccessible(true);
        break;
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    return field.get(item);
  }
}
