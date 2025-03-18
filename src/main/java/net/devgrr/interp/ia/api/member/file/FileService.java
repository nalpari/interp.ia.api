package net.devgrr.interp.ia.api.member.file;

import java.io.*;
import java.nio.file.FileSystems;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    //    upload 한 파일 읽기 위해 서버에 저장
    String filePath = FILE_DIRECTORY + file.getOriginalFilename();
    File savedFile = new File(filePath);
    file.transferTo(savedFile);

    fileReader.fileReader(filePath);

    deleteFile(savedFile);
  }

  public File downloadMemberFile(
      MultipartFile file, MemberFileOptionRequest memberFileOptionRequest)
      throws BaseException, IOException {
    createDirectory();
    File savedFile;

    if (file == null || file.isEmpty()) {
      savedFile = downloadByNonFormat(memberFileOptionRequest);
    } else {
      savedFile = downloadByFormat(file, memberFileOptionRequest);
    }
    return savedFile;
  }

  public File downloadByFormat(MultipartFile file, MemberFileOptionRequest m) throws IOException, BaseException {

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

    filesWriter.filesWriter(m.header(), m.columns(), filePath, m.dataFormat());

    return new File(savedFile.getAbsolutePath());
  }

  public File downloadByNonFormat(MemberFileOptionRequest m) throws BaseException, IOException {
    String extension = "";
    if (m.fileType() == null || m.fileType().isEmpty()) {
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
    String fileName = m.fileName();
    if (m.fileName() == null || m.fileName().isEmpty()) {
      LocalDate today = LocalDate.now();
      fileName = "Member_data_" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + extension;
    } else {
      fileName = m.fileName() + extension;
    }
    String filePath = FILE_DIRECTORY + fileName;

    filesWriter.filesWriter(m.header(), m.columns(), filePath, "");

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
