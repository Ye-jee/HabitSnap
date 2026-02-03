package com.habitsnap.api.mealrecord;

import com.habitsnap.application.mealrecord.MealRecordService;
import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.config.CustomUserDetails;
import com.habitsnap.docs.mealrecord.MealRecordApiDocs.*;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import com.habitsnap.dto.mealrecord.request.MealRecordCreateRequest;
import com.habitsnap.dto.mealrecord.response.MealRecordResponse;
import com.habitsnap.dto.mealrecord.request.MealRecordUpdateRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@Tag(name = "MealRecord")
/*@SecurityRequirement(name = "bearerAuth")*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meals")
public class MealRecordController {

    private final MealRecordService mealRecordService;

    // 연관관계 추가 후
    private final UserRepository userRepository;

    // 1) 식사 기록 등록(사진 포함) - Create / POST 메서드
    @CreateMealRecordDocs
    @Operation(summary = "식사 기록 등록 API",
            description = "사진 파일을 함께 업로드하면 EXIF 메타데이터에서 촬영 시각을 추출하여 mealDate, mealTime에 자동 반영됩니다. (사진 파일 업로드는 선택)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MealRecordResponse>> createMealRecord(
            @Valid @RequestPart("data") MealRecordCreateRequest request,            // JSON 데이터
            @RequestPart(value = "photo", required = false) MultipartFile photo,    // 사진 파일
            @AuthenticationPrincipal CustomUserDetails userDetails){
            // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음

        // Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출

        // User 엔티티를 조회
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Service로 User 객체 전달
        MealRecordResponse response = mealRecordService.createMealRecordWithPhoto(request, photo, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("식사 기록 등록 성공", response));

    }

    // 2) 식사 기록 단건 조회 - Read (by ID) / GET 메서드
    @Operation(summary = "식사 기록 단건 조회 API",
            description = "식사 기록 ID를 이용해 단일 데이터를 조회할 수 있습니다. (존재하지 않는 ID일 경우 MEAL_NOT_FOUND 예외가 발생)")
    /*@GetMealRecordDocs*/
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MealRecordResponse>> getMealRecord(
            @Parameter(description = "식사 기록 ID", example = "1", required = true)
            @PathVariable Long id){

        MealRecordResponse response = mealRecordService.getMealRecord(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 3) 특정 날짜의 식사 기록 조회 - Read (by Date) / GET 메서드
    @Operation(summary = "특정 날짜의 식사 기록 조회 API",
            description = "사용자의 userId와 조회할 날짜(date)를 기반으로 해당 일자의 모든 식사 기록을 조회할 수 있습니다.")
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<MealRecordResponse>>> getMealRecordsByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails, // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음
            @Parameter(
                    description = "조회할 날짜 (yyyy-MM-dd)",
                    example = "2026-01-01",
                    required = true
            )
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam("date") LocalDate date
    ){
        // Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출

        // User 엔티티를 조회
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<MealRecordResponse> records = mealRecordService.getMealRecordsByDate(user, date);

        return ResponseEntity.ok(ApiResponse.success(records));
    }


    // 4) 특정 기간의 식사 기록 조회 - Read (by Period) / GET 메서드
    @Operation(summary = "특정 기간 식사 기록 조회 API",
            description = "사용자의 userId와 기간(start, end)을 입력받아 해당 범위 내 모든 식사 기록을 조회할 수 있습니다.")
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<MealRecordResponse>>> getMealRecordsByPeriod(
            @AuthenticationPrincipal CustomUserDetails userDetails, // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음
            @Parameter(
                    description = "조회할 시작 날짜 (yyyy-MM-dd)",
                    example = "2026-01-01",
                    required = true
            )
            @RequestParam("start") LocalDate start,
            @Parameter(
                    description = "조회할 끝 날짜 (yyyy-MM-dd)",
                    example = "2026-01-11",
                    required = true
            )
            @RequestParam("end") LocalDate end
    ){
        // Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출

        // User 엔티티를 조회
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<MealRecordResponse> records = mealRecordService.getMealRecordsByPeriod(user, start, end);

        return ResponseEntity.ok(ApiResponse.success(records));
    }


    // 5) 식사 기록 수정 - Update / PUT 메서드를 PATCH 메서드로 수정함
    @UpdateMealRecordDocs
    @Operation(summary = "식사 기록 수정 API",
            description = "식사 기록의 일부 항목을 수정할 수 있습니다. (PATCH: 변경할 필드만 전달)")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MealRecordResponse>> updateMealRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "식사 기록 ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @RequestBody MealRecordUpdateRequest request
    ){
        // PathVariable로 받은 id를 request(요청 객체의 id)에 덮어씀
        // request.setId(id);

        // 추가
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        MealRecordResponse updated = mealRecordService.updateMealRecord(user, id, request);

        return ResponseEntity.ok(ApiResponse.success("식사 기록 수정 성공", updated));
    }


    // 6) 식사 기록 삭제 - Delete / DELETE 메서드
    @Operation(summary = "식사 기록 삭제 API",
            description = "식사 기록 ID를 이용해 해당 데이터를 삭제함. 존재하지 않는 ID일 경우 MEAL_NOT_FOUND 예외가 발생함.")
    /*@DeleteMealRecordDocs*/
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMealRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "식사 기록 ID", example = "1", required = true)
            @PathVariable Long id
    ){
        // 추가
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        mealRecordService.deleteMealRecord(user, id);

        return ResponseEntity.ok(ApiResponse.success("식사 기록 삭제 성공"));
    }


}
