package kr.java.elasticache.service;

import kr.java.elasticache.domain.Authority;
import kr.java.elasticache.domain.UserAccount;
import kr.java.elasticache.dto.UserDto;
import kr.java.elasticache.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        Set<Authority> authorities = new HashSet<>();
        authorities.add(Authority.ROLE_USER);

        if (userDto.isAdmin()) {
            authorities.add(Authority.ROLE_ADMIN);
        }

        UserAccount user = UserAccount.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(authorities)
                .activated(true)
                .build();

        return UserDto.from(userRepository.save(user));
    }
}
