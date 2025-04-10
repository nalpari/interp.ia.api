package net.devgrr.interp.ia.api.member.file;

import java.io.*;
import java.nio.file.FileSystems;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.member.file.dto.MemberFileOptionRequest;
import net.devgrr.interp.ia.api.member.file.exportData.FilesWriter;
import net.devgrr.interp.ia.api.member.file.importData.FileReader;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
  private final FileReader fileReader;
  private final FilesWriter filesWriter;

  private static final String FILE_DIRECTORY =
      System.getProperty("user.dir")
          + FileSystems.getDefault().getSeparator()
          + "uploads"
          + FileSystems.getDefault().getSeparator();

  public void createDirectory() throws BaseException {
    File directory = new File(FILE_DIRECTORY);
    if (!directory.exists()) {
      boolean created = directory.mkdir();
      if (!created) {
        throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패하였습니다.");
      }
    }
  }

  public void uploadMemberFile(MultipartFile file) throws Exception {
    createDirectory();
    String filePath = FILE_DIRECTORY + file.getOriginalFilename();
    File savedFile = new File(filePath);
    file.transferTo(savedFile);

    fileReader.fileReader(filePath);

    deleteFile(savedFile);
  }

  public File downloadMemberFile(MemberFileOptionRequest request)
      throws BaseException, IOException {
    createDirectory();
    String extension = "";
    if (request.fileType() == null || request.fileType().isEmpty()) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "확장자 입력이 없습니다.");
    }
    if ("csv".equals(request.fileType())) {
      extension = ".csv";
    }
    if ("xlsx".equals(request.fileType())) {
      extension = ".xlsx";
    }
    if ("xls".equals(request.fileType())) {
      extension = ".xls";
    }
    String fileName = request.fileName();
    if (request.fileName() == null || request.fileName().isEmpty()) {
      LocalDate today = LocalDate.now();
      fileName = "Member_data_" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + extension;
    } else {
      fileName = request.fileName() + extension;
    }
    String filePath = FILE_DIRECTORY + fileName;

    filesWriter.filesWriter(request.header(), request.columns(), filePath);

    return new File(filePath);
  }


  public void deleteFile(File file) {
    if (file.exists()) {
      boolean deleted = file.delete();
    }
  }

  public void getStreamingResponse(HttpServletResponse response, File saveFile) {
    try (InputStream inputStream = new FileInputStream(saveFile);
        OutputStream outputStream = response.getOutputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
        outputStream.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      System.gc();
      deleteFile(saveFile);
    }
  }
}
