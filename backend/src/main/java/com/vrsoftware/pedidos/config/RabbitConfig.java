package com.vrsoftware.pedidos.config;

import org.springframework.amqp.core.*;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;

public class RabbitConfig {

    public static final String FILA_PEDIDOS = "pedidos.entrada.marcos";
    public static final String FILA_PEDIDOS_DLQ = "pedidos.entrada.marcos.dlq";
    public static final String FILA_STATUS_SUCESSO = "pedidos.status.sucesso.marcos";
    public static final String FILA_STATUS_FALHA = "pedidos.status.falha.marcos";

    // Exchange para mensagens (usar direct ou topic)
    public static final String EXCHANGE = "pedidos.exchange.marcos";

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue filaPedidos() {
        return QueueBuilder.durable(FILA_PEDIDOS)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", FILA_PEDIDOS_DLQ)
                .build();
    }

    @Bean
    public Queue filaPedidosDlq() {
        return QueueBuilder.durable(FILA_PEDIDOS_DLQ).build();
    }

    @Bean
    public Queue filaStatusSucesso() {
        return QueueBuilder.durable(FILA_STATUS_SUCESSO).build();
    }

    @Bean
    public Queue filaStatusFalha() {
        return QueueBuilder.durable(FILA_STATUS_FALHA).build();
    }

    @Bean
    public Binding bindingPedidos(Queue filaPedidos, DirectExchange exchange) {
        return BindingBuilder.bind(filaPedidos).to(exchange).with(FILA_PEDIDOS);
    }

    @Bean
    public Binding bindingPedidosDlq(Queue filaPedidosDlq, DirectExchange exchange) {
        return BindingBuilder.bind(filaPedidosDlq).to(exchange).with(FILA_PEDIDOS_DLQ);
    }

    @Bean
    public Binding bindingStatusSucesso(Queue filaStatusSucesso, DirectExchange exchange) {
        return BindingBuilder.bind(filaStatusSucesso).to(exchange).with(FILA_STATUS_SUCESSO);
    }

    @Bean
    public Binding bindingStatusFalha(Queue filaStatusFalha, DirectExchange exchange) {
        return BindingBuilder.bind(filaStatusFalha).to(exchange).with(FILA_STATUS_FALHA);
    }

}
