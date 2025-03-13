package net.devgrr.interp.ia.api.config.batch.exportStep;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.core.io.Resource;

@AllArgsConstructor
@NoArgsConstructor
public class ExelWriter<T> implements ItemStreamWriter<T> {
  /// 이후 다른 엔티티 데이터 export 위해 제너릭타입으로 지정

  @Setter private Resource resource;
  @Setter private boolean isXlsx;

  private Workbook workbook;
  private Sheet sheet;

  private String[] fieldsName;

//  reflection
  private Class<T> type;
  public ExelWriter(Class<T> type) {
    this.type = type;
  }

//  .xlsx, .xls 에 따라 workbook 지정
//  실제 파일 작성 전(write)에 호출됨
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (isXlsx) {
      this.workbook = new XSSFWorkbook();
    } else {
      this.workbook = new HSSFWorkbook();
    }
    this.sheet = this.workbook.createSheet("Sheet1");
  }
//  엑셀 파일 작성 메소드
//  chunk 단위로 파일을 읽음
  @Override
  public void write(Chunk<? extends T> chunk) throws Exception {
    if (chunk.isEmpty()) return;
//    읽어온 엔티티의 필드들을 가져와서 헤더로 지정
    if (fieldsName == null) {
      fieldsName = getFieldNames();
      createHeaderRow();
    }
//    읽어온 Member 의 chunk 들을 한 줄씩 row 로 작성
    for (T item : chunk) {
      createDataRow(item);
    }
  }

  private void createDataRow(T item) throws IllegalAccessException {
//    실제 데이터가 존재하는 행의 개수 반환 후 행 지정 ->
//    1열에 header(entity fields) 가 붙어있기 때문에 fields 의 개수만큼 행이 생성됨
    int rowNum = this.sheet.getPhysicalNumberOfRows();
    Row row = this.sheet.createRow(rowNum);

    for (int i = 0; i < fieldsName.length; i++) {
      String fieldName = fieldsName[i];
      Field field;
      try {
//        지정한 entity class reflection -> 정의된 fields 가져오기 위함
        field = item.getClass().getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        continue;
      }
//      Member private 객체 접근 허용하여 item(Member) 객체에서 필드의 가져옴
      field.setAccessible(true);
      Object value = field.get(item);
//      타입에 맞게 엑셀 파일 작성
//      ex: email, password 는 string 이므로 string 으로 엑셀파일에 작성하게 됨
      Cell cell = row.createCell(i);
      if (value != null) {
        if (value instanceof String) {
          cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
          cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
          cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
          cell.setCellValue((Boolean) value);
        } else {
          cell.setCellValue(value.toString());
        }
//        value 가 null 일 경우에는 빈 값으로 작성
      } else {
        cell.setCellValue("");
      }
    }
  }

//  Member 의 fields 가져와서 header 에 작성함
  private void createHeaderRow() {
    Row headerRow = this.sheet.createRow(0);
    for (int i = 0; i < fieldsName.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(fieldsName[i]);
    }
  }
  private String[] getFieldNames() {
    Field[] fields = type.getDeclaredFields();
    String[] fieldNames = new String[fields.length];
    for (int i = 0; i < fields.length; i++) {
      fieldNames[i] = fields[i].getName();
    }
    return fieldNames;
  }

//  엑셀 파일 service 에서 받은 경로로 내보냄
  @Override
  public void close() throws ItemStreamException {
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(resource.getFile());
      workbook.write(fileOutputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
