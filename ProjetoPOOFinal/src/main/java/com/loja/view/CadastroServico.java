package com.loja.view;

import com.loja.model.Servico;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.loja.database.DatabaseConnection;

public class CadastroServico extends JFrame {
    private JTextField nomeField;
    private JTextField descricaoField;
    private JTextField clienteField;
    private JTextField precoField;
    private JTextField dataInicialField;
    private JTextField dataFinalField;
    private JButton cancelarButton;
    private JButton salvarButton;

    private MainPage mainPage;

    public CadastroServico(Servico servico, MainPage mainPage) {
        this.mainPage = mainPage;

        setTitle(servico == null ? "Cadastro de Serviço" : "Edição de Serviço");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        nomeField = new JTextField(20);
        descricaoField = new JTextField(20);
        clienteField = new JTextField(20);
        precoField = new JTextField(20);
        dataInicialField = new JTextField(20);
        dataFinalField = new JTextField(20);
        cancelarButton = new JButton("Cancelar");
        salvarButton = new JButton(servico == null ? "Cadastrar" : "Editar");

        if (servico != null) {
            nomeField.setText(servico.getNome());
            descricaoField.setText(servico.getDescricao());
            clienteField.setText(servico.getCliente());
            precoField.setText(String.valueOf(servico.getPreco()));
            dataInicialField.setText(servico.getDataInicial());
            dataFinalField.setText(servico.getDataFinal());
        }

        Dimension buttonSize = new Dimension(120, 30);
        cancelarButton.setPreferredSize(buttonSize);
        salvarButton.setPreferredSize(buttonSize);

        JPanel panel = new JPanel(new GridLayout(7, 2));
        panel.add(new JLabel("Serviço:"));
        panel.add(nomeField);
        panel.add(new JLabel("Descrição:"));
        panel.add(descricaoField);
        panel.add(new JLabel("Cliente:"));
        panel.add(clienteField);
        panel.add(new JLabel("Preço:"));
        panel.add(precoField);
        panel.add(new JLabel("Data Inicial:"));
        panel.add(dataInicialField);
        panel.add(new JLabel("Data Final:"));
        panel.add(dataFinalField);
        panel.add(cancelarButton);
        panel.add(salvarButton);

        add(panel);

        cancelarButton.addActionListener(e -> {
            new MainPage("Nome do Usuário");
            dispose();
        });

        salvarButton.addActionListener(e -> {
            if (servico == null) {
                cadastrarServico();
            } else {
                editarServico(servico);
            }
        });

        setVisible(true);
    }

    private void cadastrarServico() {
        String nome = nomeField.getText();
        String descricao = descricaoField.getText();
        String cliente = clienteField.getText();
        String precoText = precoField.getText();
        String dataInicial = dataInicialField.getText();
        String dataFinal = dataFinalField.getText();

        if (nome.isEmpty() || descricao.isEmpty() || cliente.isEmpty() || precoText.isEmpty() || dataInicial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os campos corretamente.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double preco = Double.parseDouble(precoText);
            if (preco < 0) {
                throw new NumberFormatException();
            }

            if (!isValidDate(dataInicial) || (!dataFinal.isEmpty() && !isValidDate(dataFinal))) {
                JOptionPane.showMessageDialog(this, "Datas inválidas. Use o formato dd/MM/yyyy.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO servicos (nome, descricao, cliente, preco, data_inicial, data_final) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nome);
                    stmt.setString(2, descricao);
                    stmt.setString(3, cliente);
                    stmt.setDouble(4, preco);
                    stmt.setDate(5, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(dataInicial))));
                    if (dataFinal.isEmpty()) {
                        stmt.setNull(6, java.sql.Types.DATE);
                    } else {
                        stmt.setDate(6, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(dataFinal))));
                    }
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Serviço cadastrado com sucesso!");
                if (mainPage != null) {
                    mainPage.loadServicos();
                }
                dispose();
            } catch (SQLException | ParseException e) {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar serviço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarServico(Servico servico) {
        servico.setNome(nomeField.getText());
        servico.setDescricao(descricaoField.getText());
        servico.setCliente(clienteField.getText());
        servico.setPreco(Double.parseDouble(precoField.getText()));
        servico.setDataInicial(dataInicialField.getText());
        servico.setDataFinal(dataFinalField.getText());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE servicos SET servico = ?, descricao = ?, cliente = ?, preco = ?, data_inicial = ?, data_final = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, servico.getNome());
                stmt.setString(2, servico.getDescricao());
                stmt.setString(3, servico.getCliente());
                stmt.setDouble(4, servico.getPreco());
                stmt.setDate(5, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(servico.getDataInicial()))));
                if (servico.getDataFinal().isEmpty()) {
                    stmt.setNull(6, java.sql.Types.DATE);
                } else {
                    stmt.setDate(6, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(servico.getDataFinal()))));
                }
                stmt.setInt(7, servico.getId());
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Serviço editado com sucesso!");
            if (mainPage != null) {
                mainPage.loadServicos();
            }
            dispose();
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar serviço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date parsedDate = sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CadastroServico(null, null));
    }
}