package com.habitsnap.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* JWT 인증 테스트용 보호 엔드포인트
  - 토큰이 있어야 접근 가능 (@PreAuthorize)
*/
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getMyInfo() {
        return ResponseEntity.ok("인증된 사용자 접근 성공");
    }
}
