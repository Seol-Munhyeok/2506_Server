package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class PaymentDataManager {

    private final PaymentRepository paymentRepository;

    public List<Payment> findPayments(Long userId, PaymentStatus status, LocalDateTime startAt, LocalDateTime endAt) {
        Specification<Payment> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (startAt != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("paidAt"), startAt));
        }

        if (endAt != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("paidAt"), endAt));
        }

        return paymentRepository.findAll(spec);
    }
}