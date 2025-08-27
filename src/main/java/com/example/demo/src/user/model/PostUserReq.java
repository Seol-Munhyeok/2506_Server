package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.LoginType;
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
    private String phoneNumber;
    private String birthDate;
    private boolean termsOfServiceAgreed;
    private boolean privacyConsentStatus;
    private boolean locationServiceAgreed;
    private String loginType;

    public User toEntity() {
        return User.builder()
                .loginId(this.loginId)
                .email(this.email)
                .password(this.password)
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .birthDate(java.time.LocalDate.parse(this.birthDate))
                .termsOfServiceAgreed(this.termsOfServiceAgreed)
                .privacyConsentStatus(this.privacyConsentStatus)
                .locationServiceAgreed(this.locationServiceAgreed)
                .accountStatus(AccountStatus.ACTIVE)
                .loginType(LoginType.valueOf(this.loginType.toUpperCase()))
                .privacyConsentStatus(true)
                .privacyConsentDate(LocalDateTime.now())
                .joinedAt(LocalDateTime.now())
                .subscriptionActive(false)
                .build();
    }
}
