package com.habitsnap.domain.user;

import com.habitsnap.domain.mealrecord.entity.MealRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")  // MySQL 예약어 'user' 피하기 위해 복수형 사용
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "올바른 이메일 형식어야 합니다.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)            //ENUM 값을 DB에 문자열로 저장
    @Column(nullable = true, length = 10)
    private Gender gender;                  // enum 타입

    @Column(nullable = true)
    private Float height;       // cm 단위

    @Column(nullable = true)
    private Float weight;       // kg 단위

    @CreationTimestamp          // 생성시간 자동기록
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp            // 수정시간 자동기록
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계 관련 추가
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealRecord> mealRecords = new ArrayList<>();

    // 비밀번호 변경 로직
    public void updatePassword(String encodedPassword){
        this.password = encodedPassword;
    }

    public void updateProfile(String nickname, Gender gender, Float height, Float weight){
        this.nickname = nickname;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

}
