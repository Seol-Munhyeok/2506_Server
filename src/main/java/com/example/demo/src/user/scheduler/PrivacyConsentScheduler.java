package com.example.demo.src.user.scheduler;

import com.example.demo.src.user.MailService;
import com.example.demo.src.user.NotificationService;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PrivacyConsentScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MailService mailService;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void processPrivacyConsents() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<User> users = userRepository.findByPrivacyConsentDateBeforeAndPrivacyConsentStatus(
                oneYearAgo,
                true
        );
        for (User user : users) {
            user.withdrawPrivacyConsent();
            notificationService.sendPrivacyConsentRenewal(user);
            mailService.sendPrivacyConsentRenewal(user);
        }
    }
}