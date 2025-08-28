package com.example.demo.src.payment;

import com.example.demo.common.exceptions.BaseException;
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
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.demo.common.entity.BaseEntity.State;
import static com.example.demo.common.response.BaseResponseStatus.NOT_FIND_USER;
import static com.example.demo.common.response.BaseResponseStatus.SUBSCRIPTION_REQUIRED;

@RestController
@RequestMapping("/app/subscription")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtService jwtService;

    /**
     * 구독 정보 반환 API (DB 연동 없음)
     * [GET] /app/subscription/plan
     * @return BaseResponse<BillingPlanRes>
     */
    @Operation(summary = "구독 플랜 조회", description = "월 9,900원 플랜 정보를 반환합니다.")
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

    /**
     * 결제 사전등록 API (클라이언트가 결제창 열기 전)
     * [POST] /app/subscription/payments/request
     * @return BaseResponse<>((Void) null)
     */
    @Operation(summary = "결제 사전등록", description = "PortOne에 결제 사전등록을 수행하고 결제 이력을 REQUESTED로 기록합니다.")
    @PostMapping("/payments/request")
    public BaseResponse<Void> prepare(@RequestBody PreparePaymentReq req) {
        User user = getCurrentUser();
        Subscription subscription = getOrCreatePendingSubscription(user);

        if (subscription == null) {
            return new BaseResponse<>(SUBSCRIPTION_REQUIRED);
        }

        paymentGatewayService.requestPayment(req.getMerchantUid());
        paymentGatewayService.recordRequested(user, subscription, req.getMerchantUid());
        return new BaseResponse<>((Void) null);
    }

    /**
     * 결제 검증 API (결제 성공 콜백 이후 서버 검증)
     * [POST] /app/subscription/payments/verify
     * @return BaseResponse<VerifyPaymentRes>
     */
    @Operation(summary = "결제 검증", description = "imp_uid로 PortOne 결제내역을 조회하여 서버 금액과 비교, 성공/실패 이력을 저장합니다.")
    @PostMapping("/payments/verify")
    public BaseResponse<VerifyPaymentRes> verify(@RequestBody VerifyPaymentReq req) {
        User user = getCurrentUser();
        Subscription subscription = getLatestSubscription(user);

        if (subscription == null) {
            VerifyPaymentRes res = VerifyPaymentRes.builder()
                    .status("NOT_SUBSCRIBED")
                    .build();
            return new BaseResponse<>(res);
        }

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            VerifyPaymentRes res = VerifyPaymentRes.builder()
                    .status("SUBSCRIPTION_STATUS_MISMATCH")
                    .failReason("구독 상태는 PENDING 이어야 합니다.")
                    .build();
            return new BaseResponse<>(res);
        }

        Payment payment = paymentGatewayService.verifyPayment(
                req.getImpUid(), req.getMerchantUid(), user, subscription
        );

        if (payment.getStatus().name().equals("PAID")) {
            subscription.activate(
                    LocalDate.now(),
                    LocalDate.now().plusMonths(1),
                    LocalDateTime.now()
            );
        } else {
            subscription.cancelPending();
        }
        userRepository.save(user);
        subscriptionRepository.save(subscription);
        paymentGatewayService.linkPaymentToSubscription(payment, subscription);

        VerifyPaymentRes res = VerifyPaymentRes.builder()
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .failReason(payment.getFailReason())
                .build();
        return new BaseResponse<>(res);
    }

    /**
     * 결제 검증 API (결제 성공 콜백 이후 서버 검증)
     * [POST] /app/subscription/payments/fail
     * @return BaseResponse<>((Void) null)
     */
    // 결제창 실패/중단 기록(impUid가 생성되지 않은 경우 포함)
    @Operation(summary = "결제 실패 기록", description = "결제창 실패/중단 시 실패 이력을 저장합니다.")
    @PostMapping("/payments/fail")
    public BaseResponse<Void> fail(@RequestBody PaymentFailReq req) {
        User user = getCurrentUser();
        Subscription subscription = getLatestSubscription(user);

        if (subscription != null) {
            paymentGatewayService.recordFailure(
                    user,
                    subscription,
                    req.getMerchantUid(),
                    req.getReason() == null ? "User cancelled or failed" : req.getReason()
            );
        }
        return new BaseResponse<>((Void) null);
    }

    private User getCurrentUser() {
        Long userId = jwtService.getUserId();
        return userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
    }

    private Subscription getOrCreatePendingSubscription(User user) {
        return subscriptionRepository
                .findTopByUserAndStateAndStatusOrderByCreatedAtDesc(
                        user, State.ACTIVE, SubscriptionStatus.PENDING)
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .user(user)
                                .status(SubscriptionStatus.PENDING)
                                .build()
                ));
    }

    private Subscription getLatestSubscription(User user) {
        return subscriptionRepository
                .findTopByUserAndStateOrderByCreatedAtDesc(user, State.ACTIVE)
                .orElse(null);
    }
}
