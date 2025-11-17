package com.habitsnap.domain.mealrecord.entity;

import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "meal_record", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, meal_date")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                // 사용자 id

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;          // 식사 구분(아침, 점심, 저녁, 간식)

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;         // 먹는 날짜

    @Column(name = "meal_time", nullable = false)
    private LocalTime mealTime;         // 먹는 시간

    @Column(name = "meal_name", nullable = false, length = 255)
    private String mealName;            // 메뉴명

    @Enumerated(EnumType.STRING)
    @Column(name = "portion", length = 20)
    private Portion portion;            // 먹는 양

    @Column(name = "fullness_level")
    private Integer fullnessLevel;      // 배부름 정도 (1~5)

    @Column(name = "carb", length = 100)
    private String carb;                // 먹었던 메뉴 중 탄수화물에 해당하는 움식

    @Column(name = "protein", length = 100)
    private String protein;             // 먹었던 메뉴 중 단백질에 해당하는 움식

    @Column(name = "fat", length = 100)
    private String fat;                 // 먹었던 메뉴 중 지방에 해당하는 움식

    @Column(name = "notes", length = 255)
    private String notes;               // 식사 메뉴에 대한 짧은 기록

    @Column(name = "photo_url", length = 255)
    private String photoUrl;            // 식사 메뉴 사진

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public MealRecord(Long userId,
                      MealType mealType,
                      LocalDate mealDate,
                      LocalTime mealTime,
                      String mealName,
                      Portion portion,
                      Integer fullnessLevel,
                      String carb,
                      String protein,
                      String fat,
                      String notes,
                      String photoUrl){

        this.userId = userId;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.mealTime = mealTime;
        this.mealName = mealName;
        this.portion = portion;
        this.fullnessLevel = fullnessLevel;
        this.carb = carb;
        this.protein = protein;
        this.fat = fat;
        this.notes = notes;
        this.photoUrl = photoUrl;
    }


}
