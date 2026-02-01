package kr.java.elasticache.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    private boolean activated;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authority", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "authority_name")
    private Set<Authority> authorities;
}
