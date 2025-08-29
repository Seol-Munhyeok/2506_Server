package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.PaymentGateway;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {
}
