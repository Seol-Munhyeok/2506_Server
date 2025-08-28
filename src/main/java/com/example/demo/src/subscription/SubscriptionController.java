package com.example.demo.src.subscription;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.subscription.entity.SubscriptionStatus;
import com.example.demo.src.subscription.model.SubscriptionStatusRes;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.demo.common.entity.BaseEntity.State;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/status")
    public BaseResponse<SubscriptionStatusRes> getStatus() {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Subscription> latest = subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user);

        boolean active = user.isSubscriptionActive();
        String status = latest.map(s -> s.getStatus().name()).orElse("NONE");
        SubscriptionStatusRes res = SubscriptionStatusRes.builder()
                .active(active)
                .status(status)
                .startDate(latest.map(Subscription::getStartDate).orElse(null))
                .endDate(latest.map(Subscription::getEndDate).orElse(null))
                .build();
        return new BaseResponse<>(res);
    }

    @PostMapping("/cancel")
    public BaseResponse<SubscriptionStatusRes> cancel() {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<Subscription> latestOpt = subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user);
        latestOpt.ifPresent(latest -> subscriptionRepository.save(
                latest.cancel(LocalDate.now())
        ));

        user.cancelSubscription();
        userRepository.save(user);

        SubscriptionStatusRes res = SubscriptionStatusRes.builder()
                .active(false)
                .status(SubscriptionStatus.CANCELED.name())
                .startDate(latestOpt.map(Subscription::getStartDate).orElse(null))
                .endDate(LocalDate.now())
                .build();
        return new BaseResponse<>(res);
    }
}

