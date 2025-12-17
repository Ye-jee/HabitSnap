package com.habitsnap.application.mealrecord;

import com.habitsnap.domain.mealrecord.entity.MealRecord;
import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import com.habitsnap.domain.mealrecord.repository.MealRecordRepository;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import com.habitsnap.dto.mealrecord.MealRecordCreateRequest;
import com.habitsnap.dto.mealrecord.MealRecordResponse;
import com.habitsnap.dto.mealrecord.MealRecordUpdateRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/*import static org.assertj.core.api.AssertionsForClassTypes.assertThat;*/
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class MealRecordServiceTest {

    @Autowired
    private MealRecordService mealRecordService;

    @Autowired
    private MealRecordRepository mealRecordRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userRepository.save(
                User.builder()
                        .email("test@habitsnap.com")
                        .password("test!1234!")
                        .nickname("테스트유저")
                        .build()
        );

        // 테스트용 User 엔티티 생성

        // 실제 DB에 저장

    }

    // ------------------- Create(기록 생성) 이자 EXIF 누락 시 대체 로직 -----------------------
    @Test
    @DisplayName("식사 기록 생성 성공 및 EXIF 누락시 업로드 날짜와 시각을 현재 날짜와 시각으로 대체 매핑")
    void createMealRecord_success(){
        //given - 요청 DTO 생성
        MealRecordCreateRequest request = new MealRecordCreateRequest(
            MealType.BREAKFAST,
            "아보카도 닭가슴살 볶음밥",
            Portion.ONE,
            4,
            "잡곡밥",
            "닭가슴살",
            "아보카도",
            "든든하게 탄단지 잘 지켜서 먹음!"
        );

        // when - 서비스 호출, 호출 시 내부 로직에서 현재 날짜 및 시각으로 자동 대체
        MealRecordResponse response = mealRecordService.createMealRecordWithPhoto(request, null, testUser);

        // then1 - 반환값과 DB 저장 상태를 검증
        assertThat(response.getMealType()).isEqualTo(MealType.BREAKFAST.name());
        assertThat(response.getMealName()).isEqualTo("아보카도 닭가슴살 볶음밥");
        assertThat(response.getPortion()).isEqualTo(Portion.ONE.name());
        assertThat(response.getFullnessLevel()).isEqualTo(4);
        assertThat(response.getCarb()).isEqualTo("잡곡밥");
        assertThat(response.getProtein()).isEqualTo("닭가슴살");
        assertThat(response.getFat()).isEqualTo("아보카도");
        assertThat(response.getNotes()).isEqualTo("든든하게 탄단지 잘 지켜서 먹음!");

        // then2 - 반환된 DTO에 현재 날짜이 세팅되고, 시각은 비어있지 않은지 검증
        assertThat(response.getMealDate()).isEqualTo(LocalDate.now());
        assertThat(response.getMealDate()).isNotNull();
    }


    // ------------------- Read 1: 단건 조회 -------------------
    @Test
    @DisplayName("단건 조회 성공 : 특정 ID의 식사기록을 정확히 조회할 수 있는지 확인")
    void getMealRecord_success() {

        //given - DB에 직접 MealRecord 저장
        MealRecord saved_mealrecord = mealRecordRepository.save(
            MealRecord.builder()
                    .user(testUser)
                    .mealDate(LocalDate.now())
                    .mealTime(LocalTime.of(8,30))
                    .mealType(MealType.BREAKFAST)
                    .mealName("오버나이트 오트밀에 블루베리랑 견과류")
                    .portion(Portion.HALF)
                    .fullnessLevel(3)
                    .carb("오트밀")
                    .protein("그릭요거트")
                    .fat("아몬드")
                    .photoUrl(null)
                    .notes("적당한 클린식")
                    .build()
        );

        // when - getMealRecord() 호출로 ID 기반 조회
        MealRecordResponse response = mealRecordService.getMealRecord(saved_mealrecord.getId());

        // then - 응답데이터가 저장된 값과 동일한지 검증
        assertThat(response.getMealType()).isEqualTo(MealType.BREAKFAST.name());
        assertThat(response.getMealName()).isEqualTo("오버나이트 오트밀에 블루베리랑 견과류");
        assertThat(response.getProtein()).isEqualTo("그릭요거트");
    }


    // ------------------- Read 2: 날짜별 조회 -------------------
    @Test
    @DisplayName("날짜별 조회 성공 : 특정 날짜의 식사기록이 올바르게 필터링되는지 확인")
    void getMealRecordByDate_success() {
        // given - targetDate(2025-11-26)에 해당하는 기록을 DB에 저장
        LocalDate targetDate = LocalDate.of(2025, 11, 26);
        mealRecordRepository.save(
                MealRecord.builder()
                        .user(testUser)
                        .mealDate(targetDate)
                        .mealTime(LocalTime.of(18,30))
                        .mealType(MealType.DINNER)
                        .mealName("훈제연어 아보카도 포케")
                        .portion(Portion.ONE)
                        .fullnessLevel(4)
                        .carb("현미밥")
                        .protein("훈제연어")
                        .fat("아보카도")
                        .photoUrl(null)
                        .notes("연어랑 아보카도의 조합이 아주 좋았다!")
                        .build()
        );

        // when - getMealRecordsByDate()로 해당 날짜 조회
        List<MealRecordResponse> results = mealRecordService.getMealRecordsByDate(testUser, targetDate);

        // then - 정확히 1건 반환 및 데이터 필드 일치 검증
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getProtein()).isEqualTo("훈제연어");
    }


    // ------------------- Read 3: 기간별 조회 -------------------
    @Test
    @DisplayName("기간(범위)별 조회 성공 : 주간·기간 단위로 기록을 조회할 수 있는지 검증")
    void getMealRecordByPeriod_success() {
        LocalDate start = LocalDate.of(2025,11,24);
        LocalDate end = LocalDate.of(2025,11,26);

        // given - start~end 날짜 범위 내 3일치 기록 저장
        mealRecordRepository.save(
                MealRecord.builder()
                        .user(testUser)
                        .mealDate(LocalDate.of(2025,11,24))
                        .mealTime(LocalTime.of(8,30))
                        .mealType(MealType.BREAKFAST)
                        .mealName("햄치즈 계란 토스트")
                        .portion(Portion.HALF)
                        .fullnessLevel(2)
                        .carb("잡곡식빵")
                        .protein("계란")
                        .fat("치즈")
                        .photoUrl(null)
                        .notes("월요일 아침")
                        .build()
        );

        mealRecordRepository.save(
                MealRecord.builder()
                        .user(testUser)
                        .mealDate(LocalDate.of(2025,11,25))
                        .mealTime(LocalTime.of(12,30))
                        .mealType(MealType.LUNCH)
                        .mealName("닭가슴살 양배추 볶음밥")
                        .portion(Portion.ONE)
                        .fullnessLevel(4)
                        .carb("잡곡밥")
                        .protein("닭가슴살")
                        .fat("올리브유")
                        .photoUrl(null)
                        .notes("화요일 점심")
                        .build()
        );

        mealRecordRepository.save(
                MealRecord.builder()
                        .user(testUser)
                        .mealDate(LocalDate.of(2025,11,26))
                        .mealTime(LocalTime.of(19,30))
                        .mealType(MealType.DINNER)
                        .mealName("간장계란밥")
                        .portion(Portion.ONE)
                        .fullnessLevel(3)
                        .carb("잡곡밥")
                        .protein("계란")
                        .fat("참기름")
                        /*.photoUrl(null)*/
                        .notes("수요일 저녁")
                        .build()
        );

        // when - getMealRecordsByPeriod() 호출
        List<MealRecordResponse> results = mealRecordService.getMealRecordsByPeriod(testUser, start, end);

        // then - 3개의 기록이 모두 조회되고, 각 레코드 값들이 예상 값과 일치하는지 검증
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getPortion()).isEqualTo(Portion.HALF.name());
        assertThat(results.get(1).getNotes()).isEqualTo("화요일 점심");
        assertThat(results.get(2).getMealType()).isEqualTo(MealType.DINNER.name());
    }

    // ------------------- Update (식사기록 부분 수정) -----------------------
    @Test
    // @DisplayName("식사 기록 수정 성공 : 사용자가 기록을 수정했을 때 변경 내용이 DB에 반영되는지 테스트")
    @DisplayName("식사 기록 부분 수정 성공 : null 아닌 필드만 반영")
    void updateMealRecord_partialUpdate_success() {
        // given - 기존 데이터 저장 후 수정 요청 DTO 준비
        MealRecord saved_mealrecord = mealRecordRepository.save(
                MealRecord.builder()
                        .user(testUser)
                        .mealDate(LocalDate.now())
                        .mealTime(LocalTime.of(8,30))
                        .mealType(MealType.BREAKFAST)
                        .mealName("오버나이트 오트밀에 블루베리랑 견과류")
                        .portion(Portion.HALF)
                        .fullnessLevel(3)
                        .carb("오트밀")
                        .protein("그릭요거트")
                        .fat("아몬드")
                        .photoUrl(null)
                        .notes("수정 전 식단")
                        .build()
        );

        // when1 - 수정 요청 DTO 생성
        MealRecordUpdateRequest updateRequest = new MealRecordUpdateRequest();
        updateRequest.setId(saved_mealrecord.getId());
        updateRequest.setNotes("수정 후 식단");              // 해당 필드만 수정

        // when2 - updateMealRecord() 호출
        MealRecordResponse response = mealRecordService.updateMealRecord(updateRequest);

        // then - 반환된 응답의 필드가 수정 요청값과 일치하는지 확인
        assertThat(response.getNotes()).isEqualTo("수정 후 식단");               // 변경됨
        assertThat(response.getCarb()).isEqualTo("오트밀");                     // 그대로
        assertThat(response.getProtein()).isEqualTo("그릭요거트");               // 그대로
        assertThat(response.getMealType()).isEqualTo(MealType.BREAKFAST.name());        // 그대로
    }


    // ------------------- Delete (식사기록 삭제) -----------------------
    @Test
    @DisplayName("식사 기록 삭제 성공 : 삭제 요청 시 DB에서 실제로 데이터가 제거되는지 확인")
    void deleteMealRecord_success() {
        // given - DB에 1개의 식사기록 저장
        MealRecord saved_mealrecord = mealRecordRepository.save(
                MealRecord.builder()
                        .user(testUser)
                        .mealDate(LocalDate.now())
                        .mealTime(LocalTime.of(20,30))
                        .mealType(MealType.DINNER)
                        .mealName("소고기 구이")
                        .portion(Portion.TWO)
                        .fullnessLevel(5)
                        .carb("잡곡밥")
                        .protein("소고기")
                        .fat("참기름")
                        .photoUrl(null)
                        .notes("삭제 테스트용")
                        .build()
        );

        // when - deleteMealRecord() 호출
        mealRecordService.deleteMealRecord(saved_mealrecord.getId());

        // then - 해당 ID로 조회 시, Optional.empty()로 삭제되었는지 확인
        assertThat(mealRecordRepository.findById(saved_mealrecord.getId())).isEmpty();
    }


    // ------------------- Exception (존재하지 않는 ID 조회 시 커스템 예외 MEAL_NOT_FOUND) -----------------------
    @Test
    @DisplayName("존재하지 않는 ID 조회 시 예외 발생 : 커스텀 예외 MEAL_NOT_FOUND가 실제로 발생되는지 확인")
    void getMealRecord_notFound() {
        // given - (생략) 존재하지 않는 ID
        // when - getMealRecord(9999L) 호출
        // then - CustomException이 발생하고 메시지에 MEAL_NOT_FOUND 포함됨
        assertThatThrownBy(()-> mealRecordService.getMealRecord(9999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("식사 기록을 찾을 수 없습니다.");
    }




}
