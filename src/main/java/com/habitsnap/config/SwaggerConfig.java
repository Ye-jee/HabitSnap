package com.habitsnap.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* Swagger (springdoc-openapi) 설정 파일
 - HabitSnap API 문서화
 - JWT 인증 헤더 자동 적용(Bearer 스킴)
* */

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI habitSnapOpenAPI(){

        // JWT 인증용 SecurityScheme
        SecurityScheme bearerAuth = new SecurityScheme()
                .name("Authorization")              // 헤더 이름
                .type(SecurityScheme.Type.HTTP)     // HTTP 인증 방식
                .scheme("bearer")                   // 인증 스킴 이름
                .bearerFormat("JWT")                // 토큰 포맷
                .in(SecurityScheme.In.HEADER);      // 헤더 위치


        // HabitSnap API 정보
        Info apiInfo = new Info()
                .title("HabitSnap API Documentation")
                .description("""
                                사진 기반 식사 기록 및 건강한 습관 형성 프로젝트, HabitSnap의 백엔드 API 문서입니다.\n
                                각 엔드포인트는 Swagger UI를 통해 테스트할 수 있습니다.\n
                                """
                        + "이 문서는 JWT 기반 인증 시스템을 포함하며, Swagger 상단의 Authorize 버튼을 통해 "
                        + "Bearer Token을 입력하면 보호된 API를 테스트할 수 있습니다.")
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Developed by Yeji An")
                        .url("https://github.com/Ye-jee/HabitSnap"))
                .license(new License()
                        .name("All rights reserved")
                        .url("https://github.com/Ye-jee/HabitSnap"));

        // 보안 스킴과 문서 정보 결합
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))   // 보안 요구사항
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .info(apiInfo);

    }
}
