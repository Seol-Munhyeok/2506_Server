package com.example.demo;

import com.example.demo.src.user.MailService;
import com.example.demo.src.user.NotificationService;
import com.example.demo.src.user.UserDataManager;
import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.LoginType;
import com.example.demo.src.user.entity.User;
import com.example.demo.src.user.scheduler.PrivacyConsentScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivacyConsentSchedulerTest {

    @Mock
    private UserDataManager userDataManager;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MailService mailService;
    @InjectMocks
    private PrivacyConsentScheduler privacyConsentScheduler;

    @Test
    void processPrivacyConsents_updatesStatusAndSendsNotifications() {
        User user = User.builder()
                .id(1L)
                .loginId("login")
                .name("name")
                .password("pwd")
                .email("email@example.com")
                .phoneNumber(null)
                .birthDate(java.time.LocalDate.of(2000,1,1))
                .termsOfServiceAgreed(true)
                .privacyConsentStatus(true)
                .locationServiceAgreed(true)
                .joinedAt(LocalDateTime.now().minusYears(2))
                .lastLoginAt(null)
                .accountStatus(AccountStatus.ACTIVE)
                .loginType(LoginType.LOCAL)
                .privacyConsentStatus(true)
                .privacyConsentDate(LocalDateTime.now().minusYears(2))
                .build();

        when(userDataManager.findByPrivacyConsentDateBeforeAndPrivacyConsentStatus(any(), eq(true)))
                .thenReturn(Collections.singletonList(user));

        privacyConsentScheduler.processPrivacyConsents();

        assertFalse(user.isPrivacyConsentStatus());
        verify(notificationService).sendPrivacyConsentRenewal(user);
        verify(mailService).sendPrivacyConsentRenewal(user);
    }
}