/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : SwaggerConfig (operator-service)
 * DESCRIPTION:
 *   OpenAPI (Swagger) configuration for the Operator Service.
 *   Defines API metadata for the Swagger UI accessible at
 *   http://localhost:8083/swagger-ui/index.html
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
     * METHOD: operatorServiceOpenAPI
     * DESCRIPTION:
     *   Creates and returns the OpenAPI bean with project metadata
     *   for the Operator Service API documentation.
     * ================================================================ */
    @Bean
    public OpenAPI operatorServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Operator Service API")
                        .description("REST APIs for managing operators and recharge plans")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Recharge Nova Team")
                                .email("support@rechargenova.com")));
    }
}
