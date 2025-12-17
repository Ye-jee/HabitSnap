package com.habitsnap.domain.mealrecord;

import com.habitsnap.domain.mealrecord.entity.MealRecord;
import com.habitsnap.domain.mealrecord.repository.MealRecordRepository;
import com.habitsnap.domain.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.util.List;

// N+1 쿼리 제거 전후 비교 테스트 코드
@DataJpaTest        // Repository + DB 레이어만 로드해서 JPA 성능을 직접 측정
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 추가
@Transactional
@Rollback(false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)   // 테스트 순서 지정
public class MealRecordRepositoryPerformanceTest {

    @Autowired
    private MealRecordRepository mealRecordRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Statistics getHibernateStatistics(){
        Session session = entityManager.unwrap(Session.class);
        session.getSessionFactory().getStatistics().setStatisticsEnabled(true);
        return session.getSessionFactory().getStatistics();
    }

    /*private User user;

    @BeforeEach
    void setup() {
        // 실제 DB의 User 엔티티를 참조
        user = entityManager.find(User.class, 1L);
    }*/

    @Test
    @Order(1)
    @DisplayName("1) Lazy 로딩 상태(N+1 쿼리 발생)")
    @Commit
    void testLazyLoading_NPlus1(){
        /*LocalDate start = LocalDate.of(2025,12,1);
        LocalDate end = LocalDate.of(2025,12,5);*/

        Statistics stats = getHibernateStatistics();
        stats.clear();

        long startTime = System.currentTimeMillis();

        // Lazy 로딩 상태 : MealRecord만 조회하고, getUser() 접근 시 개별 쿼리 발생
        List<MealRecord> records = mealRecordRepository.findAllMealRecords();

        // 각 MealRecord에서 user 정보 접근 (N+1 유발)
        for (MealRecord record : records) {
            record.getUser().getEmail();    // Lazy 로딩으로 매번 select 발생
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\n======================================");
        System.out.println("[Lazy 로딩 테스트]");
        System.out.println("총 MealRecord 개수: " + records.size());
        System.out.println("실행 시간(ms): " + (endTime - startTime));
        System.out.println("총 실행된 쿼리수: " + stats.getPrepareStatementCount());
        System.out.println("======================================\n");
    }

    @Test
    @Order(2)
    @DisplayName("2) @EntityGraph 적용 후 (N+1 제거)")
    @Commit
    void testEntityGraph_NoNPlus1(){
        Statistics stats = getHibernateStatistics();
        stats.clear();

        /*LocalDate start = LocalDate.of(2025,12,1);
        LocalDate end = LocalDate.of(2025,12,5);*/

        long startTime = System.currentTimeMillis();

        // EntityGraph: JOIN FETCH로 User까지 함께 조회 (N+1 제거)
        List<MealRecord> records = mealRecordRepository.findAllWithUser();
        for (MealRecord record : records) {
            record.getUser().getEmail();    // 이미 JOIN으로 불러옴 -> 추가 쿼리 없음
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\n======================================");
        System.out.println("[EntityGraph 테스트]");
        System.out.println("총 MealRecord 개수: " + records.size());
        System.out.println("실행 시간(ms): " + (endTime - startTime));
        System.out.println("총 실행된 쿼리수: " + stats.getPrepareStatementCount());
        System.out.println("======================================\n");
    }



}
