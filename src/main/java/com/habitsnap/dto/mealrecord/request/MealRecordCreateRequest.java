package com.habitsnap.dto.mealrecord;

import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.annotations.NotNull;

/* 식사 기록(MealRecord) 생성 요청 DTO
* - 사용자가 새로운 식사 기록을 생성할 때 전달하는 데이터
* */
@Getter
@Setter
@Schema(name = "MealRecordCreateRequest", description = "식사 기록 등록 요청 DTO")
@NoArgsConstructor      // 테스트를 위해 추가
@AllArgsConstructor
public class MealRecordCreateRequest {

    @NotNull
    @Schema(description = "식사 종류, 선택지: BREAKFAST, LUNCH, DINNER, SNACK", example = "BREAKFAST")
    private MealType mealType;          // enum 적용 - 식사 종류

    @NotNull
    @Schema(description = "식사 이름 (예: 아보카도 닭가슴살 볶음밥)", example = "아보카도 닭가슴살 볶음밥")
    private String mealName;

    @Schema(description = "먹는 양, 선택지: SMALL, HALF, ONE, TWO", example = "ONE")
    private Portion portion;            // enum 적용 - 먹는 양

    @Schema(description = "식사 후 포만감 (1~5 사이 정수)", example = "3")
    private Integer fullnessLevel;      // 슷자 (1~5)

    // 사용자가 먹은 메뉴 중 탄수화물/단백질/지방에 해당하는 음식을 직접 작성
    @Schema(description = "식사 메뉴 중 탄수화물에 해당하는 음식", example = "잡곡밥")
    private String carb;

    @Schema(description = "식사 메뉴 중 단백질에 해당하는 음식", example = "닭가슴살")
    private String protein;

    @Schema(description = "식사 메뉴 중 지방에 해당하는 음식", example = "아보카도")
    private String fat;

    @Schema(description = "식사 메뉴에 대한 짧은 기록", example = "간단하지만 든든한 식사였음")
    private String notes;               // 식사 메뉴에 대한 짧은 기록

}
