package net.devgrr.interp.ia.api.member.file.importData;

import jakarta.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class FileReader {
  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;

  @Transactional
  public void fileReader(String filePath) throws Exception {
    File file = new File(filePath);
    if (filePath.endsWith(".csv")) {
      csvReader(filePath);
    } else if (filePath.endsWith(".xlsx")) {
      exelReader(filePath, true);
    } else if (filePath.endsWith(".xls")) {
      exelReader(filePath, false);
    } else {
      deleteErrorFile(file, null);
      throw new IllegalArgumentException("Unsupported file type: " + filePath);
    }
  }

  public void exelReader(String filePath, boolean isXlsx) throws BaseException {
    ExelStreamReader<MemberRequest> exelStreamReader = new ExelStreamReader<>();
    exelStreamReader.setFile(new File(filePath));
    exelStreamReader.setXlsx(isXlsx);

    exelStreamReader.setClazz(MemberRequest.class);

    exelStreamReader
        .read()
        .forEach(
            memberRequest -> {
              if (!memberRepository.existsByEmail(memberRequest.email())) {
                memberRepository.save(memberMapper.toMember(memberRequest));
              }
            });
  }

  public void csvReader(String filePath) throws IOException, BaseException {
    File file = new File(filePath);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new java.io.FileReader(file));
      br.mark(1);
      if (br.read() != 0xFEFF) {
        br.reset();
      }

      CSVParser parser = CSVFormat.EXCEL.builder().setHeader().build().parse(br);
      List<String> fieldNames = handlingFieldNames(parser, file, br);

      List<Member> memberList = new ArrayList<>();

      for (CSVRecord record : parser.getRecords()) {
        //        CSV 한 줄씩 읽고 MemberRequest 에 정의된 필드 명과 비교 후 없으면 null 할당
        Object[] values =
            fieldNames.stream()
                .map(fieldName -> record.isMapped(fieldName) ? record.get(fieldName) : null)
                .toArray();

        //        MemberRequest 생성자
        MemberRequest memberRequest =
            MemberRequest.class
                .getDeclaredConstructor(
                    Arrays.stream(MemberRequest.class.getDeclaredFields())
                        .map(Field::getType)
                        .toArray(Class[]::new))
                .newInstance(values);

        try {
          Member member = memberMapper.toMember(memberRequest);
          memberList.add(member);
        } catch (Exception e) {
          throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
      }
      for (Member member : memberList) {
        if (!memberRepository.existsByEmail(member.getEmail())) {
          memberRepository.save(member);
        }
      }
    } catch (NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException
        | InstantiationException e) {
      throw new RuntimeException(e);
    } finally {
      deleteErrorFile(file, br);
    }
  }

  private List<String> handlingFieldNames(CSVParser parser, File file, BufferedReader br)
      throws BaseException, IOException {
    List<String> headers = parser.getHeaderNames();
    //    MemberRequest 에 정의된 모든 필드들
    List<String> fieldNames =
        new ArrayList<>(
            Arrays.stream(MemberRequest.class.getDeclaredFields()).map(Field::getName).toList());
    //    MemberRequest 에 정의된 필드 중 필수 입력값 (Validation 있는 필드) 개수
    List<String> requestFieldNames =
        Arrays.stream(MemberRequest.class.getDeclaredFields())
            .filter(field -> field.getAnnotation(NotBlank.class) != null)
            .map(Field::getName)
            .toList();
    //    "필수 입력 필드 수 < 입력된 헤더 < 전체 필드 수" 가 아닐 시 오류 반환
    if (headers.size() > fieldNames.size() || headers.size() < requestFieldNames.size()) {
      deleteErrorFile(file, br);
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "입력 된 컬럼 수가 맞지 않습니다.");
    }

    List<String> onlyHeader = headers.stream().filter(s -> !requestFieldNames.contains(s)).toList();
    if (!onlyHeader.isEmpty() && onlyHeader.stream().anyMatch(s -> !fieldNames.contains(s))) {
      deleteErrorFile(file, br);
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "입력 된 헤더 명이 맞지 않습니다.");
    }
    return fieldNames;
  }

  private void deleteErrorFile(File file, BufferedReader br) throws IOException {
    if (file.exists()) {
      if (br != null) {
        br.close();
      }
      boolean deleted = file.delete();
    }
  }
}
