package net.devgrr.interp.ia.api.member.file.importData;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.ia.api.member.MemberRepository;
import net.devgrr.interp.ia.api.member.dto.MemberRequest;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.apache.commons.csv.CSVFormat;
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
    if (filePath.endsWith(".csv")) {
      csvReader(filePath);
    } else if (filePath.endsWith(".xlsx")) {
      exelReader(filePath, true);
    } else if (filePath.endsWith(".xls")) {
      exelReader(filePath, false);
    } else {
      throw new IllegalArgumentException("Unsupported file type: " + filePath);
    }
  }

  public void exelReader(String filePath, boolean isXlsx) throws BaseException {
    ExelStreamReader<MemberRequest> exelStreamReader = new ExelStreamReader<>();
    exelStreamReader.setFile(new File(filePath));
    exelStreamReader.setXlsx(isXlsx);
    exelStreamReader.setFieldNames(
        Arrays.stream(MemberRequest.class.getDeclaredFields())
            .map(Field::getName)
            .toArray(String[]::new));
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
    try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
      br.mark(1);
      if (br.read() != 0xFEFF) {
        br.reset();
      }

      Iterable<CSVRecord> records = CSVFormat.EXCEL.builder().setHeader().build().parse(br);
      List<String> fieldNames =
          new ArrayList<>(
              Arrays.stream(MemberRequest.class.getDeclaredFields()).map(Field::getName).toList());

      List<Member> memberList = new ArrayList<>();

      for (CSVRecord record : records) {
        String[] values =
            fieldNames.stream()
                .map(field -> Optional.ofNullable(record.get(field)).orElse(""))
                .toArray(String[]::new);

        try {
          MemberRequest memberRequest =
              new MemberRequest(
                  values[0], values[1], values[2], values[3], values[4], values[5], values[6],
                  values[7], values[8]);
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
    }
  }
}
