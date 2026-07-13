package com.capg.RechargeHub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : SwaggerConfig
 * DESCRIPTION:
 *   OpenAPI (Swagger) configuration for the Recharge Service.
 *   Defines API metadata including title, description, version,
 *   and contact information displayed in the Swagger UI.
 *   Swagger UI is accessible at: /swagger-ui/index.html
 * ================================================================ */
@Configuration
public class SwaggerConfig {

    /* ================================================================
     * METHOD: rechargeServiceOpenAPI
     * DESCRIPTION:
     *   Creates and returns the OpenAPI bean with project metadata
     *   for the Recharge Service API documentation.
     * ================================================================ */
    @Bean
    public OpenAPI rechargeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Recharge Service API")
                        .description("REST APIs for mobile recharge operations in Recharge Nova")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Recharge Nova Team")
                                .email("support@rechargenova.com")));
    }
}
