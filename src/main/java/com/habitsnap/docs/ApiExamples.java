package com.habitsnap.docs;

/* HabitSnap Swagger 문서용 응답 예시 모음
* - Swagger UI에서 ExampleObject로 표시되는 JSON
* */

public class ApiExamples {

    // 식사 기록 등록 성공
    public static final String CREATE_MEAL_SUCCESS = """
        {
          "status": 201,
          "message": "식사 기록이 성공적으로 등록되었습니다.",
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
          "message": "식사 기록 단건 조회 성공",
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


}
