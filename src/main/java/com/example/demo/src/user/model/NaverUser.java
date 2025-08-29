package com.example.demo.src.user.model;

import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.LoginType;
import com.example.demo.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NaverUser {
    public String id;
    public String email;
    public Boolean verifiedEmail;
    public String name;
    public String givenName;
    public String familyName;
    public String picture;
    public String locale;

    public User toEntity() {
        return User.builder()
                .loginId(this.email)
                .email(this.email)
                .password("NONE")
                .name(this.name)
                .phoneNumber(null)
                .birthDate(java.time.LocalDate.of(2000,1,1))
                .termsOfServiceAgreed(true)
                .privacyConsentStatus(true)
                .locationServiceAgreed(true)
                .accountStatus(AccountStatus.ACTIVE)
                .loginType(LoginType.NAVER)
                .privacyConsentStatus(true)
                .joinedAt(java.time.LocalDateTime.now())
                .privacyConsentDate(java.time.LocalDateTime.now())
                .subscriptionActive(false)
                .build();
    }
}


