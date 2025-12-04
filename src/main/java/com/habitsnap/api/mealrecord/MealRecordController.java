package com.habitsnap.api.mealrecord;

import com.habitsnap.application.mealrecord.MealRecordService;
import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.config.CustomUserDetails;
import com.habitsnap.docs.mealrecord.MealRecordApiDocs.*;
import com.habitsnap.dto.mealrecord.MealRecordCreateRequest;
import com.habitsnap.dto.mealrecord.MealRecordResponse;
import com.habitsnap.dto.mealrecord.MealRecordUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/* 식사기록(MealRecord) 관련 API 컨트롤러
* - CRUD 및 특정날짜/기간별 조회 엔드포인트 관리
* - JWT 인증 기반으로 로그인한 사용자 정보(@AuthenticationPrincipal) 활용
* */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meals")
@Tag(name = "MealRecord", description = "식사 기록 CRUD API")
public class MealRecordController {

    private final MealRecordService mealRecordService;

    // 1) 식사 기록 등록(사진 포함) - Create / POST 메서드
    /*@Operation(summary = "식사 기록 등록 API",
            description = """
            사진 파일을 함께 업로드하면 EXIF 메타데이터에서 촬영 시각을 추출하여 mealDate, mealTime에 자동 반영됨. (사진 파일 업로드는 선택)
            <br> multipart/form-data 형식으로 요청
            <br> JWT 인증 필요 (Authorize -> Token 값 입력 (Bearer 제외해서 입력)
            """,
            tags = {"MealRecord"})*/
    @CreateMealRecordDocs
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MealRecordResponse>> createMealRecord(
            @Valid @RequestPart("data") MealRecordCreateRequest request,            // JSON 데이터
            @RequestPart(value = "photo", required = false) MultipartFile photo,    // 사진 파일
            @AuthenticationPrincipal CustomUserDetails userDetails){
            // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음

        Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출

        MealRecordResponse response = mealRecordService.createMealRecordWithPhoto(request, photo, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("식사 기록 등록 성공", response));

    }

    // 2) 식사 기록 단건 조회 - Read (by ID) / GET 메서드
    /*@Operation(summary = "식사 기록 단건 조회 API",
            description = "식사 기록 ID를 이용해 단일 데이터를 조회할 수 있음. 존재하지 않는 ID일 경우 MEAL_NOT_FOUND 예외가 발생함.",
            tags = {"MealRecord"})*/
    @GetMealRecordDocs
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MealRecordResponse>> getMealRecord(@PathVariable Long id){

        MealRecordResponse response = mealRecordService.getMealRecord(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 3) 특정 날짜의 식사 기록 조회 - Read (by Date) / GET 메서드
    @Operation(summary = "특정 날짜의 식사 기록 조회 API",
            description = "사용자의 userId와 조회할 날짜(date)를 기반으로 해당 일자의 모든 식사 기록을 조회함",
            tags = {"MealRecord"})
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<MealRecordResponse>>> getMealRecordsByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails, // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음
            @RequestParam("value") LocalDate date
    ){
        Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출
        List<MealRecordResponse> records = mealRecordService.getMealRecordsByDate(userId, date);

        return ResponseEntity.ok(ApiResponse.success(records));
    }


    // 4) 특정 기간의 식사 기록 조회 - Read (by Period) / GET 메서드
    @Operation(summary = "특정 기간 식사 기록 조회 API",
            description = "사용자의 userId와 기간(start, end)을 입력받아 해당 범위 내 모든 식사 기록을 조회함",
            tags = {"MealRecord"})
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<MealRecordResponse>>> getMealRecordsByPeriod(
            @AuthenticationPrincipal CustomUserDetails userDetails, // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end
    ){
        Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출
        List<MealRecordResponse> records = mealRecordService.getMealRecordsByPeriod(userId, start, end);

        return ResponseEntity.ok(ApiResponse.success(records));
    }


    // 5) 식사 기록 수정 - Update / PUT 메서드를 PATCH 메서드로 수정함
    @Operation(summary = "식사 기록 수정 API",
            description = "식사 기록의 일부 항목만 수정할 수 있음 ('string' 또는 null 값은 기존 데이터에 영향을 주지 않음)",
            tags = {"MealRecord"})
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MealRecordResponse>> updateMealRecord(
            @PathVariable Long id,
            @RequestBody MealRecordUpdateRequest request
    ){
        // PathVariable로 받은 id를 request(요청 객체의 id)에 덮어씀
        request.setId(id);
        MealRecordResponse updated = mealRecordService.updateMealRecord(request);

        return ResponseEntity.ok(ApiResponse.success("식사 기록 수정 성공", updated));
    }


    // 6) 식사 기록 삭제 - Delete / DELETE 메서드
    /*@Operation(summary = "식사 기록 삭제 API",
            description = "식사 기록 ID를 이용해 해당 데이터를 삭제함. 존재하지 않는 ID일 경우 MEAL_NOT_FOUND 예외가 발생함.",
            tags = {"MealRecord"})*/
    @DeleteMealRecordDocs
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMealRecord(@PathVariable Long id){

        mealRecordService.deleteMealRecord(id);

        return ResponseEntity.ok(ApiResponse.success("식사 기록 삭제 성공"));
    }


}
