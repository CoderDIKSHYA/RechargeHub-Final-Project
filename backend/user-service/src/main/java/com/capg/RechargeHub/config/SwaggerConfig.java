/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : SwaggerConfig (user-service)
 * DESCRIPTION:
 *   OpenAPI (Swagger) configuration for the User Service.
 *   Defines API metadata for the Swagger UI accessible at
 *   http://localhost:8082/swagger-ui/index.html
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
     * METHOD: userServiceOpenAPI
     * DESCRIPTION:
     *   Creates and returns the OpenAPI bean with project metadata
     *   for the User Service API documentation.
     * ================================================================ */
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("REST APIs for user registration, login, and profile management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Recharge Nova Team")
                                .email("support@rechargenova.com")));
    }
}
