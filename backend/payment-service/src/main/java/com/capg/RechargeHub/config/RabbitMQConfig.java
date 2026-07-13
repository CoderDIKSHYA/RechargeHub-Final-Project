/** ================================================================
 * AUTHOR  : Dikshya Runuwal
 * CLASS   : RabbitMQConfig (payment-service)
 * DESCRIPTION:
 *   RabbitMQ configuration for the Payment Service.
 *   Declares the queue, topic exchange, and binding used to
 *   publish PaymentEvent messages after a transaction is processed.
 *   Uses Jackson2JsonMessageConverter for JSON serialization.
 * ================================================================ */
package com.capg.RechargeHub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "payment_queue";
    public static final String EXCHANGE = "payment_exchange";
    public static final String ROUTING_KEY = "payment_routing_key";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
