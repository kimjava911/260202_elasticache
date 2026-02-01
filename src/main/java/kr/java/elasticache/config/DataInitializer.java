package kr.java.elasticache.config;

import kr.java.elasticache.domain.Authority;
import kr.java.elasticache.domain.UserAccount;
import kr.java.elasticache.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 초기 데이터: admin 계정이 없으면 생성
        if (userRepository.findOneWithAuthoritiesByUsername("admin").isEmpty()) {
            initAdminUser();
        }
    }

    private void initAdminUser() {
        UserAccount adminUser = UserAccount.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234")) // 비밀번호 암호화
                .nickname("관리자")
                .activated(true)
                .authorities(Collections.singleton(Authority.ROLE_ADMIN))
                .build();

        userRepository.save(adminUser);
    }
}
