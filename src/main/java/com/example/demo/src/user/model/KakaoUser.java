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
public class KakaoUser {
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
                .accountStatus(AccountStatus.ACTIVE)
                .loginType(LoginType.KAKAO)
                .privacyConsentStatus(true)
                .joinedAt(java.time.LocalDateTime.now())
                .build();
    }
}

