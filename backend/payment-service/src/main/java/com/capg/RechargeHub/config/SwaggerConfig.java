/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : SwaggerConfig (payment-service)
 * DESCRIPTION:
 *   OpenAPI (Swagger) configuration for the Payment Service.
 *   Defines API metadata for the Swagger UI accessible at
 *   http://localhost:8084/swagger-ui/index.html
 * ================================================================ */
package com.capg.RechargeHub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /* ================================================================
     * METHOD: paymentServiceOpenAPI
     * DESCRIPTION:
     *   Creates and returns the OpenAPI bean with project metadata
     *   for the Payment Service API documentation.
     * ================================================================ */
    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .description("REST APIs for processing payments and querying transaction history")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Recharge Nova Team")
                                .email("support@rechargenova.com")));
    }
}
