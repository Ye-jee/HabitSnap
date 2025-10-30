package com.habitsnap.api;

import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExceptionController {

    @GetMapping("/api/test/error")
    public String throwError(){
        throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
