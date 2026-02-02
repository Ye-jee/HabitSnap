package com.habitsnap.docs;

/* HabitSnap Swagger 문서용 응답 예시 모음
* - Swagger UI에서 ExampleObject로 표시되는 JSON
* */

public class ApiExamples {

    // 식사 기록 등록 성공
    public static final String CREATE_MEAL_SUCCESS = """
        {
          "status": 201,
          "message": "식사 기록 등록 성공",
          "code": null,
          "timestamp": "2025-12-04T14:45:00.000",
          "data": {
            "id": 11,
            "userId": 1,
            "mealType": "BREAKFAST",
            "mealName": "닭가슴살 아보카도 덮밥",
            "portion": "ONE",
            "fullnessLevel": 3,
            "carb": "잡곡밥",
            "protein": "닭가슴살",
            "fat": "아보카도",
            "notes": "든든한 아침식사 였음",
            "photoUrl": null,
            "mealDate": "2025-12-04",
            "mealTime": "14:45:00.000",
            "createdAt": "2025-12-04T14:45:00.000"
          }
        }
    """;


    // 식사 기록 단건 조회 성공
    public static final String GET_MEAL_SUCCESS = """
        {
          "status": 200,
          "message": "요청이 성공적으로 처리되었습니다.",
          "code": null,
          "timestamp": "2025-12-04T14:46:00.000",
          "data": {
            "id": 13,
            "userId": 1,
            "mealType": "LUNCH",
            "mealName": "연어 샐러드",
            "portion": "ONE",
            "fullnessLevel": 3,
            "carb": "감자",
            "protein": "연어",
            "fat": "아보카도",
            "notes": "맛있는 점심이었음",
            "photoUrl": null,
            "mealDate": "2025-12-04",
            "mealTime": "14:45:00.000",
            "createdAt": "2025-12-04T14:45:00.000"
          }
        }
    """;


    // 식사 기록 삭제 성공
    public static final String DELETE_MEAL_SUCCESS = """
        {
          "status": 200,
          "message": "식사 기록이 삭제되었습니다.",
          "code": null,
          "timestamp": "2025-12-04T14:47:00.000",
          "data": null
        }
    """;


    // 존재하지 않는 식사 기록
    public static final String MEAL_NOT_FOUND = """
        {
          "status": 404,
          "message": "식사 기록을 찾을 수 없습니다.",
          "code": "MEAL_NOT_FOUND",
          "timestamp": "2025-12-04T14:48:00.000",
          "data": null
        }
    """;


    // 토큰 인증 실패 또는 토큰 만료
    public static final String MISSING_TOKEN = """
            {
              "status": 401,
              "message": "Authorization 헤더가 존재하지 않습니다.",
              "code": "MISSING_TOKEN",
              "timestamp": "2026-01-28T20:56:00.0280839",
              "data": null
            }
     """;

    // 회원가입 성공
    public static final String SIGNUP_SUCCESS = """
            {
              "status": 200,
              "message": "회원가입에 성공했습니다.",
              "code": null,
              "timestamp": "2026-01-28T21:36:12.0526361",
              "data": null
            }
            """;

    // 회원가입 실패 - 비밀번호 8자리 이상
    public static final String INVALID_INPUT_VALUE = """
            {
              "status": 400,
              "message": "잘못된 입력값입니다.",
              "code": "INVALID_INPUT_VALUE",
              "timestamp": "2026-01-28T21:35:32.0872311",
              "data": {
                "password": "비밀번호는 최소 8자 이상이어야 합니다."
              }
            }
            
            """;

    // 로그인 성공
    public static final String LOGIN_SUCCESS = """
            {
              "status": 201,
              "message": "로그인에 성공했습니다.",
              "code": null,
              "timestamp": "2026-01-28T21:36:12.0526361",
              "data": null
            }
            """;

    // 로그인 실패(인증되지 않은 토큰이거나 만료 토큰)
    public static final String LOGIN_FAIL = """
            {
              "status": 401,
              "message": "만료된 토큰입니다.",
              "code": "EXPIRED_TOKEN",
              "timestamp": "2026-01-28T21:35:32.0872311",
              "data": null
            }
            """;


}
