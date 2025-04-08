package net.devgrr.interp.ia.api.work.issue.file;

import static net.devgrr.interp.ia.api.util.DateUtil.formatDate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.member.dto.MemberResponse;
import net.devgrr.interp.ia.api.work.issue.dto.IssueResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IssueFileService {

  public void exportIssuesToCsv(List<IssueResponse> issues, OutputStream outputStream)
      throws IOException {

    try {
      BufferedWriter writer =
          new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
      writer.write("\uFEFF");

      writer.write("프로젝트 ID,프로젝트 제목,ID,제목,부제목,유형,상태,중요도,생성자,담당자,생성일,수정일,시작일,종료일,기한일,상위이슈 ID\n");

      for (IssueResponse issue : issues) {
        writer.write(
            String.format(
                "%d,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                issue.parentProject().id(),
                issue.parentProject().title(),
                issue.id(),
                issue.title(),
                issue.subTitle() != null ? issue.subTitle() : "",
                issue.type() != null ? issue.type().getValue() : "",
                issue.status() != null ? issue.status().getValue() : "",
                issue.priority() != null ? issue.priority().getValue() : "",
                issue.creator().name(),
                issue.assignee().stream()
                    .map(MemberResponse::name)
                    .reduce((a, b) -> a + "/" + b)
                    .orElse(""),
                issue.createdDate() != null ? formatDate(issue.createdDate()) : "",
                issue.updatedDate() != null ? formatDate(issue.updatedDate()) : "",
                issue.startDate() != null ? formatDate(issue.startDate()) : "",
                issue.endDate() != null ? formatDate(issue.endDate()) : "",
                issue.dueDate() != null ? formatDate(issue.dueDate()) : "",
                issue.parentIssue() != null ? issue.parentIssue().id() : ""));
      }

      writer.flush();
      writer.close();

    } catch (IOException e) {
      log.error("Failed to export issues to CSV.", e);
      throw new IOException(e);
    }
  }

  public void exportIssuesToXlsx(List<IssueResponse> issues, OutputStream outputStream)
      throws IOException {

    try {
      Workbook workbook = new XSSFWorkbook();
      Sheet sheet = workbook.createSheet("Issues");

      Row header = sheet.createRow(0);
      String[] columns = {
        "프로젝트 ID", "프로젝트 제목", "ID", "제목", "부제목", "유형", "상태", "중요도", "생성자", "담당자", "생성일", "수정일",
        "시작일", "종료일", "기한일", "상위이슈 ID"
      };
      for (String column : columns) {
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue(column);
      }

      int rowIdx = 1;
      for (IssueResponse issue : issues) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(issue.parentProject().id());
        row.createCell(1).setCellValue(issue.parentProject().title());
        row.createCell(2).setCellValue(issue.id());
        row.createCell(3).setCellValue(issue.title());
        row.createCell(4).setCellValue(issue.subTitle());
        row.createCell(5).setCellValue(issue.type() != null ? issue.type().getValue() : null);
        row.createCell(6).setCellValue(issue.status() != null ? issue.status().getValue() : null);
        row.createCell(7)
            .setCellValue(issue.priority() != null ? issue.priority().getValue() : null);
        row.createCell(8).setCellValue(issue.creator().name());
        row.createCell(9)
            .setCellValue(
                issue.assignee().stream()
                    .map(MemberResponse::name)
                    .reduce((a, b) -> a + "/" + b)
                    .orElse(""));
        row.createCell(10)
            .setCellValue(issue.createdDate() != null ? formatDate(issue.createdDate()) : null);
        row.createCell(11)
            .setCellValue(issue.updatedDate() != null ? formatDate(issue.updatedDate()) : null);
        row.createCell(12)
            .setCellValue(issue.startDate() != null ? formatDate(issue.startDate()) : null);
        row.createCell(13)
            .setCellValue(issue.endDate() != null ? formatDate(issue.endDate()) : null);
        row.createCell(14)
            .setCellValue(issue.dueDate() != null ? formatDate(issue.dueDate()) : null);
        if (issue.parentIssue() != null) {
          row.createCell(15).setCellValue(issue.parentIssue().id());
        }
      }

      workbook.write(outputStream);
      workbook.close();

    } catch (IOException e) {
      log.error("Failed to export issues to XLSX.", e);
      throw new IOException(e);
    }
  }
}
