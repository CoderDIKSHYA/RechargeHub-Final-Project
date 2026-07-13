package com.capg.RechargeHub.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testErrorResponseDTO() {
        ErrorResponseDTO dto = new ErrorResponseDTO();
        dto.setStatus(400);
        dto.setError("Bad Request");
        dto.setMessage("Msg");
        dto.setTimestamp(LocalDateTime.now());
        Map<String, String> errors = new HashMap<>();
        errors.put("f", "e");
        dto.setValidationErrors(errors);

        assertEquals(400, dto.getStatus());
        assertEquals("Bad Request", dto.getError());
        assertEquals("Msg", dto.getMessage());
        assertNotNull(dto.getTimestamp());
        assertEquals(errors, dto.getValidationErrors());

        ErrorResponseDTO dto2 = new ErrorResponseDTO(500, "Err", "Msg");
        assertEquals(500, dto2.getStatus());
        
        ErrorResponseDTO dto3 = new ErrorResponseDTO(400, "Err", "Msg", errors);
        assertEquals(errors, dto3.getValidationErrors());
    }

    @Test
    void testPaymentRequest() {
        PaymentRequest req = new PaymentRequest();
        req.setRechargeId(1L);
        req.setUserId(101L);
        req.setAmount(BigDecimal.TEN);
        req.setPaymentMethod("UPI");
        req.setUserEmail("a@b.com");
        req.setMobileNumber("123");
        req.setOperatorName("Op");

        assertEquals(1L, req.getRechargeId());
        assertEquals(101L, req.getUserId());
        assertEquals(BigDecimal.TEN, req.getAmount());
        assertEquals("UPI", req.getPaymentMethod());
        assertEquals("a@b.com", req.getUserEmail());
        assertEquals("123", req.getMobileNumber());
        assertEquals("Op", req.getOperatorName());
        
        PaymentRequest req2 = new PaymentRequest(1L, 101L, BigDecimal.TEN, "UPI", "a@b.com", "123", "Op");
        assertEquals(1L, req2.getRechargeId());
    }

    @Test
    void testPaymentResponse() {
        PaymentResponse res = new PaymentResponse();
        res.setId(1L);
        res.setRechargeId(2L);
        res.setUserId(3L);
        res.setAmount(BigDecimal.ONE);
        res.setPaymentMethod("P");
        res.setStatus("S");
        res.setTransactionTime(LocalDateTime.now());
        res.setTransactionId("TX1");

        assertEquals(1L, res.getId());
        assertEquals(2L, res.getRechargeId());
        assertEquals(3L, res.getUserId());
        assertEquals(BigDecimal.ONE, res.getAmount());
        assertEquals("P", res.getPaymentMethod());
        assertEquals("S", res.getStatus());
        assertNotNull(res.getTransactionTime());
        assertEquals("TX1", res.getTransactionId());

        PaymentResponse res2 = new PaymentResponse(1L, 2L, 3L, BigDecimal.ONE, "P", "S", LocalDateTime.now());
        assertEquals(1L, res2.getId());
    }
    
    @Test
    void testPaymentEvent() {
        PaymentEvent event = new PaymentEvent();
        event.setTransactionId(1L);
        event.setRechargeId(2L);
        event.setUserId(3L);
        event.setStatus("S");
        event.setUserEmail("e");
        event.setAmount(BigDecimal.ONE);
        event.setMobileNumber("m");
        event.setOperatorName("o");
        
        assertEquals(1L, event.getTransactionId());
        assertEquals(2L, event.getRechargeId());
        assertEquals(3L, event.getUserId());
        assertEquals("S", event.getStatus());
        assertEquals("e", event.getUserEmail());
        assertEquals(BigDecimal.ONE, event.getAmount());
        assertEquals("m", event.getMobileNumber());
        assertEquals("o", event.getOperatorName());
        
        PaymentEvent event2 = new PaymentEvent(1L, 2L, 3L, "S", "e", BigDecimal.ONE, "m", "o");
        assertEquals(1L, event2.getTransactionId());
    }
}
