package net.devgrr.interp.ia.api.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
  private final String message;
  private final HttpStatus status;

  public ErrorResponse(ErrorCode errorCode) {
    this.message = errorCode.getMessage();
    this.status = errorCode.getStatus();
  }

  public ErrorResponse(ErrorCode errorCode, String message) {
    this.message = message;
    this.status = errorCode.getStatus();
  }
}
