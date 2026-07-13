package com.capg.RechargeHub.client;

import com.capg.RechargeHub.dto.OtpEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/send-otp")
    ResponseEntity<String> sendOtpEmail(@RequestBody OtpEmailRequest request);
}
