package net.devgrr.interp.ia.api.member.file.importData;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.SneakyThrows;
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

  @Setter private String[] fieldNames;
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

  public Stream<T> read() {
    if(this.workbook == null) {
      this.open();
    }
    if(currentSheet >= workbook.getNumberOfSheets()) {
      return null;
    }

    if(currentRow > sheet.getLastRowNum()) {
      currentSheet++;
      if(currentSheet >= workbook.getNumberOfSheets()) {
        return null;
      }
      this.sheet = workbook.getSheetAt(currentSheet);
      currentRow = 1;
      return read();
    }

    Row row = sheet.getRow(currentRow);
    if(row == null) {
      currentRow++;
      return read();
    }
    Iterator<Row> rowIterator = sheet.iterator();
    if(rowIterator.hasNext()) {
      rowIterator.next();
    }

    return Stream.generate(() -> rowIterator.hasNext() ? rowIterator.next() : null)
            .takeWhile(Objects::nonNull)
            .map(rowData-> {
              try {
                return mappingData(rowData);
              } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                return null;
              }
            });
  }

  private T mappingData(Row row) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    List<Object> values = new ArrayList<>();
    for (int i =0; i<fieldNames.length; i++) {
      Cell cell = row.getCell(i);

      Object value = getCellValue(cell);
      values.add(value.toString());
    }
    Constructor<T> constructor = clazz.getDeclaredConstructor(
            Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getType)
                    .toArray(Class[]::new)
    );
    currentRow++;
    return constructor.newInstance(values.toArray());
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
        return "";
      }
    }
  }
}
