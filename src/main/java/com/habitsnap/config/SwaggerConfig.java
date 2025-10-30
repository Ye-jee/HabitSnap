package com.habitsnap.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI habitSnapOpenAPI(){

        return new OpenAPI()
                .info(new Info()
                        .title("HabitSnap API Documentation")
                        .description("""
                                사진 기반 식사 기록 및 건강한 습관 형성 프로젝트, HabitSnap의 REST API 명세입니다.
                                각 엔드포인트는 Swagger UI를 통해 테스트할 수 있습니다.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Developed by Yeji An")
                                .url("https://github.com/Ye-jee/HabitSnap"))
                        .license(new License()
                                .name("All rights reserved")
                                .url("https://github.com/Ye-jee/HabitSnap"))
                );


    }
}
