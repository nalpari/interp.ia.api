package net.devgrr.interp.ia.api.member.file.exportData;

import java.io.*;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExelStreamWriter {
  @Setter private File file;
  @Setter private String dataFormat;
  @Setter private boolean header;
  @Setter private boolean isXlsx;

  private Workbook workbook;
  private Sheet sheet;

  @Setter private String[] fieldNames;

  private int row = 0;
  private int cell = 0;

  public void open() throws IOException {
    if (dataFormat != null && !dataFormat.isEmpty()) {
      setRowCol();
      doOpenFile();
      return;
    }
    if (isXlsx) {
      this.workbook = new XSSFWorkbook();
    } else {
      this.workbook = new HSSFWorkbook();
    }
    this.sheet = workbook.createSheet("Sheet1");
  }

  private void doOpenFile() {
    try {
      FileInputStream inputStream = new FileInputStream(file);
      if (isXlsx) {
        this.workbook = new XSSFWorkbook(inputStream);
      } else {
        this.workbook = new HSSFWorkbook(inputStream);
      }
      this.sheet = workbook.getSheetAt(0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void write(List<Map<String, Object>> data) throws IOException {
    open();

    if (header) {
      createHeader();
    }
    for (Map<String, Object> datum : data) {
      Row r = sheet.createRow(this.row++);
      for (int i = 0; i < fieldNames.length; i++) {
        String column = fieldNames[i];
        Object value = datum.get(column);
        Cell c = r.createCell(this.cell + i);
        if (value != null) {
          if (value instanceof String) {
            c.setCellValue((String) value);
          } else if (value instanceof Integer) {
            c.setCellValue((Integer) value);
          } else if (value instanceof Double) {
            c.setCellValue((Double) value);
          } else if (value instanceof Boolean) {
            c.setCellValue((Boolean) value);
          } else if(value instanceof Long) {
            c.setCellValue((Long) value);
          }else {
            c.setCellValue(String.valueOf(value));
          }
        } else {
          c.setCellValue("");
        }
      }
    }
    close();
  }

  private void close() {
    try {
      FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
      workbook.write(outputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createHeader() {
    Row headerRow = sheet.createRow(row);
    for (int i = 0; i < fieldNames.length; i++) {
      Cell c = headerRow.createCell(this.cell + i);
      c.setCellValue(fieldNames[i]);
    }
    row++;
  }

  private void setRowCol() {
    this.cell = Character.toLowerCase(dataFormat.charAt(0) - 'a');
    this.row = Integer.parseInt(dataFormat.substring(1)) - 1;
  }
}
