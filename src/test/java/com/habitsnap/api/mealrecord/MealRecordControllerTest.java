package com.habitsnap.api.mealrecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitsnap.application.mealrecord.MealRecordService;
import com.habitsnap.config.CustomUserDetails;
import com.habitsnap.domain.mealrecord.enums.MealType;
import com.habitsnap.domain.mealrecord.enums.Portion;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;


import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/* MealRecordController 통합 테스트
* - MockMvc 기반 REST API 검증
* - JWT 인증(MockUser) + Multipart 요청 포함
* - 실제 Service, Repository 호출(MockBean)

* 왜 .with(user(...))가 필요한가?
*   - 컨트롤러가 @AuthenticationPrincipal CustomUserDetails를 받기 때문
*   - 테스트에서도 “로그인한 사용자”를 SecurityContext에 넣어줘야 한다.

* 왜 .with(csrf())가 필요한가?
*   - Spring Security 기본 설정에서 POST/PATCH/DELETE는 CSRF 토큰 없으면 403(Access Denied)
*   - 브라우저는 자동으로 CSRF 토큰을 보내지만, MockMvc는 아니어서 테스트에서 직접 추가해야 한다.

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

    @MockBean
    private UserRepository userRepository;      // 추가

    // private CustomUserDetails mockUser;

    // 테스트 전역에서 접근할 수 있도록 전역 변수로 선언 (추가)
    private User mockUserEntity;

    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setup(){
        // JWT 인증 모의 유저
        // mockUser = new CustomUserDetails(1L, "test@habitsnap.com");

        // User 엔티티를 클래스 필드로 선언해 재사용이 가능하도록 함
        mockUserEntity = User.builder()
                .id(1L)
                .email("test@habitsnap.com")
                .password("habitsnapPwd")
                .nickname("테스트유저")
                .build();

        // 이제 인증 주입은 요청마다 진행
        customUserDetails = new CustomUserDetails(mockUserEntity);

        // 컨트롤러에서 항상 userRepository.findByEmail(userDetails.getEmail())로 User를 조회하므로, 공통 stub 고정
        given(userRepository.findByEmail("test@habitsnap.com"))
                .willReturn(Optional.of(mockUserEntity));

        // CusteomUserDetails로 감싸기 (Spring Security 인증용)
        /*CustomUserDetails customUserDetails = new CustomUserDetails(mockUserEntity);

        // SecurityContext에 인증 객체 주입
        SecurityContext context = SecurityContextHolder.createEmptyContext();   // 새로운 빈(SecurityContext가 없는) 인증 컨텍스트를 하나 생성
        context.setAuthentication(      // 인증 정보 등록
                new UsernamePasswordAuthenticationToken(                        // 로그인 완료된 사용자를 만드는 코드
                    customUserDetails, null, customUserDetails.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);  // 최종 등록*/

        /*SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
        );*/
    }

    // 매 요청마다 "로그인한 사용자"를 주입하는 헬퍼
    private RequestPostProcessor auth(){
        return user(customUserDetails);
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

        /*given(mealRecordService.createMealRecordWithPhoto(any(), any(), eq(mockUserEntity)))
                .willReturn(response);*/

        // photo는 테스트 요청에서 보내지 않으므로 null 가능 → nullable()로 매칭
        given(mealRecordService.createMealRecordWithPhoto(
                any(MealRecordCreateRequest.class),
                nullable(MultipartFile.class),
                any(User.class)
        )).willReturn(response);


        // when(요청) & then(검증)
        mockMvc.perform(multipart("/api/meals")
                        .file(data)
                        .with(auth())       // 인증 주입 (AuthenticationPrincipal 채워짐)
                        .with(csrf())       // POST는 CSRF 없으면 403 에러 발생
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8")
                )
                .andDo(print())
                .andExpect(status().isCreated())     // Service mock이 반환값을 줬기 때문에 OK로 설정, 컨트롤러가 201(CREATED) 반환
                .andExpect(jsonPath("$.data.mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.data.mealName").value("닭가슴살 아보카도 덮밥"))
                .andExpect(jsonPath("$.data.protein").value("닭가슴살"));

    }


    // ------------------------ READ 1: 단건 조회 ------------------------
    @Test
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
        // 보안 설정에서 /api/** 인증 필요로 걸어뒀을 가능성이 높아서 auth() 붙여주는 편이 안전
        mockMvc.perform(get("/api/meals/{id}", 1L)
                        .with(auth()))
                .andDo(print())
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

        /*given(mealRecordService.getMealRecordsByDate(eq(mockUserEntity), eq(date)))
                .willReturn(responses);*/
        given(mealRecordService.getMealRecordsByDate(any(User.class), eq(date)))
                .willReturn(responses);


        // when & then
        mockMvc.perform(get("/api/meals/date")
                        .with(auth())
                        .param("value", "2025-12-02"))
                .andDo(print())
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
                MealRecordResponse.builder()
                        .mealDate(LocalDate.of(2025,11,24))
                        .mealType(MealType.BREAKFAST.name())
                        .build(),
                MealRecordResponse.builder()
                        .mealDate(LocalDate.of(2025,11,25))
                        .mealType(MealType.LUNCH.name())
                        .build(),
                MealRecordResponse.builder()
                        .mealDate(LocalDate.of(2025,11,26))
                        .mealType(MealType.DINNER.name())
                        .build()
        );

        /*given(mealRecordService.getMealRecordsByPeriod(eq(mockUserEntity), any(), any()))
                .willReturn(responses);*/
        given(mealRecordService.getMealRecordsByPeriod(any(User.class), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/meals/range")
                        .with(auth())
                        .param("start", "2025-11-24")
                        .param("end", "2025-11-26"))
                .andDo(print())
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

        /*given(mealRecordService.updateMealRecord(any())).willReturn(updatedResponse);*/
        given(mealRecordService.updateMealRecord(
                any(User.class),
                any(MealRecordUpdateRequest.class)
        )).willReturn(updatedResponse);

        // when & then
        // 요청 바디는 "updateRequest"가 들어가야 함 (응답 DTO 보내면 깨짐)
        mockMvc.perform(patch("/api/meals/{id}", 1L)
                        .with(auth())       // 인증
                        .with(csrf())       // PATCH는 CSRF 없으면 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))       // updatedResponse 였다가 수정
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes").value("수정된 식단"))
                .andExpect(jsonPath("$.data.protein").value("닭가슴살"));
    }


    // ---------------------------- DELETE ----------------------------
    @Test
    @DisplayName("식사 기록 삭제 성공")
    void deleteMealRecord_success() throws Exception {
        // given
        Mockito.doNothing().when(mealRecordService)
                .deleteMealRecord(
                        any(User.class),
                        eq(1L));

        // when & then
        mockMvc.perform(delete("/api/meals/{id}", 1L)
                    .with(auth())       // 인증
                    .with(csrf())       // DELETE는 CSRF 없으면 403
                )
                .andDo(print())
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
