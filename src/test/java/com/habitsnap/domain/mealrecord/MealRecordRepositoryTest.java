package com.habitsnap.domain.mealrecord;

import com.habitsnap.domain.mealrecord.entity.MealRecord;
import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import com.habitsnap.domain.mealrecord.repository.MealRecordRepository;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Disabled
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)        // 테스트 종류 후에도 데이터가 DB에 남도록 설정
public class MealRecordRepositoryTest {

    @Autowired
    MealRecordRepository mealRecordRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void insertDummyAndSearchByPeriod() {

        /*Long userId = 1L;*/

        User user = userRepository.save(
                User.builder()
                        .email("test@habitsnap.com")
                        .password("test!1234!")
                        .nickname("테스트유저")
                        .build()
        );

        LocalDate startDate = LocalDate.of(2025,11,1);

        // 10일치 더미데이터 생성
        for(int i=0; i<10; i++){
            LocalDate date = startDate.plusDays(i);

            MealRecord record = MealRecord.builder()
                    .user(user)
                    .mealDate(date)
                    .mealTime(LocalTime.of(12,0))
                    .mealType(MealType.LUNCH)
                    .mealName("테스트 식사"+ (i+1))
                    .portion(Portion.ONE)
                    .fullnessLevel(3)
                    .build();

            mealRecordRepository.save(record);
        }

        // 기간 조회 테스트
        LocalDate from = LocalDate.of(2025,11,3);
        LocalDate to = LocalDate.of(2025,11,7);

        List<MealRecord> results = mealRecordRepository.findByUserAndMealDateBetween(user, from, to);

        // 콘솔 출력
        System.out.println("\n ========== 기간 조회 결과 ========== ");
        System.out.println("조회된 개수 >> "+ results.size());
        // results.forEach(System.out::println);
        results.forEach(r -> System.out.println(r));


        // 검증
        // 기대 : 11/3~11/7 -> 5개 반환
        assertThat(results.size()).isEqualTo(5);

    }



}
