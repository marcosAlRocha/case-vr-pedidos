package com.vr;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;



public class PedidoSwingApp extends JFrame{
    private JTextField produtoField = new JTextField(20);
    private JTextField quantidadeField = new JTextField(5);
    private JButton enviarButton = new JButton("Enviar Pedido");
    private DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Status"}, 0);
    private JTable pedidosTable = new JTable(tableModel);

    private Map<UUID, String> pedidosStatus = new ConcurrentHashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private OkHttpClient client = new OkHttpClient();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public PedidoSwingApp() {
        super("Pedidos Swing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.add(new JLabel("Produto:"));
        form.add(produtoField);
        form.add(new JLabel("Quantidade:"));
        form.add(quantidadeField);
        form.add(enviarButton);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(pedidosTable), BorderLayout.CENTER);

        enviarButton.addActionListener(this::enviarPedido);

        scheduler.scheduleAtFixedRate(this::pollStatus, 3, 3, TimeUnit.SECONDS);

        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void enviarPedido(ActionEvent e) {
        String produto = produtoField.getText().trim();
        String quantidadeStr = quantidadeField.getText().trim();
        int quantidade;
        try {
            quantidade = Integer.parseInt(quantidadeStr);
        } catch (NumberFormatException ex) {
            showError("Quantidade inválida.");
            return;
        }
        if (produto.isEmpty() || quantidade <= 0) {
            showError("Preencha produto e quantidade corretamente.");
            return;
        }

        UUID id = UUID.randomUUID();
        Map<String, Object> pedido = new HashMap<>();
        pedido.put("id", id.toString());
        pedido.put("produto", produto);
        pedido.put("quantidade", quantidade);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        pedido.put("dataCriacao", LocalDateTime.now().format(formatter));

        try {
            String json = mapper.writeValueAsString(pedido);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/pedidos")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException ex) {
                    SwingUtilities.invokeLater(() -> showError("Falha ao enviar pedido: " + ex.getMessage()));
                }
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        pedidosStatus.put(id, "ENVIADO, AGUARDANDO PROCESSO");
                        SwingUtilities.invokeLater(() -> tableModel.addRow(new Object[]{id, "ENVIADO, AGUARDANDO PROCESSO"}));
                    } else {
                        SwingUtilities.invokeLater(() -> showError("Erro ao enviar pedido: " + response.message()));
                    }
                }
            });
        } catch (Exception ex) {
            showError("Erro ao serializar pedido.");
        }
    }

    private void pollStatus() {
        List<UUID> aguardando = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : pedidosStatus.entrySet()) {
            if (entry.getValue().contains("AGUARDANDO")) {
                aguardando.add(entry.getKey());
            }
        }
        for (UUID id : aguardando) {
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/pedidos/status/" + id)
                    .get()
                    .build();
            client.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException ex) {
                    // Ignora polling falho, pode logar se quiser
                }
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            Map statusMap = mapper.readValue(response.body().string(), Map.class);
                            String status = String.valueOf(statusMap.get("status"));
                            if ("SUCESSO".equals(status) || "FALHA".equals(status)) {
                                pedidosStatus.put(id, status);
                                SwingUtilities.invokeLater(() -> atualizarTabela(id, status));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            });
        }
    }

    private void atualizarTabela(UUID id, String status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (id.toString().equals(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(status, i, 1);
                break;
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PedidoSwingApp::new);
    }
}
