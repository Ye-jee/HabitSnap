package com.habitsnap.api.mealrecord;

import com.habitsnap.application.mealrecord.MealRecordService;
import com.habitsnap.config.CustomUserDetails;
import com.habitsnap.dto.mealrecord.MealRecordCreateRequest;
import com.habitsnap.dto.mealrecord.MealRecordResponse;
import com.habitsnap.dto.mealrecord.MealRecordUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class MealRecordController {

    private final MealRecordService mealRecordService;

    // 1) 식사 기록 등록 - Create / POST 메서드
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MealRecordResponse createMealRecord(
            @Valid @RequestPart("data") MealRecordCreateRequest request,            // JSON 데이터
            @RequestPart(value = "photo", required = false) MultipartFile photo,    // 사진 파일
            @AuthenticationPrincipal CustomUserDetails userDetails){
            // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음

        Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출
        // return mealRecordService.createMealRecord(request, userId);
        return mealRecordService.createMealRecordWithPhoto(request, photo, userId);

    }

    // 2) 식사 기록 단건 조회 - Read (by ID) / GET 메서드
    @GetMapping("/{id}")
    public MealRecordResponse getMealRecord(@PathVariable Long id){

        return mealRecordService.getMealRecord(id);
    }


    // 3) 특정 날짜의 식사 기록 조회 - Read (by Date) / GET 메서드
    @GetMapping("/date")
    public List<MealRecordResponse> getMealRecordsByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails, // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음
            @RequestParam("value") LocalDate date
    ){
        Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출
        return mealRecordService.getMealRecordsByDate(userId, date);
    }


    // 4) 특정 기간의 식사 기록 조회 - Read (by Period) / GET 메서드
    @GetMapping("/range")
    public List<MealRecordResponse> getMealRecordsByPeriod(
            @AuthenticationPrincipal CustomUserDetails userDetails, // userId를 직접 받는 대신 토큰을 이용해서 사용자 정보를 받음
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end
    ){
        Long userId = userDetails.getUserId();      // JWT 토큰에서 로그인한 사용자 ID 추출
        return mealRecordService.getMealRecordsByPeriod(userId, start, end);
    }


    // 5) 식사 기록 수정 - Update / PUT 메서드를 PATCH 메서드로 수정함
    @PatchMapping("/{id}")
    public MealRecordResponse updateMealRecord(
            @PathVariable Long id,
            /*@Valid*/ @RequestBody MealRecordUpdateRequest request
    ){
        // 요청 객체의 id를 path variable로 덮어씀
        request.setId(id);

        return mealRecordService.updateMealRecord(request);
    }


    // 6) 식사 기록 삭제 - Delete / DELETE 메서드
    @DeleteMapping("/{id}")
    public void deleteMealRecord(@PathVariable Long id){
        mealRecordService.deleteMealRecord(id);
    }


}
