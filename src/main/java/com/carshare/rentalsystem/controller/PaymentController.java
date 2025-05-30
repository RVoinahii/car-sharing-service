package com.carshare.rentalsystem.controller;

import static com.carshare.rentalsystem.controller.RentalController.AUTHORITY_MANAGER;

import com.carshare.rentalsystem.dto.payment.request.dto.CreatePaymentRequestDto;
import com.carshare.rentalsystem.dto.payment.request.dto.PaymentSearchParameters;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentCancelResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentPreviewResponseDto;
import com.carshare.rentalsystem.dto.payment.response.dto.PaymentResponseDto;
import com.carshare.rentalsystem.model.User;
import com.carshare.rentalsystem.service.payment.stripe.StripePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments management", description = "Endpoints for managing payments")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final StripePaymentService paymentService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(
            summary = "Retrieve payments",
            description = "Returns a paginated list of payments records. Customers can only view"
                    + " their own payments. Managers can view all rentals and filter them using"
                    + " 'user ID' parameter. (Required roles: CUSTOMER, MANAGER)"
    )
    public Page<PaymentResponseDto> getAllPayments(Authentication authentication,
            PaymentSearchParameters searchParameters, Pageable pageable) {
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AUTHORITY_MANAGER));

        if (isManager) {
            return paymentService.getSpecificPayments(searchParameters, pageable);
        }

        return paymentService.getPaymentsById(getAuthenticatedUserId(authentication), pageable);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment details by ID",
            description = "Returns detailed information about a specific payment by its ID."
                    + "Customers can only access their own payments, while managers can access"
                    + " any payment. (Required roles: CUSTOMER, MANAGER)"
    )
    public PaymentResponseDto getPaymentDetails(Authentication authentication,
                                                @PathVariable Long paymentId) {
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AUTHORITY_MANAGER));

        if (isManager) {
            return paymentService.getAnyPaymentInfo(paymentId);
        }

        return paymentService.getCustomerPaymentInfo(
                getAuthenticatedUserId(authentication), paymentId
        );
    }

    @GetMapping("/success")
    @Operation(
            summary = "Confirm payment success",
            description = "Handles a successful Stripe session and marks the corresponding payment"
                    + " as COMPLETED. Used internally by Stripe after user completes payment."
                    + " (Required roles: CUSTOMER, MANAGER)"
    )
    public PaymentResponseDto paymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.handleSuccess(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(
            summary = "Handle cancelled payment",
            description = "Returns information about a cancelled payment. Stripe redirects here"
                    + "  when the user cancels the session. (Required roles: CUSTOMER, MANAGER)"
    )
    public PaymentCancelResponseDto paymentCancel(@RequestParam("session_id") String sessionId) {
        return paymentService.handleCancel(sessionId);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping
    @Operation(
            summary = "Create a new Stripe payment session",
            description = "Creates a Stripe session for the specified rental and payment type."
                    + "  The session is valid for 24 hours. (Required roles: CUSTOMER, MANAGER)"
    )
    public PaymentPreviewResponseDto createPaymentSession(
            @RequestBody CreatePaymentRequestDto requestDto, Authentication authentication) {
        return paymentService.createStripeSession(requestDto,
                getAuthenticatedUserId(authentication));
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PatchMapping("/renew/{paymentId}")
    @Operation(
            summary = "Renew expired payment session",
            description = "Creates a new Stripe session for an expired payment. Only"
                    + "  allowed for payments with EXPIRED status."
                    + " (Required roles: CUSTOMER, MANAGER)"
    )
    public PaymentPreviewResponseDto renewPayment(@PathVariable Long paymentId,
                                                  Authentication authentication) {
        return paymentService.renewStripeSession(getAuthenticatedUserId(authentication),
                paymentId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
