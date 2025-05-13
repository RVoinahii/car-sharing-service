package com.carshare.rentalsystem.service.payment;

import com.carshare.rentalsystem.model.PaymentSession;
import java.math.BigDecimal;

public interface PaymentProvider {
    PaymentSession createSession(String description, BigDecimal amount);
}
