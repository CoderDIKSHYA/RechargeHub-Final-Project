package com.capg.RechargeHub.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "recharge-service", url = "${RECHARGE_SERVICE_URL:http://recharge-service:8086}")
public interface RechargeClient {

    @PutMapping("/recharges/{id}/status")
    Object updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "transactionId", required = false) String transactionId
    );
}
