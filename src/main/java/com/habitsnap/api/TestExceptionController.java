package com.habitsnap.api;

import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class TestExceptionController {

    @GetMapping("/api/test/error")
    public String throwError(){
        throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
