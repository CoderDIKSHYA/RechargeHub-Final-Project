package com.capg.RechargeHub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"CLOUDINARY_CLOUD_NAME=test",
		"CLOUDINARY_API_KEY=test",
		"CLOUDINARY_API_SECRET=test"
})
class OperatorServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
