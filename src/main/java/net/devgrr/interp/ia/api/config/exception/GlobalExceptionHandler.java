package net.devgrr.interp.ia.api.config.exception;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.attoparser.ParseException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.jdbc.BatchFailedException;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ErrorResponse> handle(HttpRequestMethodNotSupportedException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.METHOD_NOT_ALLOWED));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
    final String errorMessages =
        e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.INVALID_INPUT_VALUE, errorMessages));
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handle(Exception e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
  }

  @ExceptionHandler(BaseException.class)
  protected ResponseEntity<ErrorResponse> handle(BaseException e) {
    final ErrorCode errorCode = e.getErrorCode();
    final String errorMessage = e.getMessage();
    return ResponseEntity.status(errorCode.getStatus())
        .body(new ErrorResponse(errorCode, errorMessage));
  }

  @ExceptionHandler(AuthenticationException.class)
  protected ResponseEntity<ErrorResponse> handle(AuthenticationException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.FORBIDDEN));
  }

  @ExceptionHandler(ResponseStatusException.class)
  protected ResponseEntity<ErrorResponse> handle(ResponseStatusException e) {
    if (e.getStatusCode().is4xxClientError()) {
      return ResponseEntity.badRequest()
          .body(new ErrorResponse(ErrorCode.INVALID_INPUT_VALUE, e.getMessage()));
    } else {
      return ResponseEntity.internalServerError()
          .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
  }

  @ExceptionHandler(BatchFailedException.class)
  protected ResponseEntity<ErrorResponse> handle(BatchFailedException e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
  }

  @ExceptionHandler(JobExecutionException.class)
  protected ResponseEntity<ErrorResponse> handle(JobExecutionException e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
  }

  @ExceptionHandler(ParseException.class)
  protected ResponseEntity<ErrorResponse> handle(ParseException e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ErrorResponse> handle(ConstraintViolationException e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
  }
}
