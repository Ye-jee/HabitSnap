package com.habitsnap.api.mealrecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitsnap.application.mealrecord.MealRecordService;
import com.habitsnap.config.CustomUserDetails;
import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import com.habitsnap.domain.user.User;
import com.habitsnap.dto.mealrecord.MealRecordCreateRequest;
import com.habitsnap.dto.mealrecord.MealRecordResponse;
import com.habitsnap.dto.mealrecord.MealRecordUpdateRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/* MealRecordController 통합 테스트
* - MockMvc 기반 REST API 검증
* - JWT 인증(MockUser) + Multipart 요청 포함
* - 실제 Service 호출(MockBean)
*/
@SpringBootTest
@AutoConfigureMockMvc
public class MealRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MealRecordService mealRecordService;

    // private CustomUserDetails mockUser;

    // 테스트 전역에서 접근할 수 있도록 전역 변수로 선언 (추가)
    private User mockUserEntity;

    @BeforeEach
    void setupSecurityContext(){
        // JWT 인증 모의 유저
        // mockUser = new CustomUserDetails(1L, "test@habitsnap.com");

        // User 엔티티를 클래스 필드로 선언해 재사용이 가능하도록 함
        mockUserEntity = User.builder()
                .id(1L)
                .email("test@habitsnap.com")
                .password("habitsnapPwd")
                .nickname("테스트유저")
                .build();

        // CusteomUserDetails로 감싸기 (Spring Security 인증용)
        CustomUserDetails customUserDetails = new CustomUserDetails(mockUserEntity);

        // SecurityContext에 인증 객체 주입
        SecurityContext context = SecurityContextHolder.createEmptyContext();   // 새로운 빈(SecurityContext가 없는) 인증 컨텍스트를 하나 생성
        context.setAuthentication(      // 인증 정보 등록
                new UsernamePasswordAuthenticationToken(                        // 로그인 완료된 사용자를 만드는 코드
                    customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);  // 최종 등록

        /*SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
        );*/

    }

    // ---------------------------- CREATE ----------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("식사 기록 등록 성공 - Multipart(JSON + 파일 선택적)")
    void createMealRecord_success() throws Exception {
        // given(테스트 준비 단계)
        MealRecordCreateRequest request = new MealRecordCreateRequest(
            MealType.BREAKFAST,
            "아보카도 닭가슴살 볶음밥",
            Portion.ONE,
            4,
            "잡곡밥",
            "닭가슴살",
            "아보카도",
            "아침식사 잘 챙김"
        );

        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json", objectMapper.writeValueAsBytes(request)
        );

        MealRecordResponse response = MealRecordResponse.builder()
                .id(1L)
                .mealDate(LocalDate.now())
                .mealTime(LocalTime.of(8,30))
                .mealType(MealType.BREAKFAST.name())
                .mealName("닭가슴살 아보카도 덮밥")
                .carb("잡곡밥")
                .protein("닭가슴살")
                .fat("아보카도")
                .notes("아침식사 잘 챙김")
                .build();

        // 위에서 전역으로 User 객체 생성

        given(mealRecordService.createMealRecordWithPhoto(any(), any(), mockUserEntity))
                .willReturn(response);


        // when(요청) & then(검증)
        mockMvc.perform(multipart("/api/meals")
                        .file(data)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is2xxSuccessful())     // Service mock이 반환값을 줬기 때문에 OK로 설정
                .andExpect(jsonPath("$.data.mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.data.mealName").value("닭가슴살 아보카도 덮밥"))
                .andExpect(jsonPath("$.data.protein").value("닭가슴살"));

    }


    // ------------------------ READ 1: 단건 조회 ------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("식사 기록 단건 조회 성공")
    void getMealRecord_success() throws Exception {
        // given
        MealRecordResponse response = MealRecordResponse.builder()
                .id(1L)
                .mealDate(LocalDate.now())
                .mealTime(LocalTime.of(8,30))
                .mealType(MealType.BREAKFAST.name())
                .mealName("오버나이트 오트밀")
                .protein("그릭요거트")
                .notes("간단한 아침")
                .build();

        given(mealRecordService.getMealRecord(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/meals/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.data.mealName").value("오버나이트 오트밀"))
                .andExpect(jsonPath("$.data.protein").value("그릭요거트"));
    }


    // ------------------------ READ 2: 날짜별 조회 ------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("특정 날짜별 식사기록 조회 성공")
    void getMealRecordsByDate_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 2);
        List<MealRecordResponse> responses = List.of(
                MealRecordResponse.builder().mealType(MealType.LUNCH.name()).protein("오리고기").build(),
                MealRecordResponse.builder().mealType(MealType.DINNER.name()).protein("연어").build()
        );

        given(mealRecordService.getMealRecordsByDate(mockUserEntity, eq(date)))
                .willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/meals/date").param("value", "2025-12-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].mealType").value("LUNCH"))
                .andExpect(jsonPath("$.data[1].protein").value("연어"));
    }


    // ------------------------ READ 3: 기간별 조회 ------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("특정 기간 범위 조회 성공")
    void getMealRecordsByPeriod_success() throws Exception {
        // given
        List<MealRecordResponse> responses = List.of(
                MealRecordResponse.builder().mealDate(LocalDate.of(2025,11,24)).mealType(MealType.BREAKFAST.name()).build(),
                MealRecordResponse.builder().mealDate(LocalDate.of(2025,11,25)).mealType(MealType.LUNCH.name()).build(),
                MealRecordResponse.builder().mealDate(LocalDate.of(2025,11,26)).mealType(MealType.DINNER.name()).build()
        );

        given(mealRecordService.getMealRecordsByPeriod(mockUserEntity, any(), any()))
                .willReturn(responses);


        // when & then
        mockMvc.perform(get("/api/meals/range")
                        .param("start", "2025-11-24")
                        .param("end", "2025-11-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.data[2].mealType").value("DINNER"));
    }


    // ---------------------------- UPDATE ----------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("식사 기록 수정 성공 - null 아닌 필드만 반영")
    void updateMealRecord_partial_success() throws Exception {
        // given
        MealRecordUpdateRequest updateRequest = new MealRecordUpdateRequest();
        updateRequest.setNotes("수정된 식단");

        MealRecordResponse updatedResponse = MealRecordResponse.builder()
                .id(1L)
                .notes("수정된 식단")
                .protein("닭가슴살")
                .mealType(MealType.SNACK.name())
                .build();

        given(mealRecordService.updateMealRecord(any())).willReturn(updatedResponse);

        // when & then
        mockMvc.perform(patch("/api/meals/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes").value("수정된 식단"))
                .andExpect(jsonPath("$.data.protein").value("닭가슴살"));
    }


    // ---------------------------- DELETE ----------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("식사 기록 삭제 성공")
    void deleteMealRecord_success() throws Exception {
        // given
        Mockito.doNothing().when(mealRecordService).deleteMealRecord(1L);

        // when & then
        mockMvc.perform(delete("/api/meals/{id}", 1L))
                .andExpect(status().isOk());
    }


    // ---------------------------- EXCEPTION ----------------------------
    @Test
    /*@WithMockUser*/
    @DisplayName("존재하지 않는 ID 조회 시 예외 발생")
    void getMealRecord_notFound() throws Exception {
        given(mealRecordService.getMealRecord(9999L))
                .willThrow(new CustomException(ErrorCode.MEAL_NOT_FOUND));

        mockMvc.perform(get("/api/meals/{id}", 9999L))
                .andExpect(status().is4xxClientError());

    }




}
