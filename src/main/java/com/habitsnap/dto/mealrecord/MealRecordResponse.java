package com.habitsnap.dto.mealrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/*
 * 식사 기록(MealRecord) 응답 DTO
 * - 조회, 생성, 수정 후 클라이언트에 반환되는 데이터
 */

@Schema(description = "식사 기록 응답 DTO")
@Getter
@Builder
public class MealRecordResponse {

    @Schema(description = "식사 기록 ID", example = "1")
    private Long id;                // 식사 기록 ID

    @Schema(description = "작성자 ID", example = "10")
    private Long userId;            // 작성자 ID

    @Schema(description = "식사 종류 (예: BREAKFAST, LUNCH, DINNER, SNACK)", example = "BREAKFAST")
    private String mealType;

    @Schema(description = "식사 이름", example = "닭가슴살 아보카도 볶음밥")
    private String mealName;

    @Schema(description = "식사량 (예: SMALL, HALF, ONE, TWO)", example = "ONE")
    private String portion;

    @Schema(description = "포만감 지수 (1~5)", example = "3")
    private Integer fullnessLevel;

    @Schema(description = "식사에서 탄수화물에 해당하는 음식", example = "잡곡밥")
    private String carb;

    @Schema(description = "식사에서 단백질에 해당하는 음식", example = "닭가슴살")
    private String protein;

    @Schema(description = "식사에서 지방에 해당하는 음식", example = "아보카도")
    private String fat;

    @Schema(description = "식사에 대한 짧은 메모 및 후기", example = "간단하지만 든든한 식사였음")
    private String notes;

    @Schema(description = "사진 URL", example = "https://habitsnap.s3.ap-northeast-2.amazonaws.com/photos/meal123.jpg")
    private String photoUrl;

    @Schema(description = "식사 날짜 (yyyy-MM-dd)", example = "2025-12-04")
    private LocalDate mealDate;

    @Schema(description = "식사 시각 (HH:mm:ss)", example = "08:30:00")
    private LocalTime mealTime;

    @Schema(description = "작성 시각", example = "2025-12-04T08:32:15.122")
    private LocalDateTime createdAt;
}
