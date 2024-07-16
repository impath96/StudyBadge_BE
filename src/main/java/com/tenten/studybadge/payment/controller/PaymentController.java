package com.tenten.studybadge.payment.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.payment.dto.PaymentConfirmRequest;
import com.tenten.studybadge.payment.dto.PaymentRequest;
import com.tenten.studybadge.payment.dto.PaymentResponse;
import com.tenten.studybadge.payment.dto.PaymentConfirm;
import com.tenten.studybadge.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    @Operation(summary = "결제 요청", description = "토스페이먼츠에 결제 요청할 API", security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "paymentRequest", description = "결제 요청을 위한 값들")
    @PostMapping("/toss")
    public ResponseEntity<PaymentResponse> requestPayment(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody PaymentRequest paymentRequest) {

        PaymentResponse response = paymentService.requestPayment(principal.getId(), paymentRequest);

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "결제 성공", description = "토스페이먼츠에서 결제 승인되어 성공 정보를 저장하는 API", security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "confirmRequest", description = "결제 승인을 위한 요청 값(paymentKey, orderId, amount)")
    @PostMapping("/success")
    public ResponseEntity<PaymentConfirm> paymentConfirm(@Valid @RequestBody PaymentConfirmRequest confirmRequest) {

        PaymentConfirm confirm = paymentService.paymentConfirm(confirmRequest);

        return ResponseEntity.ok(confirm);

    }
}
