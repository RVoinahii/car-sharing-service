package com.carshare.rentalsystem.controller;

import static com.carshare.rentalsystem.controller.RentalController.AUTHORITY_MANAGER;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Payments management",
        description = """
        Endpoints for handling rental payment workflows via Stripe.

        - **Customers** can create payment sessions, view their own payments, and handle
         payment results.
        - **Managers** have full access to all payment records and can search by user or status.

        Includes:
        - Stripe session creation and renewal
        - Payment success/cancellation handling
        - Role-based access to payment data
            """
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final StripePaymentService paymentService;

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(
            summary = "Retrieve payments",
            description = """
        Returns a paginated list of payment records.
        
        - Customers can only view **their own** payments.
        - Managers can view **all** payments and filter them using optional search parameters
        (e.g. `userId`, `status`).

        **Required roles**: CUSTOMER, MANAGER
            """
    )
    public Page<PaymentPreviewResponseDto> getAllPayments(Authentication authentication,
            PaymentSearchParameters searchParameters, Pageable pageable) {
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AUTHORITY_MANAGER));

        if (isManager) {
            return paymentService.getSpecificPayments(searchParameters, pageable);
        }

        return paymentService.getPaymentsByUserId(getAuthenticatedUserId(authentication), pageable);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment details by ID",
            description = """
        Returns full details of a specific payment by its ID.
        
        - Customers can access only their **own** payments.
        - Managers can access **any** payment.

        **Required roles**: CUSTOMER, MANAGER
            """
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
            description = """
        Endpoint used internally by Stripe to confirm a **successful** payment session.
        Automatically marks the associated payment as `COMPLETED` and updates the related
         rental status if necessary.

        This endpoint is triggered after the user finishes the Stripe checkout process.

        ⚠️ Accessible via redirect from Stripe only.

        **Required roles**: CUSTOMER, MANAGER
            """
    )
    public PaymentResponseDto paymentSuccess(@RequestParam("session_id") String sessionId) {
        return paymentService.handleSuccess(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(
            summary = "Handle cancelled payment",
            description = """
        Returns information about a payment that was **cancelled** via Stripe checkout.
        
        Does not modify the payment status unless explicitly processed on Stripe’s side.

        ⚠️ Accessible via redirect from Stripe when a user **aborts** payment.

        **Required roles**: CUSTOMER, MANAGER
            """
    )
    public PaymentCancelResponseDto paymentCancel(@RequestParam("session_id") String sessionId) {
        return paymentService.handleCancel(sessionId);
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PostMapping("/{rentalId}")
    @Operation(
            summary = "Create a new Stripe payment session",
            description = """
        Initiates a **Stripe Checkout Session** for the specified rental.

        - The session remains valid for **24 hours**.
        - Only one active session can exist per user at a time.
        - The amount and session type are calculated dynamically depending on:
          - rental cancellation,
          - early return,
          - late return,
          - or standard completion.

        **Required roles**: CUSTOMER, MANAGER
            """
    )
    public PaymentResponseDto createPaymentSession(
            @PathVariable Long rentalId, Authentication authentication) {
        return paymentService.createStripeSession(rentalId,
                getAuthenticatedUserId(authentication));
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'MANAGER')")
    @PatchMapping("/renew/{paymentId}")
    @Operation(
            summary = "Renew expired payment session",
            description = """
        Creates a **new Stripe session** for a payment with status `EXPIRED`.

        - Applicable only if the original session has expired (after 24h).
        - The session is recreated with the same logic as initial creation.

        **Required roles**: CUSTOMER, MANAGER
            """
    )
    public PaymentResponseDto renewPayment(@PathVariable Long paymentId,
                                                  Authentication authentication) {
        return paymentService.renewStripeSession(getAuthenticatedUserId(authentication),
                paymentId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
