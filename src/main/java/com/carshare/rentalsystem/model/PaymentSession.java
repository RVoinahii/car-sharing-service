package com.carshare.rentalsystem.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentSession {
    private String sessionId;
    private String sessionUrl;

    public PaymentSession(String sessionId, String sessionUrl) {
        this.sessionId = sessionId;
        this.sessionUrl = sessionUrl;
    }
}
