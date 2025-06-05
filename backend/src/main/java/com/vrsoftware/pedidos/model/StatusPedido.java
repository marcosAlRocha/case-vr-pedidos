package com.vrsoftware.pedidos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class StatusPedido {
    private UUID idPedido;
    private Status status;
    private String mensagemErro;
    private LocalDateTime dataProcessamento;

    public enum Status {
        SUCESSO, FALHA
    }
}
