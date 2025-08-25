package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserReq {
    private String loginId;
    private String email;
    private String password;
    private String name;

    private boolean isOAuth;

    public User toEntity() {
        return User.builder()
                .loginId(this.loginId)
                .email(this.email)
                .password(this.password)
                .name(this.name)
                .accountStatus("ACTIVE")
                .loginType("LOCAL")
                .privacyConsentStatus("AGREE")
                .joinedAt(java.time.LocalDateTime.now())
                .build();
    }
}
