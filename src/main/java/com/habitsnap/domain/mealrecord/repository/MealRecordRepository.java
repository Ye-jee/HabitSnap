package com.habitsnap.domain.mealrecord.repository;

import com.habitsnap.domain.mealrecord.entity.MealRecord;
import com.habitsnap.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    // 특정 유저의 특정 날짜 식사 기록 조회
    List<MealRecord> findByUserAndMealDate(User user, LocalDate mealDate);

    // 특정 유저의 기간별 식사 기록 조회
    List<MealRecord> findByUserAndMealDateBetween(User user, LocalDate start, LocalDate end);

    // N+1 쿼리 제거 (성능 최적화, 비교 테스트 전용 / 특정 유저)
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT m FROM MealRecord m WHERE m.user = :user AND m.mealDate BETWEEN :start AND :end")
    List<MealRecord> findAllWithUserByUserAndMealDateBetween(User user, LocalDate start, LocalDate end);

    // 전체 MealRecord 조회 / 모든 유저 식사기록 조회 (Lazy 로딩 방식, N+1 쿼리 발생)
    @Query("SELECT m FROM MealRecord m")
    List<MealRecord> findAllMealRecords();

    // 전체 MealRecord + User 함께 조회 (N+1 제거용)
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT m FROM MealRecord m")
    List<MealRecord> findAllWithUser();


}
