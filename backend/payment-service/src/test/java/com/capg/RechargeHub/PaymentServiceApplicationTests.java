package com.capg.RechargeHub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"RAZORPAY_KEY_ID=test_id",
		"RAZORPAY_KEY_SECRET=test_secret"
})
class PaymentServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
