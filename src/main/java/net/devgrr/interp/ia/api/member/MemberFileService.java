package net.devgrr.interp.ia.api.member;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.member.dto.file.MemberFileOptionRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberFileService {
  private final JobLauncher jobLauncher;
  private final Job importMemberJob;
  private final Job exportMemberJob;

  private static final String FILE_DIRECTORY = "C:\\uploads\\";

  public void createDirectory() throws BaseException {
    File directory = new File(FILE_DIRECTORY);
    if (!directory.exists()) {
      boolean created = directory.mkdir();
      if (!created) {
        throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패하였습니다.");
      }
    }
  }

  public void uploadMemberFile(MultipartFile file, String dataSkip)
      throws IOException, JobExecutionException, BaseException {
    createDirectory();

    //    upload 한 파일 읽기 위해 서버에 저장
    String filePath = FILE_DIRECTORY + file.getOriginalFilename();
    File savedFile = new File(filePath);
    file.transferTo(savedFile);

    //    batch step 에 동적으로 파일 위치 지정
    JobParameters jobParameter =
        new JobParametersBuilder()
            .addString("filePath", savedFile.getAbsolutePath())
            .addString("dataSkip", dataSkip)
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
    //    batch job 실행
    jobLauncher.run(importMemberJob, jobParameter);
    //    db 에 data 저장된 후 파일 삭제
    deleteFile(savedFile);
  }

  public File downloadMemberFile(
      MultipartFile file, MemberFileOptionRequest memberFileOptionRequest)
      throws JobExecutionException, BaseException, IOException {
    createDirectory();
    File savedFile;

    if (file == null || file.isEmpty()) {
      savedFile = downloadByNonFormat(memberFileOptionRequest);
    } else {
      savedFile = downloadAtFile(file, memberFileOptionRequest);
    }
    return savedFile;
  }

  public File downloadAtFile(MultipartFile file, MemberFileOptionRequest m)
      throws IOException, JobExecutionException {

    String filePath = "";
    String fileName = m.fileName();
    if (fileName == null) {
      filePath = FILE_DIRECTORY + "Data_" + file.getOriginalFilename();
    } else {
      filePath =
          FILE_DIRECTORY + fileName + "." + FilenameUtils.getExtension(file.getOriginalFilename());
    }
    File savedFile = new File(filePath);
    file.transferTo(savedFile);

    String cols = "";
    if (m.columns() != null && !m.columns().isEmpty()) {
      cols = String.join(",", m.columns());
    }

    //    job step 에 동적으로 파라미터 지정
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", savedFile.getAbsolutePath())
            .addString("dataFormat", m.dataFormat())
            .addString("header", String.valueOf(m.header()))
            .addString("columns", cols)
            .addString("classType", "Member")
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    jobLauncher.run(exportMemberJob, jobParameters);

    return new File(savedFile.getAbsolutePath());
  }

  public File downloadByNonFormat(MemberFileOptionRequest m)
      throws JobExecutionException, BaseException {
    String extension = "";
    if (m.fileType().isBlank()) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "확장자 입력이 없습니다.");
    }
    if ("csv".equals(m.fileType())) {
      extension = ".csv";
    }
    if ("xlsx".equals(m.fileType())) {
      extension = ".xlsx";
    }
    if ("xls".equals(m.fileType())) {
      extension = ".xls";
    }
    //    resource 를 클라이언트에게 전송하기 전 서버에 먼저 저장하기 위해 path 선언
    String fileName = m.fileName();
    if (m.fileName() == null || m.fileName().isEmpty()) {
      LocalDate today = LocalDate.now();
      fileName = "Member_data_" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + extension;
    } else {
      fileName = m.fileName() + extension;
    }
    String filePath = FILE_DIRECTORY + fileName;

    String cols = "";
    if (m.columns() != null && !m.columns().isEmpty()) {
      cols = String.join(",", m.columns());
    }

    //    job step 에 동적으로 파라미터 지정
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", filePath)
            .addString("dataFormat", "")
            .addString("header", String.valueOf(m.header()))
            .addString("columns", cols)
            .addString("classType", "Member")
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    jobLauncher.run(exportMemberJob, jobParameters);

    return new File(filePath);
  }

  public void deleteFile(File file) {
    if (file.exists()) {
      boolean deleted = file.delete();
    }
  }

  public void getStreamingResponse(OutputStream outputStream, File saveFile) {
    try (FileInputStream fileInputStream = new FileInputStream(saveFile)) {
      InputStream inputStream = new BufferedInputStream(fileInputStream);
      byte[] buffer = new byte[1024];
      int bytesRead;
      //  파일에서 데이터 읽고 outputStream 으로 전송
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
        //  버퍼에 데이터 쌓이는 것 방지
        outputStream.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      //  garbage collector 실행 -> 메모리 회수
      //  엑셀 파일의 경우 responseEntity 반환된 이후에도 열려있기 때문에 가비지 컬렉터 실행시켜 메모리 회수함
      System.gc();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } finally {
        deleteFile(saveFile);
      }
    }
  }
}
