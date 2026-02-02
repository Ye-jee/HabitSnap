package com.habitsnap.dto.mealrecord.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.annotations.NotNull;

import java.time.LocalTime;

/* 식사 기록(MealRecord) 수정 요청 DTO
* - create와 다르게 mealTime 수정 가능
* - 식사 데이터 전체를 수정할 수 있음
* */
@Getter
@Setter
@Schema(name = "MealRecordUpdateRequest", description = "식사 기록 수정 요청 DTO")
@NoArgsConstructor      // 테스트를 위해 추가
@AllArgsConstructor
public class MealRecordUpdateRequest {
    // MealRecordUpdateRequest에서는 수정할 필드만 null이 아니고, null이 아니면 반영되도록 하기 위해서 기본값을 제거함

    // id 제거: PathVariable로 받으므로 body에 있을 필요 없음
    /*@NotNull
    private Long id;*/                // 수정할 식사 기록 ID (필수)

    @Schema(description = "식사 종류", examples = "LUNCH", nullable = true)
    private MealType mealType;      // enum 적용 - 식사 종류

    @Schema(description = "메뉴명", examples = "연어 덮밥")
    private String mealName;

    @Schema(description = "식사 시간 (HH:mm:ss 형식)", examples = "12:30:00", nullable = true)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime mealTime;     // 식사 시간 (수정 가능)

    @Schema(description = "먹는 양", examples = "ONE", nullable = true)
    private Portion portion;        // enum 적용 - 먹는 양

    @Schema(description = "포만감(1~5)", examples = "4", nullable = true)
    private Integer fullnessLevel;  // 숫자(1~5)

    // 사용자가 먹은 메뉴 중 탄수화물/단백질/지방에 해당하는 음식을 직접 작성
    @Schema(description = "먹은 메뉴 중 탄수화물에 해당하는 음식", examples = "잡곡밥", nullable = true)
    private String carb;

    @Schema(description = "먹은 메뉴 중 단백질에 해당하는 음식", examples = "연어", nullable = true)
    private String protein;

    @Schema(description = "먹은 메뉴 중 지방에 해당하는 음식", examples = "연어", nullable = true)
    private String fat;

    @Schema(description = "식사 메뉴에 대한 짧은 기록", examples = "연어가 신선해서 더 맛있었음", nullable = true)
    private String notes;               // 식사 메뉴에 대한 짧은 기록
}
