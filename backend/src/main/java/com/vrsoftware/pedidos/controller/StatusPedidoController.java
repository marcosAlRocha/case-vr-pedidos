package com.vrsoftware.pedidos.controller;

import com.vrsoftware.pedidos.model.StatusPedido;
import com.vrsoftware.pedidos.service.StatusPedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos/status")
public class StatusPedidoController {

    private final StatusPedidoService statusPedidoService;

    public StatusPedidoController(StatusPedidoService statusPedidoService) {
        this.statusPedidoService = statusPedidoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusPedido> consultarStatus(@PathVariable UUID id) {
        StatusPedido status = statusPedidoService.obterStatus(id);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

}
