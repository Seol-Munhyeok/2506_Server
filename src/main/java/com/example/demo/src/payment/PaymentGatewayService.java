package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.entity.PaymentGateway;
import com.example.demo.src.payment.entity.PaymentGatewayStatus;
import com.example.demo.src.payment.entity.PaymentStatus;
import com.example.demo.src.payment.model.PortOnePaymentResponse;
import com.example.demo.src.payment.model.PortOneTokenResponse;
import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService {

    private static final int SUBSCRIPTION_PRICE = 9900;

    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;

    private String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";
        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<PortOneTokenResponse> response = restTemplate.postForEntity(url, entity, PortOneTokenResponse.class);

        PortOneTokenResponse token = response.getBody();
        if (token == null || token.getResponse() == null) {
            throw new com.example.demo.common.exceptions.BaseException(
                    com.example.demo.common.response.BaseResponseStatus.SERVER_ERROR);
        }
        return token.getResponse().getAccessToken();
    }

    public void requestPayment(String merchantUid) {
        String token = getAccessToken();
        String url = "https://api.iamport.kr/payments/prepare";
        Map<String, Object> body = new HashMap<>();
        body.put("merchant_uid", merchantUid);
        body.put("amount", SUBSCRIPTION_PRICE);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, Map.class);
    }

    public void recordRequested(User user, Subscription subscription, String merchantUid) {
        Payment entity = Payment.builder()
                .user(user)
                .subscription(subscription)
                .merchantUid(merchantUid)
                .impUid(null)
                .amount(0)
                .status(PaymentStatus.REQUESTED)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(entity);
    }

    public void recordFailure(User user, Subscription subscription, String merchantUid, String reason) {
        Payment entity = Payment.builder()
                .user(user)
                .subscription(subscription)
                .merchantUid(merchantUid)
                .impUid(null)
                .amount(0)
                .status(PaymentStatus.FAILED)
                .failReason(reason)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(entity);
    }

    @Transactional
    public Payment verifyPayment(String impUid, String merchantUid,
                                 User user, Subscription subscription) {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<PortOnePaymentResponse> response = restTemplate.exchange(
                "https://api.iamport.kr/payments/" + impUid,
                HttpMethod.GET,
                entity,
                PortOnePaymentResponse.class);

        PortOnePaymentResponse body = response.getBody();
        if (body == null || body.getResponse() == null) {
            throw new com.example.demo.common.exceptions.BaseException(
                    com.example.demo.common.response.BaseResponseStatus.RESPONSE_ERROR);
        }
        PortOnePaymentResponse.PaymentData data = body.getResponse();
        int paidAmount = (int) data.getAmount();

        Payment.PaymentBuilder builder = Payment.builder()
                .user(user)
                .subscription(subscription)
                .merchantUid(merchantUid)
                .impUid(impUid)
                .amount(paidAmount)
                .paidAt(toLocalDateTime(data.getPaidAt()));

        // 상태 우선 확인 (포트원 상태가 paid가 아니면 실패로 기록)
        if (data.getStatus() != null && !"paid".equalsIgnoreCase(data.getStatus())) {
            builder.status(PaymentStatus.FAILED).failReason(
                    (data.getFailCode() != null ? data.getFailCode() + ": " : "") +
                            (data.getFailReason() != null ? data.getFailReason() : data.getStatus())
            );
        }
        else if (paidAmount != SUBSCRIPTION_PRICE) {
            cancelPaymentInternal(token, impUid, "Amount mismatch");
            builder.status(PaymentStatus.CANCELLED).failReason("Amount mismatch");
        } else {
            builder.status(PaymentStatus.PAID);
        }

        Payment saved = paymentRepository.save(builder.build());

        PaymentGateway gateway = PaymentGateway.of(
                data.getImpUid(),
                merchantUid,
                data.getPayMethod(),
                data.getPgProvider(),
                data.getPgTid(),
                data.getPgId(),
                BigDecimal.valueOf(data.getAmount()),
                data.getCurrency(),
                data.getApplyNum(),
                data.getBuyerName(),
                data.getCardCode(),
                data.getCardName(),
                data.getCardQuota(),
                data.getCardNumber(),
                PaymentGatewayStatus.valueOf(data.getStatus().toUpperCase()),
                data.getCardType(),
                toLocalDateTime(data.getStartedAt()),
                toLocalDateTime(data.getPaidAt()),
                toLocalDateTime(data.getCanceledAt()),
                toLocalDateTime(data.getFailedAt()),
                saved
        );
        paymentGatewayRepository.save(gateway);
        return saved;
    }

    public Payment cancelPayment(String impUid, String merchantUid,
                                 User user, Subscription subscription, String reason) {
        String token = getAccessToken();
        cancelPaymentInternal(token, impUid, reason);
        Payment entity = Payment.builder()
                .user(user)
                .subscription(subscription)
                .merchantUid(merchantUid)
                .impUid(impUid)
                .amount(SUBSCRIPTION_PRICE)
                .status(PaymentStatus.CANCELLED)
                .failReason(reason)
                .paidAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(entity);
    }

    private void cancelPaymentInternal(String token, String impUid, String reason) {
        String url = "https://api.iamport.kr/payments/cancel";
        Map<String, Object> body = new HashMap<>();
        body.put("imp_uid", impUid);
        body.put("reason", reason);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, Map.class);
    }

    public void validateAndCancelIfMismatch(Payment paid) {
        if (paid.getImpUid() == null) return;
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<PortOnePaymentResponse> response = restTemplate.exchange(
                "https://api.iamport.kr/payments/" + paid.getImpUid(),
                HttpMethod.GET,
                entity,
                PortOnePaymentResponse.class);
        PortOnePaymentResponse.PaymentData data = Objects.requireNonNull(response.getBody()).getResponse();
        int amount = (int) data.getAmount();
        if (amount != SUBSCRIPTION_PRICE) {
            cancelPaymentInternal(token, paid.getImpUid(), "Scheduled validation mismatch");
            Payment entityToSave = Payment.builder()
                    .user(paid.getUser())
                    .subscription(paid.getSubscription())
                    .merchantUid(paid.getMerchantUid())
                    .impUid(paid.getImpUid())
                    .amount(amount)
                    .status(PaymentStatus.CANCELLED)
                    .failReason("Scheduled validation mismatch")
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(entityToSave);
        }
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        return epochSeconds == null ? null : LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);
    }


}
