package com.habitsnap.docs.auth;

/* HabitSnap - Auth API 문서 어노테이션 모음
 * 컨트롤러 코드(AuthController)의 가독성을 높이기 위해 Swagger 관련 문서를 별도로 정의함
 * */

import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.docs.ApiExamples;
import com.habitsnap.dto.auth.LoginRequest;
import com.habitsnap.dto.auth.SignUpRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

public class AuthApiDocs {

    // 회원가입 - 요청 예시, 200 response, 400 response(validation/bad request)
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignUpRequest.class),
                examples = @ExampleObject(
                        value = """
                          {
                            "email": "user@habitsnap.com",
                            "password": "userPassword1234",
                            "nickname": "해빗유저"
                          }
                          """
                )
        )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.SIGNUP_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "회원가입 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.INVALID_INPUT_VALUE)
                    )

            )
    })
    public @interface SignUpDocs {}


    // 로그인 - 요청 예시, 201 response(created), 401 response(unauthorized)
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            value = """
                          {
                            "email": "user@habitsnap.com",
                            "password": "userPassword1234"
                          }
                          """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.LOGIN_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = ApiExamples.LOGIN_FAIL)
                    )

            )
    })
    public @interface LoginDocs {}
}
