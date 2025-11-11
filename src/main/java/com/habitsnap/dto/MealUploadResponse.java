package com.habitsnap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/* 파일 업로드 후 반환되는 응답 DTO
 - 업로드된 파일명
 - EXIF 또는 업로드 시각 기준 날짜/시간
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealUploadResponse {

    private String fileName;        // 저장된 파일명
    private LocalDate mealDate;     // 촬영일자 (또는 업로드 일자)
    private LocalTime mealTime;     // 촬영일자 (또는 업로드 시각)

}
