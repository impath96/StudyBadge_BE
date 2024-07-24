package com.tenten.studybadge.common.exception.basic;

import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Custom한 예외 처리
    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(AbstractException e) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, e.getHttpStatus());
    }

    // 요청 body값이 잘못됐을 때 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(errors);
    }

    // 잘못된 인수 또는 상태일 때 발생하는 예외 처리
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(RuntimeException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("BAD_REQUEST")
            .message(ex.getMessage())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 파일 업로드 크기 초과 예외 처리
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("FILE_SIZE_LIMIT_EXCEEDED")
            .message("업로드 하려는 파일이 너무 큽니다. 2MB 이하만 업로드 가능합니다.")
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // NullPointerException 및 기타 일반 예외 처리
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred: " + ex.getMessage())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
