package com.example.demo.src.payment;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.entity.PaymentStatus;
import com.example.demo.src.payment.model.BillingPlanRes;
import com.example.demo.src.payment.model.PreparePaymentReq;
import com.example.demo.src.payment.model.PaymentFailReq;
import com.example.demo.src.payment.model.VerifyPaymentReq;
import com.example.demo.src.payment.model.VerifyPaymentRes;
import com.example.demo.src.subscription.SubscriptionHistoryRepository;
import com.example.demo.src.subscription.SubscriptionRepository;
import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.subscription.entity.SubscriptionHistory;
import com.example.demo.src.subscription.entity.SubscriptionStatus;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.demo.common.entity.BaseEntity.State;
import static com.example.demo.common.response.BaseResponseStatus.NOT_FIND_USER;
import static com.example.demo.common.response.BaseResponseStatus.SUBSCRIPTION_REQUIRED;

@Tag(name = "payment", description = "구독 결제 API")
@RestController
@RequestMapping("/app/subscription")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PaymentRepository paymentRepository;
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
        Payment requested = paymentRepository.findByMerchantUid(req.getMerchantUid()).orElse(null);

        if (requested == null || !requested.getUser().getId().equals(user.getId())) {
            VerifyPaymentRes res = VerifyPaymentRes.builder()
                    .status("PAYMENT_NOT_FOUND")
                    .failReason("결제 내역을 찾을 수 없습니다.")
                    .build();
            return new BaseResponse<>(res);
        }

        Subscription subscription = requested.getSubscription();

        if (subscription == null) {
            VerifyPaymentRes res = VerifyPaymentRes.builder()
                    .status("NOT_SUBSCRIBED")
                    .build();
            return new BaseResponse<>(res);
        }

        SubscriptionHistory latest = subscriptionHistoryRepository
                .findTopBySubscriptionOrderByCreatedAtDesc(subscription)
                .orElse(null);

        if (latest == null || latest.getStatus() != SubscriptionStatus.PENDING) {
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
            SubscriptionHistory history = subscription.activate(
                    LocalDate.now(),
                    LocalDate.now().plusMonths(1),
                    LocalDateTime.now()
            );
            subscriptionHistoryRepository.save(history);
        } else {
            SubscriptionHistory history = subscription.cancelPending();
            subscriptionHistoryRepository.save(history);
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
     * 결제 실패 기록 API
     * [POST] /app/subscription/payments/fail
     * @return BaseResponse<>((Void) null)
     */
    // 결제창 실패/중단 기록(impUid가 생성되지 않은 경우 포함)
    @Operation(summary = "결제 실패 기록", description = "결제창 실패/중단 시 실패 이력을 저장합니다.")
    @PostMapping("/payments/fail")
    public BaseResponse<Void> fail(@RequestBody PaymentFailReq req) {
        User user = getCurrentUser();
        paymentRepository.findByMerchantUid(req.getMerchantUid())
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .filter(p -> p.getStatus() == PaymentStatus.REQUESTED)
                .ifPresent(p -> paymentGatewayService.recordFailure(
                        user,
                        p.getSubscription(),
                        req.getMerchantUid(),
                        req.getReason() == null ? "사용자 직접 취소 또는 실패" : req.getReason()
                ));

        return new BaseResponse<>((Void) null);
    }

    private User getCurrentUser() {
        Long userId = jwtService.getUserId();
        return userRepository.findByIdAndState(userId, State.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
    }

    private Subscription getOrCreatePendingSubscription(User user) {
        Subscription subscription = subscriptionRepository
                .findByUser(user)
                .orElseGet(() -> subscriptionRepository.save(
                        Subscription.builder()
                                .user(user)
                                .build()
                ));

        SubscriptionHistory latest = subscriptionHistoryRepository
                .findTopBySubscriptionOrderByCreatedAtDesc(subscription)
                .orElse(null);

        if (latest == null || latest.getStatus() != SubscriptionStatus.PENDING) {
            SubscriptionHistory pending = SubscriptionHistory.builder()
                    .subscription(subscription)
                    .user(user)
                    .status(SubscriptionStatus.PENDING)
                    .build();
            subscriptionHistoryRepository.save(pending);
        }
        return subscription;
    }
}
