package com.habitsnap.dto.mealrecord;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/*
 * 식사 기록(MealRecord) 응답 DTO
 * - 조회, 생성, 수정 후 클라이언트에 반환되는 데이터
 */
@Getter
@Builder
public class MealRecordResponse {

    private Long id;                // 식사 기록 ID
    private Long userId;            // 작성자 ID

    private String mealType;
    private String mealName;

    private String portion;
    private Integer fullnessLevel;

    private String carb;
    private String protein;
    private String fat;

    private String notes;
    private String photoUrl;

    private LocalDate mealDate;
    private LocalTime mealTime;

    private LocalDateTime createAt;     // 작성 시각
}
