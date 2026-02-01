package kr.java.elasticache.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.java.elasticache.domain.UserAccount;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;

    public static UserDto from(UserAccount user) {
        if(user == null) return null;

        return UserDto.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
