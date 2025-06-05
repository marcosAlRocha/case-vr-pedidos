package com.vrsoftware.pedidos.service;

import com.vrsoftware.pedidos.model.StatusPedido;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatusPedidoService {
    private final Map<UUID, StatusPedido> statusMap = new ConcurrentHashMap<>();

    public void atualizarStatus(StatusPedido status) {
        statusMap.put(status.getIdPedido(), status);
    }

    public StatusPedido obterStatus(UUID id) {
        return statusMap.get(id);
    }
}
