package com.example.demo.src.subscription;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.payment.entity.PaymentStatus;
import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.subscription.entity.SubscriptionStatus;
import com.example.demo.src.subscription.model.SubscriptionStatusRes;
import com.example.demo.src.payment.PaymentRepository;
import com.example.demo.src.payment.PaymentGatewayService;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.demo.common.entity.BaseEntity.State;

@RestController
@RequestMapping("/app/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService gatewayService;

    @Operation(summary = "구독 상태 조회", description = "가장 최근 구독 이력을 기반으로 ACTIVE 여부를 반환합니다.")
    @GetMapping("/status")
    public BaseResponse<SubscriptionStatusRes> getStatus() {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new com.example.demo.common.exceptions.BaseException(
                        com.example.demo.common.response.BaseResponseStatus.NOT_FIND_USER));

        Optional<Subscription> latest = subscriptionRepository.findTopByUserAndStateOrderByCreatedAtDesc(user, State.ACTIVE);

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

    @Operation(summary = "구독 해지", description = "마지막 결제를 취소하고 구독 이력을 CANCELED로 추가, 사용자 구독 활성화를 해제합니다.")
    @PostMapping("/cancel")
    public BaseResponse<SubscriptionStatusRes> cancel() {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new com.example.demo.common.exceptions.BaseException(
                        com.example.demo.common.response.BaseResponseStatus.NOT_FIND_USER));

        Optional<Subscription> latestOpt = subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user);
        latestOpt.ifPresent(latest -> subscriptionRepository.save(latest.cancel(LocalDate.now())));

        latestOpt.ifPresent(latest -> {
            Subscription canceled = latest.cancel(LocalDate.now());
            subscriptionRepository.save(latest);
            subscriptionRepository.delete(canceled);
            paymentRepository.findTopBySubscriptionAndStatusOrderByPaidAtDesc(latest, PaymentStatus.PAID)
                    .ifPresent(paid -> gatewayService.cancelPayment(
                            paid.getImpUid(), paid.getMerchantUid(), user, latest, "사용자 직접 취소"
                    ));
        });

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
