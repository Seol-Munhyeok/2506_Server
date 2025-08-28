package com.example.demo.src.payment;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.model.BillingPlanRes;
import com.example.demo.src.payment.model.PreparePaymentReq;
import com.example.demo.src.payment.model.PaymentFailReq;
import com.example.demo.src.payment.model.VerifyPaymentReq;
import com.example.demo.src.payment.model.VerifyPaymentRes;
import com.example.demo.src.subscription.SubscriptionRepository;
import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.subscription.entity.SubscriptionStatus;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.demo.common.entity.BaseEntity.State;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtService jwtService;

    @GetMapping("/plan")
    public BaseResponse<BillingPlanRes> getPlan() {
        BillingPlanRes res = BillingPlanRes.builder()
                .planId("basic_monthly")
                .amount(9900)
                .currency("KRW")
                .interval("month")
                .label("월 9,900원")
                .build();
        return new BaseResponse<>(res);
    }

    @PostMapping("/prepare")
    public BaseResponse<Void> prepare(@RequestBody PreparePaymentReq req) {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ensure subscription context exists (history model)
        Subscription subscription = subscriptionRepository
                .findTopByUserAndStateOrderByCreatedAtDesc(user, State.ACTIVE)
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .user(user)
                                .startDate(LocalDate.now())
                                .status(SubscriptionStatus.ACTIVE)
                                .paymentDate(LocalDateTime.now())
                                .build()
                ));

        // record REQUESTED history
        paymentGatewayService.requestPayment(req.getMerchantUid());
        paymentGatewayService.recordRequested(user, subscription, req.getMerchantUid());
        return new BaseResponse<>((Void) null);
    }

    @PostMapping("/verify")
    public BaseResponse<VerifyPaymentRes> verify(@RequestBody VerifyPaymentReq req) {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository
                .findTopByUserAndStateOrderByCreatedAtDesc(user, State.ACTIVE)
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .user(user)
                                .startDate(LocalDate.now())
                                .status(SubscriptionStatus.ACTIVE)
                                .paymentDate(LocalDateTime.now())
                                .build()
                ));

        Payment payment = paymentGatewayService.verifyPayment(
                req.getImpUid(), req.getMerchantUid(), user, subscription
        );

        // 결제 성공 시 사용자 구독 활성화
        if (payment.getStatus().name().equals("PAID")) {
            user.activateSubscription();
            userRepository.save(user);
            // 구독 기간 이력 생성 (1개월)
            Subscription activated = Subscription.builder()
                    .user(user)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(1))
                    .status(SubscriptionStatus.ACTIVE)
                    .paymentDate(LocalDateTime.now())
                    .build();
            subscriptionRepository.save(activated);
        }

        VerifyPaymentRes res = VerifyPaymentRes.builder()
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .failReason(payment.getFailReason())
                .build();
        return new BaseResponse<>(res);
    }

    // 결제창 실패/중단 기록(impUid가 생성되지 않은 경우 포함)
    @PostMapping("/fail")
    public BaseResponse<Void> fail(@RequestBody PaymentFailReq req) {
        Long userId = jwtService.getUserId();
        User user = userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = subscriptionRepository
                .findTopByUserAndStateOrderByCreatedAtDesc(user, State.ACTIVE)
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .user(user)
                                .startDate(LocalDate.now())
                                .status(SubscriptionStatus.ACTIVE)
                                .paymentDate(LocalDateTime.now())
                                .build()
                ));

        paymentGatewayService.recordFailure(
                user,
                subscription,
                req.getMerchantUid(),
                req.getReason() == null ? "User cancelled or failed" : req.getReason()
        );
        return new BaseResponse<>((Void) null);
    }
}
