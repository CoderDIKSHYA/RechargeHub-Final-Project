package com.capg.RechargeHub.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testTransaction() {
        Transaction t = new Transaction();
        t.setId(1L);
        t.setRechargeId(2L);
        t.setUserId(3L);
        t.setAmount(BigDecimal.ONE);
        t.setPaymentMethod("P");
        t.setStatus("S");
        t.setTransactionTime(LocalDateTime.now());

        assertEquals(1L, t.getId());
        assertEquals(2L, t.getRechargeId());
        assertEquals(3L, t.getUserId());
        assertEquals(BigDecimal.ONE, t.getAmount());
        assertEquals("P", t.getPaymentMethod());
        assertEquals("S", t.getStatus());
        assertNotNull(t.getTransactionTime());

        Transaction t2 = new Transaction(1L, 2L, 3L, BigDecimal.ONE, "P", "S", LocalDateTime.now());
        assertEquals(1L, t2.getId());
    }
}
