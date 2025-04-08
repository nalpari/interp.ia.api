package net.devgrr.interp.ia.api.work.project.file;

import static net.devgrr.interp.ia.api.util.DateUtil.formatDate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;
import net.devgrr.interp.ia.api.work.project.dto.ProjectResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProjectFileService {

  public void exportProjectsToCsv(List<ProjectResponse> projects, OutputStream outputStream)
      throws IOException {

    try {
      BufferedWriter writer =
          new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
      writer.write("\uFEFF");

      writer.write("ID,제목,부제목,상태,중요도,생성자,담당자,생성일,수정일,시작일,종료일,기한일\n");

      for (ProjectResponse project : projects) {
        writer.write(
            String.format(
                "%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                project.id(),
                project.title(),
                project.subTitle() != null ? project.subTitle() : "",
                project.status() != null ? project.status() : "",
                project.priority() != null ? project.priority() : "",
                project.creator().name(),
                project.assignee().stream()
                    .map(MemberResponse::name)
                    .reduce((a, b) -> a + ";" + b)
                    .orElse(""),
                project.createdDate() != null ? formatDate(project.createdDate()) : "",
                project.updatedDate() != null ? formatDate(project.updatedDate()) : "",
                project.startDate() != null ? formatDate(project.startDate()) : "",
                project.endDate() != null ? formatDate(project.endDate()) : "",
                project.dueDate() != null ? formatDate(project.dueDate()) : ""));
      }

      writer.flush();
      writer.close();

    } catch (IOException e) {
      log.error("Failed to export projects to CSV.", e);
      throw new IOException(e);
    }
  }

  public void exportProjectsToXlsx(List<ProjectResponse> projects, OutputStream outputStream)
      throws IOException {

    try {
      Workbook workbook = new XSSFWorkbook();
      Sheet sheet = workbook.createSheet("Projects");

      Row header = sheet.createRow(0);
      String[] columns = {
        "ID", "제목", "부제목", "상태", "중요도", "생성자", "담당자", "생성일", "수정일", "시작일", "종료일", "기한일"
      };
      for (String column : columns) {
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue(column);
      }

      int rowIdx = 1;
      for (ProjectResponse project : projects) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(project.id());
        row.createCell(1).setCellValue(project.title());
        row.createCell(2).setCellValue(project.subTitle());
        row.createCell(3)
            .setCellValue(project.status() != null ? project.status().getValue() : null);
        row.createCell(4)
            .setCellValue(project.priority() != null ? project.priority().getValue() : null);
        row.createCell(5).setCellValue(project.creator().name());
        row.createCell(6)
            .setCellValue(
                project.assignee().stream()
                    .map(MemberResponse::name)
                    .reduce((a, b) -> a + ";" + b)
                    .orElse(""));
        row.createCell(7)
            .setCellValue(project.createdDate() != null ? formatDate(project.createdDate()) : null);
        row.createCell(8)
            .setCellValue(project.updatedDate() != null ? formatDate(project.updatedDate()) : null);
        row.createCell(9)
            .setCellValue(project.startDate() != null ? formatDate(project.startDate()) : null);
        row.createCell(10)
            .setCellValue(project.endDate() != null ? formatDate(project.endDate()) : null);
        row.createCell(11)
            .setCellValue(project.dueDate() != null ? formatDate(project.dueDate()) : null);
      }

      workbook.write(outputStream);
      workbook.close();

    } catch (IOException e) {
      log.error("Failed to export projects to XLSX.", e);
      throw new IOException(e);
    }
  }
}
