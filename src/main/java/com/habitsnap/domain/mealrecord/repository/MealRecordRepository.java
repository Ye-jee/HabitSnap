package com.habitsnap.domain.mealrecord.repository;

import com.habitsnap.domain.mealrecord.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    // 특정 유저의 특정 날짜 식사 기록 조회
    List<MealRecord> findByUserIdAndMealDate(Long userId, LocalDate mealDate);

    // 특정 유저의 기간별 식사 기록 조회
    List<MealRecord> findByUserIdAndMealDateBetween(Long userId, LocalDate start, LocalDate end);

}
