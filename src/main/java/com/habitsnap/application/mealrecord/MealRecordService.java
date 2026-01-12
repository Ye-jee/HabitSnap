package com.habitsnap.application.mealrecord;

import com.habitsnap.application.photo.PhotoUploadService;
import com.habitsnap.domain.mealrecord.entity.MealRecord;
import com.habitsnap.domain.mealrecord.repository.MealRecordRepository;
import com.habitsnap.domain.user.User;
import com.habitsnap.dto.MealUploadResponse;
import com.habitsnap.dto.mealrecord.MealRecordCreateRequest;
import com.habitsnap.dto.mealrecord.MealRecordResponse;
import com.habitsnap.dto.mealrecord.MealRecordUpdateRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/* MealRecordService
* - 식사 기록 생성, 수정, 조회, 삭제 로직 담당
* - JWT 인증 기반으로 로그인한 사용자의 userId를 받아 처리
* */
@Service
@RequiredArgsConstructor
@Transactional
public class MealRecordService {

    private final MealRecordRepository mealRecordRepository;

    private final PhotoUploadService photoUploadService;

    /* 식사 기록 생성 - Create */
    @CacheEvict(value = "mealRecords", key = "#user.id")        // 식사기록 생성 시, 특정 사용자(user) 단위로 캐시 무효화 (최신 데이터 반영을 위해)
    public MealRecordResponse createMealRecord(MealRecordCreateRequest request, User user){
        // EXIF 추출 로직 연동 전까지 임시 기본값 처리
        LocalDate mealDate = LocalDate.now();
        LocalTime mealTime = LocalTime.now();

        // 나중에 EXIF 메타데이터 추출할 때 교체할 코드
        /*LocalDate mealDate = exifMetadata.getDate() != null
                ? exifMetadata.getDate() : LocalDate.now();

        LocalTime mealTime = exifMetadata.getTime() != null
                ? exifMetadata.getTime() : LocalTime.now();*/


        MealRecord record = MealRecord.builder()
                /*.userId(userId)*/                     // 로그인 사용자 ID 주입 (컨트롤러쪽에서)
                .user(user)
                .mealType(request.getMealType())
                .mealName(request.getMealName())
                .portion(request.getPortion())
                .fullnessLevel(request.getFullnessLevel())
                .carb(request.getCarb())
                .protein(request.getProtein())
                .fat(request.getFat())
                .notes(request.getNotes())
                .mealDate(mealDate)                 // 자동으로 현재 날짜
                .mealTime(mealTime)                 // 자동으로 현재 시간
                .createdAt(LocalDateTime.now())     // 작성 시간 자동 기록
                .build();

        mealRecordRepository.save(record);
        return toResponse(record);
    }


    /* 식사 기록 생성, 사진파일과 함께 - Create2 */
    @CacheEvict(value = "mealRecords", key = "#user.id")        // 식사기록 생성 시, 특정 사용자(user) 단위로 캐시 무효화 (최신 데이터 반영을 위해)
    public MealRecordResponse createMealRecordWithPhoto(MealRecordCreateRequest request, MultipartFile photo, User user) {

        LocalDate mealDate = LocalDate.now();
        LocalTime mealTime = LocalTime.now();
        String photoUrl = null;

        // 1) 파일이 존재하면 업로드 + EXIF 추출
        if(photo != null && !photo.isEmpty()){
            MealUploadResponse uploadResponse = photoUploadService.uploadAndExtract(photo);
            mealDate = uploadResponse.getMealDate();
            mealTime = uploadResponse.getMealTime();
            photoUrl = uploadResponse.getImageUrl();
        }

        // 2) DB에 저장
        MealRecord record = MealRecord.builder()
                .user(user)                             // user 객체 주입 (컨트롤러쪽에서)
                .mealType(request.getMealType())
                .mealName(request.getMealName())
                .portion(request.getPortion())
                .fullnessLevel(request.getFullnessLevel())
                .carb(request.getCarb())
                .protein(request.getProtein())
                .fat(request.getFat())
                .notes(request.getNotes())
                .photoUrl(photoUrl)
                .mealDate(mealDate)                 // 자동으로 사진파일에 있는 EXIF 추출해 해당 날짜 저장
                .mealTime(mealTime)                 // 자동으로 사진파일에 있는 EXIF 추출해 해당 날짜 저장
                .createdAt(LocalDateTime.now())     // 작성 시간 자동 기록
                .build();

        mealRecordRepository.save(record);
        return toResponse(record);

    }


