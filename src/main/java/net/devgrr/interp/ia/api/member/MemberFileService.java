package net.devgrr.interp.ia.api.member;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

  public void uploadMemberFile(MultipartFile file)
      throws IOException, JobExecutionException, BaseException {
    createDirectory();

    String filePath = FILE_DIRECTORY + file.getOriginalFilename();
    File savedFile = new File(filePath);
    file.transferTo(savedFile);

    JobParameters jobParameter =
        new JobParametersBuilder()
            .addString("filePath", savedFile.getAbsolutePath())
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    jobLauncher.run(importMemberJob, jobParameter);

    deleteFile(savedFile);
  }

  public File downloadMemberFile(String fileType) throws JobExecutionException, BaseException {
    createDirectory();

    LocalDate today = LocalDate.now();
    String extension = "";
    if (fileType.isBlank()) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "확장자 입력이 없습니다.");
    }
    if ("csv".equals(fileType)) {
      extension = ".csv";
    }
    if ("xlsx".equals(fileType)) {
      extension = ".xlsx";
    }
    if ("xls".equals(fileType)) {
      extension = ".xls";
    }

    String fileName =
        "Member_data_" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + extension;
    String filePath = FILE_DIRECTORY + fileName;
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", filePath)
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    jobLauncher.run(exportMemberJob, jobParameters);

    return new File(filePath);
  }

  public void deleteFile(File file) {
    if (file.exists()) {
      boolean delete = file.delete();
    }
  }
}
