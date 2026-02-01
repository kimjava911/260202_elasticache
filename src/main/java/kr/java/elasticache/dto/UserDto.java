package kr.java.elasticache.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.java.elasticache.domain.UserAccount;
import lombok.*;

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

    private boolean admin; // 관리자 여부

    public static UserDto from(UserAccount user) {
        if(user == null) return null;

        return UserDto.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