    /* 식사 기록 수정 - Update */
    @CacheEvict(value = "mealRecords", key = "#user.id")        // 식사기록 수정 시, 특정 사용자(user) 단위로 캐시 무효화 (최신 데이터 반영을 위해)
    public MealRecordResponse updateMealRecord(User user, MealRecordUpdateRequest request) {    // User user는 캐시 키용으로만 필요, 지금 로직에서는 user 사용 안 함

        MealRecord record = mealRecordRepository.findById(request.getId())
                .orElseThrow(()-> new CustomException(ErrorCode.MEAL_NOT_FOUND));

        // 요청에서 mealType이 null이 아니면, 요청한 mealType으로 값 변경 즉, null이 아닌 필드만 업데이트
        // Enum 타입은 null 먼저 체크
        if(request.getMealType() != null && isValid(request.getMealType().toString())){
            record.setMealType(request.getMealType());
        }
        if(request.getPortion() != null && isValid(request.getPortion().toString())) {
            record.setPortion(request.getPortion());
        }

        if(request.getMealTime() != null) record.setMealTime(request.getMealTime());

        if(request.getFullnessLevel() != null) record.setFullnessLevel(request.getFullnessLevel());

        if(isValid(request.getMealName())) record.setMealName(request.getMealName());

        if(isValid(request.getCarb())) record.setCarb(request.getCarb());
        if(isValid(request.getProtein())) record.setProtein(request.getProtein());
        if(isValid(request.getFat())) record.setFat(request.getFat());

        if(isValid(request.getNotes())) record.setNotes(request.getNotes());

        return toResponse(record);
    }

    // 기본값("string") 필터링 메서드
    private boolean isValid(String value) {
        return value != null && !value.equalsIgnoreCase("string") && !value.trim().isEmpty();
    }


    /* 단일 '식사 기록' 조회 (사용자 ID 기반) - Read1 */
    @Transactional(readOnly = true)
    public MealRecordResponse getMealRecord(Long id){
        MealRecord record = mealRecordRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.MEAL_NOT_FOUND));

        return toResponse(record);
    }


    /* 특정 날짜 '식사 기록' 조회 - Read2
    * 예) 오늘 먹은 것 보기
    * */
    @Transactional(readOnly = true)
    public List<MealRecordResponse> getMealRecordsByDate(User user, LocalDate date){
        return mealRecordRepository.findByUserAndMealDate(user, date)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* 기간(범위) 전체 '식사 기록' 조회 - Read3
    * 예) 이번 주 식단 리포트 조회
    * */
    @Cacheable(value = "mealRecords", key = "#user.id + ':' + #start + ':' + #end")
    @Transactional(readOnly = true)
    public List<MealRecordResponse> getMealRecordsByPeriod(User user, LocalDate start, LocalDate end){
        return mealRecordRepository.findByUserAndMealDateBetween(user, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    /* 식사 기록 삭제 - Delete */
    @CacheEvict(value = "mealRecords", key = "#user.id")        // 식사기록 삭제 시, 특정 사용자(user) 단위로 캐시 무효화 (최신 데이터 반영을 위해)
    public void deleteMealRecord(User user, Long id){           // User user는 캐시 키용으로만 필요, 지금 로직에서는 user 사용 안 함
        MealRecord record = mealRecordRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.MEAL_NOT_FOUND));

        mealRecordRepository.delete(record);
    }


    // Entity -> Response DTO  변환 메서드
    private MealRecordResponse toResponse(MealRecord record){
        return MealRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUser().getId())           // 이 부분에서 N+1 쿼리 발생
                .mealType(record.getMealType() != null ? record.getMealType().name() : null)
                .mealName(record.getMealName())
                .portion(record.getPortion() != null ? record.getPortion().name() : null)
                .fullnessLevel(record.getFullnessLevel())
                .carb(record.getCarb())
                .protein(record.getProtein())
                .fat(record.getFat())
                .notes(record.getNotes())
                .photoUrl(record.getPhotoUrl())
                .mealDate(record.getMealDate())
                .mealTime(record.getMealTime())
                .createdAt(record.getCreatedAt())
                .build();
    }



}
