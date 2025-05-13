package com.carshare.rentalsystem.service.payment.stripe;

import com.carshare.rentalsystem.exception.StripeSessionCreationException;
import com.carshare.rentalsystem.model.PaymentSession;
import com.carshare.rentalsystem.service.payment.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class StripePaymentProvider implements PaymentProvider {
    private static final String CURRENCY = "usd";
    private static final long QUANTITY = 1L;
    private static final long STRIPE_CURRENCY_MULTIPLIER = 100L;

    @Value("${stripe.secret-api-key}")
    private String stripeSecretKey;

    @Value("${stripe.success-url}")
    private String successUrlProp;

    @Value("${stripe.cancel-url")
    private String cancelUrlProp;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public PaymentSession createSession(String description, BigDecimal amount) {
        try {
            SessionCreateParams params = buildStripeParams(description, amount);
            Session session = Session.create(params);
            return new PaymentSession(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new StripeSessionCreationException("Failed to create Stripe session", e);
        }
    }

    private SessionCreateParams buildStripeParams(String description, BigDecimal amount) {
        UriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder
                .fromCurrentContextPath();

        String successUrl = uriComponentsBuilder
                .path(successUrlProp)
                .toUriString() + "?session_id={CHECKOUT_SESSION_ID}";

        String cancelUrl = uriComponentsBuilder
                .path(cancelUrlProp)
                .toUriString();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(QUANTITY)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(amount
                                                        .multiply(BigDecimal.valueOf(
                                                                STRIPE_CURRENCY_MULTIPLIER))
                                                        .longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName(description)
                                                                .build())
                                                .build())
                                .build())
                .build();
    }
}
