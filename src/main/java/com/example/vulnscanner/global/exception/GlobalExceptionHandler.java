package com.example.vulnscanner.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.badRequest().body(Map.of("error", "파일 크기가 너무 큽니다. (제한: " + exc.getMaxUploadSize() + ")"));
    }

    @ExceptionHandler(Exception.class)
    public Object handleAllExceptions(Exception ex, org.springframework.web.context.request.WebRequest request) {
        ex.printStackTrace(); // 서버 로그에 스택 트레이스 출력

        // AJAX 요청인지 확인 (헤더 또는 Accept 타입)
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");

        boolean isAjax = "XMLHttpRequest".equals(requestedWith)
                || (accept != null && accept.contains("application/json"));

        if (isAjax) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부 오류가 발생했습니다: " + ex.getMessage()));
        } else {
            org.springframework.web.servlet.ModelAndView mav = new org.springframework.web.servlet.ModelAndView(
                    "error");
            mav.addObject("error", ex.getMessage());
            return mav;
        }
    }
}