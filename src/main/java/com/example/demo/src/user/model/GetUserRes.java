package com.example.demo.src.user.model;


import com.example.demo.src.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserRes {

    private Long id;
    private String loginId;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private boolean termsOfServiceAgreed;
    private boolean privacyConsentStatus;
    private boolean locationServiceAgreed;
    private String accountStatus;
    private String loginType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime joinedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastLoginAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime privacyConsentDate;

    public GetUserRes(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.birthDate = user.getBirthDate();
        this.termsOfServiceAgreed = user.isTermsOfServiceAgreed();
        this.privacyConsentStatus = user.isPrivacyConsentStatus();
        this.locationServiceAgreed = user.isLocationServiceAgreed();
        this.accountStatus = user.getAccountStatus() != null ? user.getAccountStatus().name() : null;
        this.loginType = user.getLoginType() != null ? user.getLoginType().name() : null;
        this.joinedAt = user.getJoinedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.privacyConsentDate = user.getPrivacyConsentDate();
    }
}
