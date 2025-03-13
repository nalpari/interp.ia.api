package net.devgrr.interp.ia.api.config.batch.importStep;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.*;
import org.springframework.core.io.Resource;

@AllArgsConstructor
@NoArgsConstructor
public class ExelReader<T> implements ItemStreamReader<T> {
  /// 이후 다른 엔티티 데이터 import 위해 제너릭타입으로 지정

  @Setter private Resource resource;
  @Setter private boolean isXlsx;

  private Workbook workbook;
  private Sheet sheet;

//  현재 읽고 있는 행
  private int currentRow = 0;
//  현재 읽고 있는 시트
  private int currentSheet = 0;

  private String[] fieldNames;

//  reflection
  private Class<T> type;
  public ExelReader(Class<T> type) {
    this.type = type;
  }

//  전달받은 엑셀 확장자에 따라 workbook 지정
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    try {
      if (resource.isFile()) {
        FileInputStream fileInputStream = new FileInputStream(resource.getFile());
        if (isXlsx) {
          this.workbook = new XSSFWorkbook(fileInputStream);
        } else {
          this.workbook = new HSSFWorkbook(fileInputStream);
        }
        this.sheet = this.workbook.getSheetAt(0);
      }
      this.currentRow = executionContext.getInt("currentRow", 0);
      this.currentSheet = executionContext.getInt("currentSheet", 0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public T read()
      throws NoSuchFieldException,
          InvocationTargetException,
          NoSuchMethodException,
          InstantiationException,
          IllegalAccessException {
//    첫 행은 헤더로 읽고 fieldsNames 정의
    if (currentRow == 0) readHeaders();
//    마지막 시트까지 읽었을 때 종료
    if (currentSheet >= workbook.getNumberOfSheets()) {
      return null;
    }
//    현 시트의 마지막 행까지 읽었을 때 다음 시트의 첫 행으로 다시 읽음
    if (currentRow > sheet.getLastRowNum()) {
      currentSheet++;
      if (currentSheet >= workbook.getNumberOfSheets()) {
        return null;
      }
      this.sheet = this.workbook.getSheetAt(currentSheet);
      currentRow = 0;
      return read();
    }

    Row row = this.sheet.getRow(currentRow);
    if (row == null) {
//      빈 행일 때 다음 행 읽음
      currentRow++;
      return read();
    }
    return mapRowToItem(row);
  }

  private T mapRowToItem(Row row)
      throws NoSuchMethodException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException,
          NoSuchFieldException {
    T item = type.getDeclaredConstructor().newInstance();

//    헤더 읽음으로서 정의해놨던 fieldNames 로 entity 의 필드와 매핑하여 entity 출력함
//    entity 의 필드와 매핑했기 때문에 파일 작성 순서가 entity 의 순서와 맞지 않더라도 읽기 가능
    for (int i = 0; i < this.fieldNames.length; i++) {
      String fieldName = this.fieldNames[i];
      Cell cell = row.getCell(i);

      Object value = getCellValue(cell);

      Field field = type.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(item, value);
    }
    currentRow++;
    return item;
  }
//  읽은 각 행의 데이터 타입에 따라 객체 반환
  private Object getCellValue(Cell cell) {
    if (cell == null) {
      return null;
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
        return null;
      }
    }
  }
//  헤더 읽어서 fieldsNames 지정
  private void readHeaders() {
    Row row = this.sheet.getRow(0);
    this.fieldNames = new String[row.getLastCellNum()];
    for (int i = 0; i < row.getLastCellNum(); i++) {
      Cell cell = row.getCell(i);
      this.fieldNames[i] = cell.getStringCellValue();
    }
    currentRow++;
  }
}
