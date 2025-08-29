package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.PaymentStatus;
import com.example.demo.src.payment.model.GetPaymentRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentDataManager paymentDataManager;

    public List<GetPaymentRes> getPayments(Long userId, PaymentStatus status, LocalDateTime startAt, LocalDateTime endAt) {
        return paymentDataManager.findPayments(userId, status, startAt, endAt).stream()
                .map(p -> GetPaymentRes.builder()
                        .id(p.getId())
                        .merchantUid(p.getMerchantUid())
                        .impUid(p.getImpUid())
                        .amount(p.getAmount())
                        .status(p.getStatus())
                        .failReason(p.getFailReason())
                        .paidAt(p.getPaidAt())
                        .build())
                .collect(Collectors.toList());
    }
}
