package com.markfest.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    @PostMapping
    public ResponseEntity<?> pay(@RequestBody Map<String,Object> body) {
        // Simulate payment success
        return ResponseEntity.ok(Map.of("status","SUCCEEDED","paymentId",12345));
    }
}
