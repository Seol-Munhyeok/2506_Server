package com.example.demo.src.user.entity;

import com.example.demo.common.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity // 필수, Class 를 Database Table화 해주는 것이다
@Table(name = "users") // Table 이름을 명시해주지 않으면 class 이름을 Table 이름으로 대체한다.
public class User extends BaseEntity {

    @Id // PK를 의미하는 어노테이션
    @Column(name = "user_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(name = "user_name", length = 100)
    private String name;

    @Column(name = "password_hash", length = 255)
    private String password;

    @Column(length = 255)
    private String email;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20, nullable = false)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", length = 20, nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_consent_status", length = 20, nullable = false)
    private PrivacyConsentStatus privacyConsentStatus;

    @Column(name = "privacy_consent_date")
    private LocalDateTime privacyConsentDate;

    @Builder
    public User(Long id, String loginId, String name, String password, String email,
                LocalDateTime joinedAt, LocalDateTime lastLoginAt, AccountStatus accountStatus,
                LoginType loginType, PrivacyConsentStatus privacyConsentStatus, LocalDateTime privacyConsentDate) {
        this.id = id;
        this.loginId = loginId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.joinedAt = joinedAt;
        this.lastLoginAt = lastLoginAt;
        this.accountStatus = accountStatus;
        this.loginType = loginType;
        this.privacyConsentStatus = privacyConsentStatus;
        this.privacyConsentDate = privacyConsentDate;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void deleteUser() {
        this.state = State.INACTIVE;
    }

    public void withdrawPrivacyConsent() {
        this.privacyConsentStatus = PrivacyConsentStatus.DISAGREE;
        this.privacyConsentDate = null;
    }
}
