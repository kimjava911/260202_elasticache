package kr.java.elasticache.repository;

import kr.java.elasticache.domain.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    @EntityGraph(attributePaths = "authorities")
    Optional<UserAccount> findOneWithAuthoritiesByUsername(String username);
}
