package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.LoginType;
import com.example.demo.src.user.entity.PrivacyConsentStatus;
import com.example.demo.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserReq {
    private String loginId;
    private String email;
    private String password;
    private String name;
    private String loginType;

    public User toEntity() {
        return User.builder()
                .loginId(this.loginId)
                .email(this.email)
                .password(this.password)
                .name(this.name)
                .accountStatus(AccountStatus.ACTIVE)
                .loginType(LoginType.valueOf(this.loginType.toUpperCase()))
                .privacyConsentStatus(PrivacyConsentStatus.AGREE)
                .privacyConsentDate(LocalDateTime.now())
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
