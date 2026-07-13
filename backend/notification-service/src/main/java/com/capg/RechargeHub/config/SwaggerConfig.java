/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : SwaggerConfig (notification-service)
 * DESCRIPTION:
 *   OpenAPI (Swagger) configuration for the Notification Service.
 *   Defines API metadata for the Swagger UI accessible at
 *   http://localhost:8085/swagger-ui/index.html
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
     * METHOD: notificationServiceOpenAPI
     * DESCRIPTION:
     *   Creates and returns the OpenAPI bean with project metadata
     *   for the Notification Service API documentation.
     * ================================================================ */
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("REST APIs for querying notifications triggered by payment events")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Recharge Nova Team")
                                .email("support@rechargenova.com")));
    }
}
