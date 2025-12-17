package com.habitsnap.application.mealrecord;

import com.habitsnap.domain.mealrecord.repository.MealRecordRepository;
import com.habitsnap.domain.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.Commit;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest     // 전체 컨텍스트 로드 (Service + Cache + Repository)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Commit
public class MealRecordCachePerformanceTest {

    @Autowired
    private MealRecordService mealRecordService;

    @Autowired
    private MealRecordRepository mealRecordRepository;

    @Autowired
    private CacheManager cacheManager;

    private static final Long TEST_USER_ID = 1L;
    private User user;

    @BeforeEach
    void setup(@Autowired EntityManager entityManager) {
        user = entityManager.find(User.class, TEST_USER_ID);
    }

    private void measureExecutionTime(String label, Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();

        System.out.println("["+ label + "] 실행시간: "+ (end-start) + "ms");
    }

    @Test
    @Order(1)
    @DisplayName("1) 캐시 미적용 - 첫 조회 (DB 쿼리 발생)")
    void testWithoutCache(){
        LocalDate start = LocalDate.of(2025,12,1);
        LocalDate end = LocalDate.of(2025,12,31);

        measureExecutionTime("첫번째 조회(DB 조회)", ()-> {
            List<?> records = mealRecordService.getMealRecordsByPeriod(user, start, end);
            System.out.println("조회 결과 개수: " + records.size());
        });

    }

    @Test
    @Order(2)
    @DisplayName("2) 캐시 미적용 - 동일 조건 재조회 (캐시 적중)")
    void testWithCacheHit(){
        LocalDate start = LocalDate.of(2025,12,1);
        LocalDate end = LocalDate.of(2025,12,31);

        measureExecutionTime("두번째 조회(캐시 HIT)", ()-> {
            List<?> records = mealRecordService.getMealRecordsByPeriod(user, start, end);
            System.out.println("조회 결과 개수: " + records.size());
        });
    }

    @Test
    @Order(3)
    @DisplayName("3) 캐시 초기화 후 재조회 (DB 재접근)")
    void testCacheEvict(){
        // 캐시 초기화
        cacheManager.getCache("mealRecords").clear();

        LocalDate start = LocalDate.of(2025,12,1);
        LocalDate end = LocalDate.of(2025,12,31);

        measureExecutionTime("캐시 삭제 후 재조회(DB 조회)", ()-> {
            List<?> records = mealRecordService.getMealRecordsByPeriod(user, start, end);
            System.out.println("조회 결과 개수: " + records.size());
        });

    }


}
