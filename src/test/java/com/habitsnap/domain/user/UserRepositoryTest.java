package com.habitsnap.domain.user;

import com.habitsnap.domain.user.User;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.Optional;

// import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThat;


@Disabled
@DataJpaTest        // JPA 관련 빈만 로드 (테스트 DB 자동 구성)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // 실제 MySQL 설정 그대로 사용하게 함
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // ---------------------------------------------------------
    // 🧩 CREATE TEST
    // ---------------------------------------------------------
    @Test
    /*@Commit*/
    @DisplayName("User 저장(Create) 테스트")
    void createUser(){
        // given - 새로운 사용자 정보를 준비
        User user = User.builder()
                .email("crate@habitsnap.com")
                .password("raw_pw1234")
                .nickname("하빗")
                .gender(Gender.FEMALE)
                .height(165.7f)
                .weight(60.2f)
                .build();

        // when - UserRepository의 save()를 호출 (insert 쿼리 실행)
        User savedUser = userRepository.save(user);

        // then - 저장 결과를 검증
        // id가 자동 생성되었는지, 이메일/닉네임이 정상 저장됐는지 확인
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("crate@habitsnap.com");
        assertThat(savedUser.getNickname()).isEqualTo("하빗");
    }

    // ---------------------------------------------------------
    // 🔍 READ TEST
    // ---------------------------------------------------------
    @Test
    /*@Commit*/
    @DisplayName("User 조회(Read) 테스트")
    void readUser(){
        // given - 테스트용 유저를 DB에 먼저 저장
        User user = userRepository.save(
                User.builder()
                        .email("read@habitsnap.com")
                        .password("password")
                        .nickname("리드테스트")
                        .build()
        );

        // when - 이메일로 조회 (select 쿼리 실행)
        Optional<User> foundUser = userRepository.findByEmail("read@habitsnap.com");

        // then - 조회 결과가 존재하며, 저장한 값과 일치하는지 확인
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo("리드테스트");
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
    }


    // ---------------------------------------------------------
    // ✏️ UPDATE TEST
    // ---------------------------------------------------------
    @Test
    /*@Commit*/
    @DisplayName("User 수정(Update) 테스트")
    void updateUser(){
        // given - 기존 사용자가 DB에 존재하는 상태
        User user = userRepository.save(
                User.builder()
                        .email("update@habitsnap.com")
                        .password("old_pw123")
                        .nickname("업데이트전")
                        .height(160f)
                        .weight(50f)
                        .build()
        );

        // when - 닉네임과 신체정보를 수정하고, 성별을 추가하고 save() 호출 (update 쿼리 실행)
        user.updateProfile("업데이트후", Gender.FEMALE, 162f, 52f);
        user.updatePassword("new_pw456");
        userRepository.save(user);

        // then - 다시 조회해보면 변경사항이 반영되어 있어야 함
        Optional<User> updatedUser = userRepository.findByEmail("update@habitsnap.com");
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getNickname()).isEqualTo("업데이트후");
        assertThat(updatedUser.get().getPassword()).isEqualTo("new_pw456");
        assertThat(updatedUser.get().getHeight()).isEqualTo(162f);
        assertThat(updatedUser.get().getWeight()).isEqualTo(52f);
    }


    // ---------------------------------------------------------
    // ❌ DELETE TEST
    // ---------------------------------------------------------
    @Test
    @DisplayName("User 삭제(Delete) 테스트")
    void deleteUser(){
        // given - DB에 유저 한 명이 저장된 상태
        User user = userRepository.save(
                User.builder()
                        .email("delete@habitsnap.com")
                        .password("password")
                        .nickname("삭제대상")
                        .build()
        );

        // when - 해당 유저를 삭제 (delete 쿼리 실행)
        userRepository.delete(user);

        // then - 같은 이메일로 다시 조회했을 때 존재하지 않아야 함
        Optional<User> deletedUser = userRepository.findByEmail("delete@habitsnap.com");
        assertThat(deletedUser).isEmpty();
    }


    // ---------------------------------------------------------
    // 📋 FIND ALL (옵션)
    // ---------------------------------------------------------
    @Test
    /*@Commit*/
    @DisplayName("모든 사용자 조회(Read-all) 테스트")
    void findAllUsers(){
        // given - 여러 유저를 저장
        userRepository.save(User.builder().email("a@habitsnap.com").password("password1").nickname("A").build());
        userRepository.save(User.builder().email("b@habitsnap.com").password("password2").nickname("B").build());
        userRepository.save(User.builder().email("c@habitsnap.com").password("password3").nickname("C").build());

        // when - findAll()로 전체 조회
        List<User> users = userRepository.findAll();

        // then - 저장된 수와 닉네임 확인
        assertThat(users).hasSizeGreaterThanOrEqualTo(3);
        assertThat(users)
                .extracting(User::getNickname)
                .contains("A", "B", "C");

    }

}
