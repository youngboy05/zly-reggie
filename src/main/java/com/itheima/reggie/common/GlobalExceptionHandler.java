package com.itheima.reggie.common;
/*
全局异常处理器
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/*
全局异常处理器的声明步骤：
    1、定义全局异常处理类，并使用 @RestControllerAdvice 标记。@RestControllerAdvice = @ControllerAdvice + @RequestBody
        @ControllerAdvice：声明该类为全局异常处理类
        @RequestBody 可以帮助将返回的内容以 JSON 格式封装好发送给前端
    2、定义异常处理方法，该方法需要使用 @ExceptionHandler 注解，且需要制定其 value 属性，也就是要处理的异常类型；
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.info(ex.getMessage()); // 例如：Duplicate entry 'zhangsan' for key 'employee.idx_username'
        if (ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error("创建失败，" + msg);
        }
        return R.error("未知错误");
    }

    @ExceptionHandler(value = CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.info(ex.getMessage()); // 例如：Duplicate entry 'zhangsan' for key 'employee.idx_username'
        return R.error(ex.getMessage());
    }
}
