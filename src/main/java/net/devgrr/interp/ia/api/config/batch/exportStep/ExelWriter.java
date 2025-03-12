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
  @Setter private Resource resource;
  @Setter private boolean isXlsx;

  private Workbook workbook;
  private Sheet sheet;

  private String[] fieldsName;

  private Class<T> type;

  public ExelWriter(Class<T> type) {
    this.type = type;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    try {
      FileOutputStream fos = new FileOutputStream(resource.getFile(), true);
      if (isXlsx) {
        this.workbook = new XSSFWorkbook();
      } else {
        this.workbook = new HSSFWorkbook();
      }
      this.sheet = this.workbook.createSheet("Sheet1");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(Chunk<? extends T> chunk) throws Exception {
    if (chunk.isEmpty()) return;

    if (fieldsName == null) {
      fieldsName = getFieldNames();
      createHeaderRow();
    }

    for (T item : chunk) {
      createDataRow(item);
    }
  }

  private void createDataRow(T item) throws IllegalAccessException {
    int rowNum = this.sheet.getPhysicalNumberOfRows();
    Row row = this.sheet.createRow(rowNum);

    for (int i = 0; i < fieldsName.length; i++) {
      String fieldName = fieldsName[i];
      Field field;
      try {
        field = item.getClass().getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        continue;
      }
      field.setAccessible(true);
      Object value = field.get(item);

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
      } else {
        cell.setCellValue("");
      }
    }
  }

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
