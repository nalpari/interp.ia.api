package net.devgrr.interp.ia.api.config.exception;

import lombok.Getter;

@Getter
public class BaseException extends Exception {

  private final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
