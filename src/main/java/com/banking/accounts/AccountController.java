package com.banking.accounts;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String accountId) {
        Map<String, Object> response = new HashMap<>();
        
        // Simple business logic: validate account ID format
        if (!accountId.matches("\\d{10}")) {
            response.put("error", "Invalid account ID format");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Simulate account balance lookup
        BigDecimal balance = new BigDecimal("2543.75");
        
        response.put("accountId", accountId);
        response.put("balance", balance);
        response.put("currency", "USD");
        response.put("status", "ACTIVE");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@PathVariable String accountId, @RequestBody TransferRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Business logic: validate transfer
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            response.put("error", "Invalid transfer amount");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (request.getAmount().compareTo(new BigDecimal("50000.00")) > 0) {
            response.put("error", "Transfer amount exceeds daily limit");
            return ResponseEntity.badRequest().body(response);
        }
        
        String transferId = "TFR-" + System.currentTimeMillis();
        
        response.put("transferId", transferId);
        response.put("status", "COMPLETED");
        response.put("fromAccount", accountId);
        response.put("toAccount", request.getToAccountId());
        response.put("amount", request.getAmount());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "account-service");
        return ResponseEntity.ok(health);
    }
    
    public static class TransferRequest {
        private BigDecimal amount;
        private String toAccountId;
        private String description;
        
        // Getters and setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getToAccountId() { return toAccountId; }
        public void setToAccountId(String toAccountId) { this.toAccountId = toAccountId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
