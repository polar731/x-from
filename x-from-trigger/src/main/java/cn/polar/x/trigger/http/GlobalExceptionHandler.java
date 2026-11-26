package cn.polar.x.trigger.http;

import cn.polar.x.api.response.Response;
import cn.polar.x.types.enums.ResponseCode;
import cn.polar.x.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理：业务异常转换为标准响应码
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public Response<Void> handleAppException(AppException e) {
        log.warn("业务异常 code:{} info:{}", e.getCode(), e.getInfo());
        return Response.<Void>builder()
                .code(e.getCode() == null ? ResponseCode.UN_ERROR.getCode() : e.getCode())
                .info(e.getInfo() == null ? ResponseCode.UN_ERROR.getInfo() : e.getInfo())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常", e);
        return Response.<Void>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(e.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Response.<Void>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("系统繁忙，请稍后重试")
                .build();
    }

}
