package com.vrsoftware.pedidos.service;

import com.vrsoftware.pedidos.config.RabbitConfig;
import com.vrsoftware.pedidos.exception.ExcecaoDeProcessamento;
import com.vrsoftware.pedidos.model.Pedido;
import com.vrsoftware.pedidos.model.StatusPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PedidoProcessamentoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoProcessamentoService.class);
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private final StatusPedidoService statusPedidoService;

    public PedidoProcessamentoService(RabbitTemplate rabbitTemplate, StatusPedidoService statusPedidoService) {
        this.rabbitTemplate = rabbitTemplate;
        this.statusPedidoService = statusPedidoService;
    }

    @RabbitListener(queues = RabbitConfig.FILA_PEDIDOS)
    public void processarPedido(Pedido pedido, Message message) {
        logger.info("Início processamento pedido: {}", pedido.getId());
        try {
            // Simula tempo de processamento 1-3s
            Thread.sleep(1000 + random.nextInt(2000));

            // Simula exceção com 20% de chance
            if (random.nextDouble() < 0.2) {
                throw new ExcecaoDeProcessamento("Erro simulada no processamento");
            }

            // Enviar status SUCESSO
            StatusPedido status = new StatusPedido();
            status.setIdPedido(pedido.getId());
            status.setStatus(StatusPedido.Status.SUCESSO);
            status.setMensagemErro(null);
            status.setDataProcessamento(LocalDateTime.now());

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.FILA_STATUS_SUCESSO, status);

            logger.info("Pedido {} processado com sucesso", pedido.getId());

        } catch (ExcecaoDeProcessamento e) {
            logger.error("Falha no processamento do pedido {}: {}", pedido.getId(), e.getMessage());

            // Enviar status FALHA
            StatusPedido status = new StatusPedido();
            status.setIdPedido(pedido.getId());
            status.setStatus(StatusPedido.Status.FALHA);
            status.setMensagemErro(e.getMessage());
            status.setDataProcessamento(LocalDateTime.now());

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.FILA_STATUS_FALHA, status);

            // Rejeitar mensagem para DLQ (Spring AMQP vai cuidar disso automaticamente se configurado)
            throw e;  // Re-throw para rejeitar a mensagem
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
