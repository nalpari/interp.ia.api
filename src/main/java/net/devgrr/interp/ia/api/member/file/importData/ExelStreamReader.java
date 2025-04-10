package net.devgrr.interp.ia.api.member.file.importData;

import jakarta.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.SneakyThrows;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExelStreamReader<T> {
  @Setter private File file;
  @Setter private boolean isXlsx;

  private Workbook workbook;
  private Sheet sheet;

  private int currentRow;
  private int currentSheet;

  private List<String> headers;
  //  전체 필드
  private List<String> allFieldNames;

  @Setter private Class<T> clazz;

  @SneakyThrows
  private void open() {
    FileInputStream fis = new FileInputStream(file);
    if (isXlsx) {
      this.workbook = new XSSFWorkbook(fis);
    } else {
      this.workbook = new HSSFWorkbook(fis);
    }
    this.sheet = workbook.getSheetAt(0);
    this.currentSheet = 0;
    this.currentRow = 1;
  }

  public Stream<T> read() throws BaseException {
    if (this.workbook == null) {
      this.open();
      readHeader();
    }
    if (currentSheet >= workbook.getNumberOfSheets()) {
      return null;
    }

    if (currentRow > sheet.getLastRowNum()) {
      currentSheet++;
      if (currentSheet >= workbook.getNumberOfSheets()) {
        return null;
      }
      this.sheet = workbook.getSheetAt(currentSheet);
      currentRow = 1;
      return read();
    }

    Row row = sheet.getRow(currentRow);
    if (row == null) {
      currentRow++;
      return read();
    }
    Iterator<Row> rowIterator = sheet.iterator();
    if (rowIterator.hasNext()) {
      rowIterator.next();
    }

    return Stream.generate(() -> rowIterator.hasNext() ? rowIterator.next() : null)
        .takeWhile(Objects::nonNull)
        .map(
            rowData -> {
              try {
                return mappingData(rowData);
              } catch (Exception e) {
                e.fillInStackTrace();
                return null;
              }
            });
  }

  private T mappingData(Row row) throws Exception {
    Object[] values = new Object[allFieldNames.size()];
    //    header 기준 입력된 데이터
    Map<String, Object> valueMap = new HashMap<>();

    for (int i = 0; i < headers.size(); i++) {
      Cell cell = row.getCell(i);

      Object value = getCellValue(cell);
      valueMap.put(headers.get(i), value);
    }

    for (int i = 0; i < allFieldNames.size(); i++) {
      Object value = valueMap.get(allFieldNames.get(i));
      values[i] = value;
      // null 이여도 할당
    }

    Constructor<T> constructor =
        clazz.getDeclaredConstructor(
            Arrays.stream(clazz.getDeclaredFields()).map(Field::getType).toArray(Class[]::new));
    currentRow++;
    try {
      return constructor.newInstance(values);
    } catch (IllegalArgumentException e) {
      deleteErrorFile(file);
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "MemberRequest 필드 타입 불일치");
    }
  }

  private Object getCellValue(Cell cell) {
    if (cell == null) {
      return "";
    }
    switch (cell.getCellType()) {
      case NUMERIC -> {
        return cell.getNumericCellValue();
      }
      case STRING -> {
        return cell.getStringCellValue();
      }
      case BOOLEAN -> {
        return cell.getBooleanCellValue();
      }
      default -> {
        return "";
      }
    }
  }

  private void readHeader() throws BaseException {
    headers = new ArrayList<>();
    Row row = sheet.getRow(0);
    for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
      Cell cell = row.getCell(i);
      headers.add(cell.getStringCellValue());
    }
    allFieldNames = Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).toList();

    //  notBlank annotation 있는 필드
    List<String> fieldNames =
        Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.getAnnotation(NotBlank.class) != null)
            .map(Field::getName)
            .toList();

    if (headers.size() > allFieldNames.size() || headers.size() < fieldNames.size()) {
      deleteErrorFile(file);
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "입력 된 컬럼 수가 맞지 않습니다.");
    }

    List<String> onlyHeader = headers.stream().filter(s -> !fieldNames.contains(s)).toList();
    if (onlyHeader.stream().anyMatch(s -> !allFieldNames.contains(s))) {
      deleteErrorFile(file);
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "입력 된 헤더 명이 맞지 않습니다.");
    }
  }

  private void deleteErrorFile(File file) {
    if(file.exists()) {
      boolean deleted = file.delete();
    }
  }
}
