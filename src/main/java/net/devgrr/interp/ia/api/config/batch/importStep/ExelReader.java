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
  @Setter private Resource resource;
  @Setter private boolean isXlsx;

  private Workbook workbook;
  private Sheet sheet;

  private int currentRow = 0;
  private int currentSheet = 0;

  private String[] fieldNames;

  private Class<T> type;

  public ExelReader(Class<T> type) {
    this.type = type;
  }

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
    if (currentRow == 0) readHeaders();
    if (currentSheet >= workbook.getNumberOfSheets()) {
      return null;
    }
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
