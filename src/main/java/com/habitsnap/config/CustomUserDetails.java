package com.habitsnap.config;

import com.habitsnap.domain.user.User;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/* JWT 인증 후, SecurityContext에 저장되는 사용자 정보
* @AuthenticationPrincipal 로 컨트롤러에서 바로 접근 가능
* */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;      // 사용자 ID
    private final String email;     // 이메일

    // 기존 생성자 (운영용 - 실제 로그인 사용자)
    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
    }

    // 추가 생성자 (테스트용 - Mock 사용자)
    public CustomUserDetails(Long userId, String email){
        this.userId = userId;
        this.email = email;
    }

    // 권한 (현재는 단일 ROLE_USER만 사용)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    // 비밀번호는 JWT 인증 후엔 사용하지 않음
    @Override
    public String getPassword() {
        return null;
    }

    // email을 대신 username으로 사용
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
