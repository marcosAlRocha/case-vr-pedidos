package com.vrsoftware.pedidos.controller;

import com.vrsoftware.pedidos.config.RabbitConfig;
import com.vrsoftware.pedidos.model.Pedido;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PedidoController.BASE_API_PATH)
public class PedidoController {

    public static final String BASE_API_PATH = "/api/pedidos";
    private final RabbitTemplate rabbitTemplate;

    public PedidoController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<?> receberPedido(@Valid @RequestBody Pedido pedido, BindingResult bindingResult) {
        if (pedido.getId() == null) {
            pedido.setId(java.util.UUID.randomUUID());
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Dados inválidos");
        }
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.FILA_PEDIDOS, pedido);
        return ResponseEntity.accepted().body(pedido.getId());
    }
}
