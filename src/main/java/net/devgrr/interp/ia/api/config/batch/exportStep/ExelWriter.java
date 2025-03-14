package net.devgrr.interp.ia.api.config.batch.exportStep;

import java.io.FileInputStream;
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
  @Setter private String dataFormat;
  @Setter private boolean header;

  private Workbook workbook;
  private Sheet sheet;

  @Setter private String[] fieldsName;

  private int row;
  private int col;

  @Setter private Class<?> clazz;

  //  .xlsx, .xls 에 따라 workbook 지정
  //  실제 파일 작성 전(write)에 호출됨
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (dataFormat != null) {
      setRowColByDataFormat();
//      포맷 옵션이 있으면 클라이언트로부터 받은 파일을 열고 작성
      doOpenResource();
      return;
    }
    if (isXlsx) {
      this.workbook = new XSSFWorkbook();
    } else {
      this.workbook = new HSSFWorkbook();
    }
    this.sheet = this.workbook.createSheet("Sheet1");
  }

  private void setRowColByDataFormat() {
    this.col = Character.toLowerCase(dataFormat.charAt(0)) - 'a';
    this.row = Integer.parseInt(dataFormat.substring(1)) - 1;
  }

  private void doOpenResource() {
    try {
      FileInputStream fileInputStream = new FileInputStream(resource.getFile());
      if (isXlsx) {
        this.workbook = new XSSFWorkbook(fileInputStream);
      } else {
        this.workbook = new HSSFWorkbook(fileInputStream);
      }
      this.sheet = this.workbook.getSheetAt(0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  //  엑셀 파일 작성 메소드
  //  chunk 단위로 파일을 읽음
  @Override
  public void write(Chunk<? extends T> chunk) throws Exception {
    if (chunk.isEmpty()) return;
//    columns 옵션이 없으면 지정한 클래스로부터 필드들 읽어옴
    if (fieldsName == null) {
      fieldsName = getFieldNames();
    }
    //    header 옵션에 따라 읽어온 엔티티의 필드들을 가져와서 헤더로 지정
    if (header) {
      createHeaderRow();
    }
    //    읽어온 Member 의 chunk 들을 한 줄씩 row 로 작성
    for (T item : chunk) {
      createDataRow((Object[]) item);
    }
  }

  private void createDataRow(Object[] item) throws IllegalAccessException {
    //    실제 데이터가 존재하는 행의 개수 반환 후 행 지정 ->
    //    1열에 header(entity fields) 가 붙어있기 때문에 fields 의 개수만큼 행이 생성됨
    int rowNum = this.sheet.getPhysicalNumberOfRows();
    if (dataFormat != null) {
      rowNum = this.row;
    }
    Row row = this.sheet.createRow(rowNum);
    for (int i = 0; i < fieldsName.length; i++) {
      Object value = item[i];

      int colIndex;
      if (dataFormat != null) {
        colIndex = col + i;
      } else {
        colIndex = i;
      }
      Cell cell = row.createCell(colIndex);
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
    if (dataFormat != null) {
      this.row++;
    }
  }

  //  Member 의 fields 가져와서 header 에 작성함
  private void createHeaderRow() {
    int row;
    if (dataFormat != null) {
      row = this.row;
    } else {
      row = 0;
    }
    Row headerRow = this.sheet.createRow(row);
    for (int i = 0; i < fieldsName.length; i++) {
      int colIndex = i;
      if (dataFormat != null) {
        colIndex = col + i;
      }
      Cell cell = headerRow.createCell(colIndex);
      cell.setCellValue(fieldsName[i]);
    }
    if (dataFormat != null) {
      this.row++;
    }
  }

  private String[] getFieldNames() {
    Field[] fields = clazz.getDeclaredFields();
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
