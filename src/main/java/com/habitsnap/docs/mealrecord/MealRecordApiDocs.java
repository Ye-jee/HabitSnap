package com.habitsnap.docs.mealrecord;

/* HabitSnap - MealRecord API 문서 어노테이션 모음
* 컨트롤러 코드(MealRecordController)의 가독성을 높이기 위해 Swagger 관련 문서를 별도로 정의함
* */

import com.habitsnap.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MealRecordApiDocs {

    // 1) 식사 기록 등록(사진 포함) - Create / POST 메서드
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(
            summary = "식사 기록 등록 API",
            description = """
            사진 파일을 함께 업로드하면 EXIF 메타데이터에서 촬영 시각을 추출하여 mealDate, mealTime에 자동 반영됨. (사진 파일 업로드는 선택)
            ✅ multipart/form-data 형식 요청  
            - data: JSON (MealRecordCreateRequest)  
            - photo: 이미지 파일 (선택)  
            
            ✅ JWT 인증 필요 (Bearer Token)
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "식사 기록 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = com.habitsnap.docs.ApiExamples.CREATE_MEAL_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 형식 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    // TODO examples 추가하기
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 인증 실패 또는 토큰 만료",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    // TODO examples 추가하기
            )
    })
    public @interface CreateMealRecordDocs {}



    // 2) 식사 기록 단건 조회 - Read (by ID) / GET 메서드
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(
            summary = "식사 기록 단건 조회 API",
            description = """
            ✅ 특정 식사 기록 ID로 단일 데이터 조회  
            ✅ 존재하지 않는 ID 요청 시 MEAL_NOT_FOUND 예외 발생
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = com.habitsnap.docs.ApiExamples.GET_MEAL_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 식사 기록",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = com.habitsnap.docs.ApiExamples.MEAL_NOT_FOUND)
                    )
            )
    })
    public @interface GetMealRecordDocs {}



    // 3) 특정 날짜의 식사 기록 조회 - Read (by Date) / GET 메서드



    // 4) 특정 기간의 식사 기록 조회 - Read (by Period) / GET 메서드



    // 5) 식사 기록 수정 - Update / PUT 메서드를 PATCH 메서드로 수정함



    // 6) 식사 기록 삭제 - Delete / DELETE 메서드
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(
            summary = "식사 기록 삭제 API",
            description = """
            ✅ 특정 식사 기록을 삭제 
            ✅ 존재하지 않는 ID 요청 시 MEAL_NOT_FOUND 예외 발생
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = com.habitsnap.docs.ApiExamples.DELETE_MEAL_SUCCESS)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 식사 기록",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = com.habitsnap.docs.ApiExamples.MEAL_NOT_FOUND)
                    )
            )
    })
    public @interface DeleteMealRecordDocs {}


}
